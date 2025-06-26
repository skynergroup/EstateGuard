# 🔥 Firebase Setup Guide for EstateGuard

## 📋 **Prerequisites**
- Firebase project created and connected (✅ Already done)
- Firebase CLI installed: `npm install -g firebase-tools`
- Node.js 18+ installed

## 🚀 **Step-by-Step Setup**

### **1. Firebase Authentication Setup**

#### **Enable Authentication Methods:**
1. Go to Firebase Console → Authentication → Sign-in method
2. Enable **Email/Password** authentication
3. Enable **Google Sign-In** (optional)
4. Configure authorized domains if needed

#### **Create Admin User:**
```bash
# In Firebase Console → Authentication → Users
# Click "Add user" and create:
Email: admin@estateguard.com
Password: [Generate secure password]
```

### **2. Firestore Database Setup**

#### **Deploy Security Rules:**
```bash
firebase deploy --only firestore:rules
```

#### **Deploy Indexes:**
```bash
firebase deploy --only firestore:indexes
```

#### **Initialize Database Structure:**
```bash
# Install dependencies for initialization script
cd firebase
npm install firebase-admin

# Download service account key from Firebase Console:
# Project Settings → Service Accounts → Generate new private key
# Save as: firebase/service-account-key.json

# Run initialization script
node firestore-init.js
```

### **3. Firebase Storage Setup**

#### **Deploy Storage Rules:**
```bash
firebase deploy --only storage
```

#### **Create Storage Buckets Structure:**
The following folders will be created automatically:
- `/profile_photos/{userId}/`
- `/incident_photos/{incidentId}/`
- `/qr_codes/{estateId}/`
- `/reports/{reportId}/`
- `/backups/{backupId}/`
- `/temp/{userId}/`

### **4. Cloud Functions Setup**

#### **Deploy Functions:**
```bash
cd firebase/functions
npm install
cd ../..
firebase deploy --only functions
```

#### **Functions Deployed:**
- `createUserProfile` - Auto-creates user profile on registration
- `deleteUserProfile` - Soft deletes user on account deletion
- `processNewIncident` - Handles new incident notifications
- `processIncidentUpdate` - Tracks incident status changes
- `processTimeEntry` - Validates and processes time entries

### **5. Firebase Configuration Verification**

#### **Test Firebase Connection:**
```bash
# Start Firebase emulators for testing
firebase emulators:start

# Access emulator UI at: http://localhost:4000
```

#### **Verify App Configuration:**
1. Check `app/google-services.json` is present ✅
2. Verify Firebase dependencies in `app/build.gradle.kts` ✅
3. Test authentication flow
4. Test Firestore read/write operations
5. Test Storage upload/download

## 🔧 **Firebase Console Configuration**

### **Authentication Settings:**
- **Password Policy**: Minimum 8 characters
- **Email Verification**: Enabled (recommended)
- **Account Recovery**: Email-based recovery enabled

### **Firestore Database Settings:**
- **Location**: Choose closest to your users (e.g., `europe-west1`)
- **Security Rules**: Deployed from `firebase/firestore.rules`
- **Indexes**: Deployed from `firebase/firestore.indexes.json`

### **Storage Settings:**
- **Location**: Same as Firestore
- **Security Rules**: Deployed from `firebase/storage.rules`
- **CORS Configuration**: Allow your domain

### **Cloud Functions Settings:**
- **Runtime**: Node.js 18
- **Memory**: 256MB (default)
- **Timeout**: 60 seconds
- **Environment Variables**: Set if needed

## 📊 **Database Collections Structure**

### **Users Collection** (`/users/{userId}`)
```javascript
{
  uid: string,
  email: string,
  firstName: string,
  lastName: string,
  role: 'admin' | 'guard',
  estateId: string,
  isActive: boolean,
  employeeId: string,
  permissions: string[],
  createdAt: timestamp,
  updatedAt: timestamp,
  lastLoginTime: timestamp,
  fcmToken: string
}
```

### **Time Entries Collection** (`/timeEntries/{entryId}`)
```javascript
{
  id: string,
  userId: string,
  estateId: string,
  type: 'CLOCK_IN' | 'CLOCK_OUT',
  timestamp: timestamp,
  location: string,
  latitude: number,
  longitude: number,
  qrCodeData: string,
  isManualEntry: boolean,
  createdAt: timestamp
}
```

### **Incidents Collection** (`/incidents/{incidentId}`)
```javascript
{
  id: string,
  userId: string,
  estateId: string,
  description: string,
  category: string,
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL',
  status: 'REPORTED' | 'IN_PROGRESS' | 'RESOLVED',
  timestamp: timestamp,
  location: string,
  latitude: number,
  longitude: number,
  photoUrls: string[],
  actionsTaken: string,
  followUpRequired: boolean,
  createdAt: timestamp,
  updatedAt: timestamp
}
```

### **Estates Collection** (`/estates/{estateId}`)
```javascript
{
  id: string,
  name: string,
  address: string,
  contactEmail: string,
  contactPhone: string,
  coordinates: { latitude: number, longitude: number },
  qrCodeData: string,
  isActive: boolean,
  totalUnits: number,
  securityPoints: array,
  createdAt: timestamp,
  updatedAt: timestamp
}
```

## 🔐 **Security Configuration**

### **Firestore Security Rules Features:**
- ✅ **Role-based access control** (admin, guard)
- ✅ **User can only access own data** (unless admin)
- ✅ **Audit logging** for all operations
- ✅ **Field-level security** for sensitive data
- ✅ **No delete operations** (soft delete only)

### **Storage Security Rules Features:**
- ✅ **File type validation** (images only for photos)
- ✅ **File size limits** (10MB max)
- ✅ **User-specific folders** for profile photos
- ✅ **Admin-only access** for reports and backups

## 📱 **Push Notifications Setup**

### **Firebase Cloud Messaging (FCM):**
1. Go to Project Settings → Cloud Messaging
2. Generate server key for backend notifications
3. Configure APNs certificates (if iOS support needed)
4. Test notifications through Firebase Console

### **Notification Types Configured:**
- 🚨 **High/Critical Incidents** → Notify all admins
- ✅ **Incident Resolution** → Notify incident reporter
- ⏰ **Shift Reminders** → Notify guards
- 📊 **Daily Reports** → Notify admins

## 🧪 **Testing Your Firebase Setup**

### **1. Authentication Test:**
```bash
# Test user creation and login
# Use Firebase Auth emulator or real Firebase
```

### **2. Firestore Test:**
```bash
# Test CRUD operations
# Verify security rules work correctly
```

### **3. Storage Test:**
```bash
# Test file upload/download
# Verify security rules and file validation
```

### **4. Functions Test:**
```bash
# Trigger functions and check logs
firebase functions:log
```

## 🎯 **Next Steps After Setup**

1. **Test all Firebase features** with your Android app
2. **Configure push notifications** in your app
3. **Set up monitoring and alerts** in Firebase Console
4. **Configure backup strategies** for Firestore data
5. **Set up analytics** for app usage tracking

## 🆘 **Troubleshooting**

### **Common Issues:**
- **Permission denied**: Check Firestore security rules
- **Index errors**: Deploy missing indexes
- **Function timeouts**: Increase timeout or optimize code
- **Storage upload fails**: Check file size and type restrictions

### **Useful Commands:**
```bash
# View Firebase logs
firebase functions:log

# Test security rules
firebase emulators:start --only firestore

# Deploy specific components
firebase deploy --only firestore:rules
firebase deploy --only functions
firebase deploy --only storage
```

## 🚀 **Quick Deployment**

Use the automated deployment script:

```bash
# Make script executable
chmod +x deploy-firebase.sh

# Run deployment
./deploy-firebase.sh
```

This script will:
- Deploy Firestore rules and indexes
- Deploy Storage rules
- Deploy Cloud Functions
- Initialize database with sample data
- Verify deployment

## ✅ **Setup Complete Checklist**

- [ ] Firebase Authentication enabled and configured
- [ ] Firestore database rules and indexes deployed
- [ ] Storage rules deployed and buckets configured
- [ ] Cloud Functions deployed and tested
- [ ] Database initialized with sample data
- [ ] Security rules tested and verified
- [ ] Push notifications configured
- [ ] Android app updated with Firebase components
- [ ] EstateGuardApplication class configured
- [ ] Firebase messaging service added
- [ ] App successfully connects to Firebase
- [ ] All CRUD operations working
- [ ] File upload/download working
- [ ] Push notifications working

## 📱 **Android App Firebase Integration**

### **Added Components:**
- ✅ **FirebaseManager.kt** - Centralized Firebase configuration
- ✅ **EstateGuardMessagingService.kt** - Push notification handling
- ✅ **EstateGuardApplication.kt** - App initialization
- ✅ **Enhanced FirebaseRepository.kt** - Complete Firebase operations
- ✅ **AndroidManifest.xml** - Firebase service configuration
- ✅ **Notification icons and channels** - Professional notification UI

### **Firebase Features Integrated:**
- 🔐 **Authentication** with role-based access
- 📊 **Firestore** with offline persistence
- 📁 **Storage** with security rules
- 📱 **Cloud Messaging** with custom notifications
- ⚡ **Cloud Functions** for backend automation
- 🔍 **Analytics** and audit logging

**🎉 Your Firebase backend is now ready for EstateGuard!**

## 🔥 **Complete Firebase Architecture**

```
EstateGuard Android App
    ↓
Firebase Authentication (Role-based)
    ↓
Firestore Database (Offline-first)
    ↓
Cloud Storage (Secure file storage)
    ↓
Cloud Functions (Backend automation)
    ↓
Cloud Messaging (Push notifications)
```

**Your EstateGuard app now has enterprise-grade Firebase backend! 🚀**
