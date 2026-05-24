package gamalsolutions.gamalprojects.autosavecontactscrm

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import gamalsolutions.gamalprojects.autosavecontactscrm.managers.PermissionManager
import gamalsolutions.gamalprojects.autosavecontactscrm.presentation.AppNavigation
import gamalsolutions.gamalprojects.autosavecontactscrm.presentation.CRMViewModel
import gamalsolutions.gamalprojects.autosavecontactscrm.services.CRMForegroundService

class MainActivity : ComponentActivity() {

    private lateinit var crmViewModel: CRMViewModel

    // Permission launcher to request SMS, Phone, Call, and Contact permissions automatically on launch
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "All core auto-saving permissions granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Caution: Some permissions were denied. Certain interceptors may fail.", Toast.LENGTH_LONG).show()
        }
        if (::crmViewModel.isInitialized) {
            crmViewModel.refreshIntegrationStatuses()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            crmViewModel = viewModel()
            AppNavigation(viewModel = crmViewModel)
        }

        // Request permissions on first boot
        requestCorePermissions()
    }

    override fun onResume() {
        super.onResume()
        if (::crmViewModel.isInitialized) {
            crmViewModel.refreshIntegrationStatuses()
        }
    }

    private fun requestCorePermissions() {
        val permissionsToRequest = PermissionManager.REQUIRED_PERMISSIONS.filter { perm ->
            checkSelfPermission(perm) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
