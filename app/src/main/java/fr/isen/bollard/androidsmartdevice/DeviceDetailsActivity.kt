package fr.isen.bollard.androidsmartdevice

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat

class DeviceDetailsActivity : ComponentActivity() {

    private var selectedDevice: BluetoothDevice? = null
    private var connectionStatus: String = "Non connecté"

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                runOnUiThread {
                    if (ActivityCompat.checkSelfPermission(
                            this@DeviceDetailsActivity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {}
                    connectionStatus = "Connecté à ${gatt?.device?.name}"
                    Toast.makeText(this@DeviceDetailsActivity, "Connecté avec succès", Toast.LENGTH_SHORT).show()
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                runOnUiThread {
                    connectionStatus = "Déconnecté"
                    Toast.makeText(this@DeviceDetailsActivity, "Déconnexion réussie", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Inconnu"
        val deviceAddress = intent.getStringExtra("DEVICE_ADDRESS") ?: "Inconnu"
        connectionStatus = intent.getStringExtra("CONNECTION_STATUS") ?: "Non connecté"
        selectedDevice = intent.getParcelableExtra("DEVICE")

        setContent {
            DeviceDetailsScreen(deviceName, deviceAddress, connectionStatus)
        }
    }

    @Composable
    fun DeviceDetailsScreen(deviceName: String, deviceAddress: String, connectionStatus: String) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card for device details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nom de l'appareil",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray
                    )
                    Text(
                        text = deviceName,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adresse MAC",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray
                    )
                    Text(
                        text = deviceAddress,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "État de la connexion",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray
                    )
                    Text(
                        text = connectionStatus,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Button(
                onClick = { checkPermissionsAndConnect() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text(text = "Connexion", fontSize = 16.sp, color = Color.White)
            }

            Button(
                onClick = { finish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(text = "Retour", fontSize = 16.sp, color = Color.White)
            }
        }
    }

    private fun checkPermissionsAndConnect() {
        // Simplified permission checking
        connectToDevice()
    }

    private fun connectToDevice() {
        selectedDevice?.let {
            try {
                connectionStatus = "Connexion à ${it.name}..."
                it.connectGatt(this, false, gattCallback)
            } catch (e: SecurityException) {
                Log.e("DeviceDetailsActivity", "Erreur de connexion : ${e.message}")
                Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
