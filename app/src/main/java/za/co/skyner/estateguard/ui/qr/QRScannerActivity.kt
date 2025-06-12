package za.co.skyner.estateguard.ui.qr

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import pub.devrel.easypermissions.EasyPermissions
import za.co.skyner.estateguard.R
import za.co.skyner.estateguard.databinding.ActivityQrScannerBinding

class QRScannerActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    
    private lateinit var binding: ActivityQrScannerBinding
    private lateinit var barcodeView: DecoratedBarcodeView
    
    companion object {
        const val EXTRA_QR_RESULT = "qr_result"
        const val REQUEST_CAMERA_PERMISSION = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupQRScanner()
        checkCameraPermission()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Scan QR Code"
        }
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupQRScanner() {
        barcodeView = binding.barcodeScanner
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.let {
                    handleQRResult(it.text)
                }
            }
            
            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                // Optional: Handle possible result points for UI feedback
            }
        })
        
        // Customize scanner
        barcodeView.setStatusText("Place QR code within the frame to scan")
    }
    
    private fun checkCameraPermission() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            startScanning()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Camera permission is required to scan QR codes",
                REQUEST_CAMERA_PERMISSION,
                Manifest.permission.CAMERA
            )
        }
    }
    
    private fun startScanning() {
        barcodeView.resume()
    }
    
    private fun handleQRResult(qrData: String) {
        // Validate QR code format for EstateGuard
        if (isValidEstateGuardQR(qrData)) {
            val resultIntent = Intent().apply {
                putExtra(EXTRA_QR_RESULT, qrData)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        } else {
            Toast.makeText(this, "Invalid QR code. Please scan an EstateGuard QR code.", Toast.LENGTH_SHORT).show()
            // Continue scanning
            barcodeView.resume()
        }
    }
    
    private fun isValidEstateGuardQR(qrData: String): Boolean {
        // EstateGuard QR codes should follow format: "ESTATEGUARD:LOCATION:timestamp"
        // For demo purposes, accept any QR code that contains "ESTATEGUARD" or is a simple location code
        return qrData.contains("ESTATEGUARD", ignoreCase = true) || 
               qrData.matches(Regex("^[A-Z0-9_-]+$")) // Simple alphanumeric codes
    }
    
    override fun onResume() {
        super.onResume()
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            barcodeView.resume()
        }
    }
    
    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
    
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            startScanning()
        }
    }
    
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}
