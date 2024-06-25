package com.example.beaconScanner

import android.Manifest
import android.Manifest.permission.BLUETOOTH_ADMIN
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat


@Composable
fun BeaconScanPermissionsScreen() {
    val context = LocalContext.current
    val permissionsHelper = remember { PermissionsHelper(context) }
    val permissionGroups by remember { mutableStateOf(permissionsHelper.beaconScanPermissionGroupsNeeded()) }
    val continueButtonEnabled = remember { mutableStateOf(false) }
    val tag = "PermissionsScreen"

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            val permissionName = it.key
            val isGranted = it.value
            if (isGranted) {
                Log.d(tag, "$permissionName permission granted: $isGranted")
            } else {
                Log.d(tag, "$permissionName permission denied: $isGranted")
            }
        }
    }

    for (permissionGroup in permissionGroups) {
        PermissionButton(
            permissionGroup = permissionGroup,
            onClick = {
                promptForPermissions(requestPermissionLauncher, permissionGroup)
                if(allPermissionGroupsGranted(context, permissionGroups = permissionGroups)){
                    continueButtonEnabled.value = true
                }
            },
            modifier = Modifier.padding(bottom = 8.dp),
            enabled = !continueButtonEnabled.value
        )
    }
}

fun allPermissionGroupsGranted(context: Context, permissionGroups: List<Array<String>>): Boolean {
    val permissionsHelper = PermissionsHelper(context)
    for (permissionsGroup in permissionGroups) {
        if (!permissionsHelper.allPermissionsGranted(permissionsGroup)) {
            return false
        }
    }
    return true
}

@Composable
fun PermissionButton(
    permissionGroup: Array<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val permissionsHelper = PermissionsHelper(LocalContext.current)
    val allGranted = permissionsHelper.allPermissionsGranted(permissionGroup)

    Button(
        onClick = onClick,
        enabled = enabled && !allGranted,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = permissionGroup.get(permissionGroup.size-1),
        )
    }
}

fun promptForPermissions(
    requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
    permissionsGroup: Array<String>
) {
    requestPermissionLauncher.launch(permissionsGroup)
}



class PermissionsHelper(private val context: Context) {
    private fun isPermissionGranted(permissionString: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permissionString) == PackageManager.PERMISSION_GRANTED
    }

    fun setFirstTimeAskingPermission(permissionString: String, isFirstTime: Boolean) {
        val sharedPreference = context.getSharedPreferences("org.altbeacon.permissions", Context.MODE_PRIVATE)
        sharedPreference.edit().putBoolean(permissionString, isFirstTime).apply()
    }

    fun isFirstTimeAskingPermission(permissionString: String): Boolean {
        val sharedPreference = context.getSharedPreferences("org.altbeacon.permissions", Context.MODE_PRIVATE)
        return sharedPreference.getBoolean(permissionString, true)
    }

    fun beaconScanPermissionGroupsNeeded(backgroundAccessRequested: Boolean = false): List<Array<String>> {
        val permissions = mutableListOf<Array<String>>()

        permissions.add(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && backgroundAccessRequested) {
            permissions.add(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }

        return permissions
    }

    fun allPermissionsGranted(permissionsGroup: Array<String>): Boolean {
        for (permission in permissionsGroup) {
            if (!isPermissionGranted(permission)) {
                return false
            }
        }
        return true
    }
}