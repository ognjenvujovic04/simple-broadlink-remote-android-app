package com.ognjen.broadlinkremote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mob41.blapi.RM2Device
import com.github.mob41.blapi.mac.Mac

import java.net.InetAddress


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request necessary permissions at runtime
        requestPermissions()

        // Enable multicast for BroadLink discovery
        enableMulticast()

        val views = listOf(
            findViewById<ImageView>(R.id.btnChannel1),
            findViewById<ImageView>(R.id.btnChannel2),
            findViewById<ImageView>(R.id.btnChannel3),
            findViewById<ImageView>(R.id.btnChannel4),
            findViewById<Button>(R.id.btnChannel5)
        )

        views.forEach { view ->
            view.setOnClickListener {
                when (view.id) {
                    R.id.btnChannel1 -> Toast.makeText(this, "Sportklub clicked!", Toast.LENGTH_SHORT).show()
                    R.id.btnChannel2 -> Toast.makeText(this, "Arenasport clicked!", Toast.LENGTH_SHORT).show()
                    R.id.btnChannel3 -> Toast.makeText(this, "Bn clicked!", Toast.LENGTH_SHORT).show()
                    R.id.btnChannel4 -> Toast.makeText(this, "Rts clicked!", Toast.LENGTH_SHORT).show()
                    R.id.btnChannel5 -> discoverBroadlinkDevices()
                }
            }
        }
    }

    // Enable Wi-Fi multicast (needed for BroadLink discovery)
    private fun enableMulticast() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val multicastLock = wifiManager.createMulticastLock("BroadLinkDiscovery")
        multicastLock.setReferenceCounted(true)
        multicastLock.acquire()
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )

        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), 1)
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied. Device discovery may not work.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Discover BroadLink devices on the network
    private fun discoverBroadlinkDevices() {
        val multicastLock = (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createMulticastLock("multicastLock")
        multicastLock.setReferenceCounted(true)
        multicastLock.acquire()

        Thread {
            try {
                Log.d("BroadLink", "Starting device discovery...")

                Log.d("BroadLink", "Starting device discovery...")

                val device = RM2Device("192.168.1.3", Mac("78:0f:77:17:ec:ee"));
                device.auth()

                val success: Boolean = device.enterLearning()
                Toast.makeText(this, "Enter Learning status: " + (if (success) "Success!" else "Failed!"), Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                Log.e("BroadLink", "Error discovering devices: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this, "Error discovering devices: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

}
