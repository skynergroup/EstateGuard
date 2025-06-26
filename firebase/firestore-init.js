// Firebase Firestore Database Initialization Script for EstateGuard
// Run this script to set up the initial database structure and sample data

const admin = require('firebase-admin');

// Initialize Firebase Admin SDK
const serviceAccount = require('./service-account-key.json'); // Download from Firebase Console

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  storageBucket: 'estateguard-demo.appspot.com' // Replace with your bucket
});

const db = admin.firestore();

async function initializeDatabase() {
  console.log('🚀 Initializing EstateGuard Firestore Database...');

  try {
    // 1. Create system settings
    await createSystemSettings();
    
    // 2. Create sample estate
    await createSampleEstate();
    
    // 3. Create admin user
    await createAdminUser();
    
    // 4. Create sample guard user
    await createSampleGuard();
    
    // 5. Create sample data
    await createSampleData();
    
    console.log('✅ Database initialization completed successfully!');
    
  } catch (error) {
    console.error('❌ Error initializing database:', error);
  }
}

async function createSystemSettings() {
  console.log('📋 Creating system settings...');
  
  const settings = {
    appVersion: '1.0.0',
    maintenanceMode: false,
    maxIncidentPhotos: 5,
    maxPhotoSizeMB: 10,
    clockInRadius: 100, // meters
    autoClockOutHours: 12,
    incidentCategories: [
      'Security Breach',
      'Suspicious Activity',
      'Property Damage',
      'Medical Emergency',
      'Fire/Safety',
      'Noise Complaint',
      'Unauthorized Access',
      'Equipment Malfunction',
      'Other'
    ],
    incidentSeverities: ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'],
    reportingEmail: 'admin@estateguard.com',
    supportEmail: 'support@estateguard.com',
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp()
  };
  
  await db.collection('settings').doc('app_config').set(settings);
  console.log('✅ System settings created');
}

async function createSampleEstate() {
  console.log('🏠 Creating sample estate...');
  
  const estate = {
    id: 'estate_001',
    name: 'Sunset Gardens Estate',
    address: '123 Estate Drive, Johannesburg, 2000',
    contactEmail: 'manager@sunsetgardens.co.za',
    contactPhone: '+27 11 123 4567',
    coordinates: {
      latitude: -26.2041,
      longitude: 28.0473
    },
    qrCodeData: 'ESTATE_001_SUNSET_GARDENS',
    isActive: true,
    totalUnits: 150,
    securityPoints: [
      {
        name: 'Main Gate',
        coordinates: { latitude: -26.2041, longitude: 28.0473 },
        qrCode: 'SG_MAIN_GATE'
      },
      {
        name: 'Back Gate',
        coordinates: { latitude: -26.2045, longitude: 28.0480 },
        qrCode: 'SG_BACK_GATE'
      },
      {
        name: 'Clubhouse',
        coordinates: { latitude: -26.2038, longitude: 28.0475 },
        qrCode: 'SG_CLUBHOUSE'
      }
    ],
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp()
  };
  
  await db.collection('estates').doc('estate_001').set(estate);
  console.log('✅ Sample estate created');
}

async function createAdminUser() {
  console.log('👤 Creating admin user...');
  
  const adminUser = {
    uid: 'admin_001',
    email: 'admin@estateguard.com',
    firstName: 'System',
    lastName: 'Administrator',
    role: 'admin',
    estateId: 'estate_001',
    isActive: true,
    permissions: [
      'user_management',
      'report_generation',
      'system_settings',
      'incident_management',
      'time_tracking',
      'data_export'
    ],
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    lastLoginTime: null,
    fcmToken: null
  };
  
  await db.collection('users').doc('admin_001').set(adminUser);
  console.log('✅ Admin user created');
}

async function createSampleGuard() {
  console.log('👮 Creating sample guard user...');
  
  const guardUser = {
    uid: 'guard_001',
    email: 'guard@estateguard.com',
    firstName: 'John',
    lastName: 'Security',
    role: 'guard',
    estateId: 'estate_001',
    isActive: true,
    employeeId: 'EG001',
    shiftPattern: 'day', // day, night, rotating
    permissions: [
      'clock_in_out',
      'incident_reporting',
      'view_own_data'
    ],
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    lastLoginTime: null,
    fcmToken: null
  };
  
  await db.collection('users').doc('guard_001').set(guardUser);
  console.log('✅ Sample guard user created');
}

async function createSampleData() {
  console.log('📊 Creating sample data...');
  
  // Sample time entry
  const timeEntry = {
    id: 'time_001',
    userId: 'guard_001',
    estateId: 'estate_001',
    type: 'CLOCK_IN',
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    location: 'Main Gate - Sunset Gardens Estate',
    latitude: -26.2041,
    longitude: 28.0473,
    qrCodeData: 'SG_MAIN_GATE',
    isManualEntry: false,
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  };
  
  await db.collection('timeEntries').doc('time_001').set(timeEntry);
  
  // Sample incident
  const incident = {
    id: 'incident_001',
    userId: 'guard_001',
    estateId: 'estate_001',
    description: 'Suspicious individual observed near the perimeter fence',
    category: 'Suspicious Activity',
    severity: 'MEDIUM',
    status: 'REPORTED',
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    location: 'Perimeter Fence - Section B',
    latitude: -26.2043,
    longitude: 28.0477,
    photoUrls: [],
    actionsTaken: 'Approached individual, requested identification, escorted off premises',
    followUpRequired: true,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp()
  };
  
  await db.collection('incidents').doc('incident_001').set(incident);
  
  console.log('✅ Sample data created');
}

// Run the initialization
initializeDatabase().then(() => {
  console.log('🎉 EstateGuard database setup complete!');
  process.exit(0);
}).catch((error) => {
  console.error('💥 Setup failed:', error);
  process.exit(1);
});
