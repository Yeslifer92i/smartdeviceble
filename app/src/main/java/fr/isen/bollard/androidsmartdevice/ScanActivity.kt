package fr.isen.bollard.androidsmartdevice

import android.Manifest
import android.content.Intent
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ScanActivity : ComponentActivity() {

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter?.bluetoothLeScanner
    }

    private val devices = mutableStateListOf<ScanResult>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            result.device?.let { device ->
                if (!devices.any { it.device.address == device.address }) {
                    devices.add(result)
                }
            }
        }


        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanActivity", "Scan échoué avec le code d'erreur : $errorCode")
            Toast.makeText(this@ScanActivity, "Scan échoué", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScanScreen()

        }
    }

    @Composable
    fun ScanScreen() {
        var isScanning by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.therock),
                contentDescription = "THEROCK",
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.blelogo),
                contentDescription = "blelogo",
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            Button(onClick = {
                if (isScanning) {
                    stopScan()
                } else {
                    checkPermissionsAndStartScan()
                }
                isScanning = !isScanning
            }) {
                Text(text = if (isScanning) "Arrêter la recherche" else "Lancer la recherche")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(devices.filter { device -> if (ActivityCompat.checkSelfPermission(
                        this@ScanActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                }
                    device.device.name != null && device.device.name != "Inconnu" }) { device ->
                    val deviceName = device.device.name ?: "Inconnu"
                    val deviceAddress = device.device.address ?: "Inconnu"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                val intent = Intent(this@ScanActivity, DeviceDetailsActivity::class.java).apply {
                                    putExtra("DEVICE_NAME", deviceName)
                                    putExtra("DEVICE_ADDRESS", deviceAddress)
                                }
                                startActivity(intent)
                            },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = deviceName, modifier = Modifier.padding(bottom = 4.dp))
                                Text(text = deviceAddress)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissionsAndStartScan() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
        } else {
            startScan()
        }
    }

    private fun startScan() {
        try {
            val bluetoothAdapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                Toast.makeText(this, "Bluetooth non disponible ou désactivé", Toast.LENGTH_LONG).show()
                return
            }

            devices.clear()  // Effacer les appareils détectés avant de lancer un nouveau scan
            bluetoothLeScanner?.startScan(scanCallback)
            Toast.makeText(this, "Scan BLE démarré", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Log.e("ScanActivity", "Permissions manquantes : ${e.message}")
            Toast.makeText(this, "Permissions requises pour le scan BLE.", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopScan() {
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
            Toast.makeText(this, "Scan BLE arrêté", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Log.e("ScanActivity", "Permissions manquantes pour arrêter le scan : ${e.message}")
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                startScan()
            } else {
                Toast.makeText(this, "Permissions manquantes. Scan non démarré.", Toast.LENGTH_LONG).show()
            }
        }
}
