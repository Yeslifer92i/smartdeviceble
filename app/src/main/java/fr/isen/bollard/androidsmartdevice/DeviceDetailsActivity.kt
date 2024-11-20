package fr.isen.bollard.androidsmartdevice

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager

class DeviceDetailsActivity : ComponentActivity() {

    private var selectedDevice: BluetoothDevice? = null
    private var connectionStatus: String = "Non connecté"
    private var errorMessage: String = ""

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                runOnUiThread {
                    if (ActivityCompat.checkSelfPermission(
                            this@DeviceDetailsActivity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                    }
                    connectionStatus = "Connecté à ${gatt?.device?.name}"
                    Toast.makeText(this@DeviceDetailsActivity, "Connecté avec succès", Toast.LENGTH_SHORT).show()
                    errorMessage = ""  // Réinitialiser le message d'erreur
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
            DeviceDetailsScreen(deviceName, deviceAddress, connectionStatus, errorMessage)
        }
    }

    @Composable
    fun DeviceDetailsScreen(deviceName: String, deviceAddress: String, connectionStatus: String, errorMessage: String) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Nom de l'appareil : $deviceName")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Adresse MAC : $deviceAddress")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "État de la connexion : $connectionStatus")

            // Affichage d'un message d'erreur si une erreur est survenue
            if (errorMessage.isNotEmpty()) {
                Text(text = "Erreur : $errorMessage", color = androidx.compose.ui.graphics.Color.Red)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton de connexion
            Button(onClick = {
                checkPermissionsAndConnect()  // Vérifier les permissions et lancer la connexion
            }) {
                Text(text = "Connexion")
            }

            Button(onClick = {
                finish()  // Ferme l'activité actuelle
            }) {
                Text(text = "Retour")
            }
        }
    }

    // Fonction pour vérifier les permissions et se connecter à l'appareil
    private fun checkPermissionsAndConnect() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1)
        } else {
            connectToDevice()
        }
    }

    // Fonction pour se connecter à l'appareil
    private fun connectToDevice() {
        selectedDevice?.let {
            try {
                connectionStatus = "Connexion à ${it.name}..."
                errorMessage = ""  // Réinitialiser le message d'erreur avant la connexion
                it.connectGatt(this, false, gattCallback)  // Connexion à l'appareil via GATT
            } catch (e: SecurityException) {
                Log.e("DeviceDetailsActivity", "Erreur de connexion : ${e.message}")
                connectionStatus = "Erreur de connexion"
                errorMessage = "Permissions manquantes pour se connecter."
                Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("DeviceDetailsActivity", "Erreur de connexion inconnue : ${e.message}")
                connectionStatus = "Erreur de connexion"
                errorMessage = "Erreur inconnue lors de la connexion."
                Toast.makeText(this, "Erreur de connexion inconnue", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
