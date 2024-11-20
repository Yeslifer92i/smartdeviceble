package fr.isen.bollard.androidsmartdevice

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import fr.isen.bollard.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class MainActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private val enableBtLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Si l'utilisateur a activé Bluetooth, démarrer le ScanActivity
            if (result.resultCode == RESULT_OK) {
                startScanActivity()
            } else {
                Toast.makeText(this, "Bluetooth doit être activé pour continuer", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSmartDeviceTheme {
                MainScreen()
            }
        }
    }

    public fun checkBluetoothAndStartScan() {
        val bluetoothAdapter = bluetoothAdapter
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            // Si le Bluetooth est désactivé, demander à l'utilisateur de l'activer
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableBtIntent)
        } else {
            // Si le Bluetooth est déjà activé, démarrer le ScanActivity
            startScanActivity()
        }
    }

    private fun startScanActivity() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current  // Get the context

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        DisplayText()
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            // Vérifie si le Bluetooth est activé avant de lancer le ScanActivity
            (context as? MainActivity)?.checkBluetoothAndStartScan()
        }) {
            Text(text = "Commencer")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AndroidSmartDeviceTheme {
        MainScreen()
    }
}

@Composable
fun DisplayText() {
    Text(text = "我是约翰·塞纳，按“开始”进入蓝牙设备搜索")
}
