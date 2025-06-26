const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const db = admin.firestore();

// ==================== USER MANAGEMENT FUNCTIONS ====================

/**
 * Triggered when a new user is created via Firebase Auth
 * Creates a corresponding user document in Firestore
 */
exports.createUserProfile = functions.auth.user().onCreate(async (user) => {
  try {
    const userProfile = {
      uid: user.uid,
      email: user.email,
      firstName: '',
      lastName: '',
      role: 'guard', // Default role
      estateId: null,
      isActive: false, // Requires admin activation
      employeeId: null,
      permissions: ['clock_in_out', 'incident_reporting', 'view_own_data'],
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      lastLoginTime: null,
      fcmToken: null
    };

    await db.collection('users').doc(user.uid).set(userProfile);
    
    // Log the user creation
    await logAuditEvent('USER_CREATED', user.uid, null, {
      email: user.email,
      createdVia: 'firebase_auth'
    });

    console.log(`User profile created for ${user.email}`);
  } catch (error) {
    console.error('Error creating user profile:', error);
  }
});

/**
 * Triggered when a user is deleted from Firebase Auth
 * Soft deletes the user document in Firestore
 */
exports.deleteUserProfile = functions.auth.user().onDelete(async (user) => {
  try {
    await db.collection('users').doc(user.uid).update({
      isActive: false,
      deletedAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    // Log the user deletion
    await logAuditEvent('USER_DELETED', user.uid, null, {
      email: user.email
    });

    console.log(`User profile soft deleted for ${user.email}`);
  } catch (error) {
    console.error('Error deleting user profile:', error);
  }
});

// ==================== INCIDENT MANAGEMENT FUNCTIONS ====================

/**
 * Triggered when a new incident is created
 * Sends notifications and performs additional processing
 */
exports.processNewIncident = functions.firestore
  .document('incidents/{incidentId}')
  .onCreate(async (snap, context) => {
    try {
      const incident = snap.data();
      const incidentId = context.params.incidentId;

      // Send notification to admins for high/critical incidents
      if (incident.severity === 'HIGH' || incident.severity === 'CRITICAL') {
        await notifyAdmins(incident, incidentId);
      }

      // Log the incident creation
      await logAuditEvent('INCIDENT_CREATED', incident.userId, incidentId, {
        severity: incident.severity,
        category: incident.category
      });

      // Update incident statistics
      await updateIncidentStats(incident.estateId, incident.severity);

      console.log(`Processed new incident: ${incidentId}`);
    } catch (error) {
      console.error('Error processing new incident:', error);
    }
  });

/**
 * Triggered when an incident is updated
 * Logs changes and sends notifications if needed
 */
exports.processIncidentUpdate = functions.firestore
  .document('incidents/{incidentId}')
  .onUpdate(async (change, context) => {
    try {
      const before = change.before.data();
      const after = change.after.data();
      const incidentId = context.params.incidentId;

      // Check if status changed
      if (before.status !== after.status) {
        await logAuditEvent('INCIDENT_STATUS_CHANGED', after.userId, incidentId, {
          oldStatus: before.status,
          newStatus: after.status
        });

        // Notify relevant parties of status change
        if (after.status === 'RESOLVED') {
          await notifyIncidentResolved(after, incidentId);
        }
      }

      console.log(`Processed incident update: ${incidentId}`);
    } catch (error) {
      console.error('Error processing incident update:', error);
    }
  });

// ==================== TIME TRACKING FUNCTIONS ====================

/**
 * Triggered when a new time entry is created
 * Validates and processes the time entry
 */
exports.processTimeEntry = functions.firestore
  .document('timeEntries/{entryId}')
  .onCreate(async (snap, context) => {
    try {
      const timeEntry = snap.data();
      const entryId = context.params.entryId;

      // Validate time entry (check for duplicates, validate location, etc.)
      const isValid = await validateTimeEntry(timeEntry);
      
      if (!isValid) {
        // Mark as invalid and notify admin
        await snap.ref.update({
          isValid: false,
          validationError: 'Failed validation checks',
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        return;
      }

      // Calculate shift duration if this is a clock out
      if (timeEntry.type === 'CLOCK_OUT') {
        await calculateShiftDuration(timeEntry);
      }

      // Log the time entry
      await logAuditEvent('TIME_ENTRY_CREATED', timeEntry.userId, entryId, {
        type: timeEntry.type,
        location: timeEntry.location
      });

      console.log(`Processed time entry: ${entryId}`);
    } catch (error) {
      console.error('Error processing time entry:', error);
    }
  });

// ==================== NOTIFICATION FUNCTIONS ====================

/**
 * Send push notification to all admin users
 */
async function notifyAdmins(incident, incidentId) {
  try {
    const adminsSnapshot = await db.collection('users')
      .where('role', '==', 'admin')
      .where('isActive', '==', true)
      .where('fcmToken', '!=', null)
      .get();

    const notifications = [];
    
    adminsSnapshot.forEach(doc => {
      const admin = doc.data();
      if (admin.fcmToken) {
        notifications.push({
          token: admin.fcmToken,
          notification: {
            title: `${incident.severity} Incident Reported`,
            body: `${incident.category}: ${incident.description.substring(0, 100)}...`
          },
          data: {
            type: 'incident',
            incidentId: incidentId,
            severity: incident.severity
          }
        });
      }
    });

    if (notifications.length > 0) {
      await admin.messaging().sendAll(notifications);
      console.log(`Sent notifications to ${notifications.length} admins`);
    }
  } catch (error) {
    console.error('Error sending admin notifications:', error);
  }
}

/**
 * Notify when incident is resolved
 */
async function notifyIncidentResolved(incident, incidentId) {
  try {
    // Get the user who reported the incident
    const userDoc = await db.collection('users').doc(incident.userId).get();
    const user = userDoc.data();

    if (user && user.fcmToken) {
      await admin.messaging().send({
        token: user.fcmToken,
        notification: {
          title: 'Incident Resolved',
          body: `Your incident report has been resolved: ${incident.description.substring(0, 100)}...`
        },
        data: {
          type: 'incident_resolved',
          incidentId: incidentId
        }
      });
    }
  } catch (error) {
    console.error('Error sending resolution notification:', error);
  }
}

// ==================== HELPER FUNCTIONS ====================

/**
 * Log audit events for compliance and tracking
 */
async function logAuditEvent(action, userId, resourceId, metadata) {
  try {
    const auditLog = {
      action: action,
      userId: userId,
      resourceId: resourceId,
      metadata: metadata || {},
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      ipAddress: null, // Would need to be passed from client
      userAgent: null  // Would need to be passed from client
    };

    await db.collection('auditLogs').add(auditLog);
  } catch (error) {
    console.error('Error logging audit event:', error);
  }
}

/**
 * Validate time entry for business rules
 */
async function validateTimeEntry(timeEntry) {
  try {
    // Check for duplicate entries within 5 minutes
    const fiveMinutesAgo = new Date(timeEntry.timestamp.toDate().getTime() - 5 * 60 * 1000);
    
    const duplicateCheck = await db.collection('timeEntries')
      .where('userId', '==', timeEntry.userId)
      .where('type', '==', timeEntry.type)
      .where('timestamp', '>=', fiveMinutesAgo)
      .limit(1)
      .get();

    if (!duplicateCheck.empty) {
      console.log('Duplicate time entry detected');
      return false;
    }

    // Add more validation rules as needed
    return true;
  } catch (error) {
    console.error('Error validating time entry:', error);
    return false;
  }
}

/**
 * Calculate shift duration when user clocks out
 */
async function calculateShiftDuration(clockOutEntry) {
  try {
    // Find the corresponding clock in entry
    const clockInQuery = await db.collection('timeEntries')
      .where('userId', '==', clockOutEntry.userId)
      .where('type', '==', 'CLOCK_IN')
      .orderBy('timestamp', 'desc')
      .limit(1)
      .get();

    if (!clockInQuery.empty) {
      const clockInEntry = clockInQuery.docs[0].data();
      const duration = clockOutEntry.timestamp.toDate().getTime() - clockInEntry.timestamp.toDate().getTime();
      const durationHours = duration / (1000 * 60 * 60);

      // Update the clock out entry with shift duration
      await db.collection('timeEntries').doc(clockOutEntry.id).update({
        shiftDurationHours: durationHours,
        clockInEntryId: clockInQuery.docs[0].id
      });
    }
  } catch (error) {
    console.error('Error calculating shift duration:', error);
  }
}

/**
 * Update incident statistics for the estate
 */
async function updateIncidentStats(estateId, severity) {
  try {
    const statsRef = db.collection('estates').doc(estateId).collection('stats').doc('incidents');
    
    await db.runTransaction(async (transaction) => {
      const statsDoc = await transaction.get(statsRef);
      
      if (statsDoc.exists) {
        const stats = statsDoc.data();
        const currentCount = stats[severity.toLowerCase()] || 0;
        transaction.update(statsRef, {
          [severity.toLowerCase()]: currentCount + 1,
          total: (stats.total || 0) + 1,
          lastUpdated: admin.firestore.FieldValue.serverTimestamp()
        });
      } else {
        transaction.set(statsRef, {
          [severity.toLowerCase()]: 1,
          total: 1,
          lastUpdated: admin.firestore.FieldValue.serverTimestamp()
        });
      }
    });
  } catch (error) {
    console.error('Error updating incident stats:', error);
  }
}
