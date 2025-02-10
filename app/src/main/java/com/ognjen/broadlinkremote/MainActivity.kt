package com.ognjen.broadlinkremote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mob41.blapi.RM2Device
import com.github.mob41.blapi.mac.Mac


class MainActivity : AppCompatActivity() {
    private var isEditingMode = false
    private lateinit var popupManager: PopupManager
    private lateinit var broadlinkManager: BroadlinkManager
    private lateinit var editControls: LinearLayout
    private lateinit var txtEditMode: TextView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private var editModeRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Not sure if it is necessary
//        // Request necessary permissions at runtime
//        requestPermissions()
//
//        // Enable multicast for BroadLink discovery
//        enableMulticast()

        // Initialize edit controls
        editControls = findViewById(R.id.editControls)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        btnSave.setOnClickListener {
            broadlinkManager.saveIRCodes()
            exitEditMode()
        }

        btnCancel.setOnClickListener {
            broadlinkManager.loadIRCodes()  // Reload original codes
            exitEditMode()
        }

        val overlay: View = findViewById(R.id.overlay)
        popupManager = PopupManager(this, overlay, ::sendSignal)
        // todo real address of the file
        broadlinkManager = BroadlinkManager("ir_codes")

        // Initialize the top buttons
        val btnOnOff: ImageButton = findViewById(R.id.btnOnOff)
        val btnRefresh: ImageButton = findViewById(R.id.btnRefresh)

// Add regular click listener for power functionality
        btnOnOff.setOnClickListener {
                sendSignal("power")
            if (isEditingMode){
                exitEditMode()
                sendSignal("Exiting edit mode")
            } else {
                enterEditMode()
                sendSignal("Entering edit mode")
            }
        }

        btnRefresh.setOnClickListener {
            Toast.makeText(this, "Refresh clicked!", Toast.LENGTH_SHORT).show()
        }

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
                    R.id.btnChannel1 -> popupManager.showSkPopup(view)
                    R.id.btnChannel2 -> popupManager.showArenaPopup(view)
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

                val device = RM2Device("192.168.1.3", Mac("78:0f:77:17:ec:ee"))
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

    // todo placeholder for broadlinkManager
    private fun sendSignal(code: String) {
        // Use Broadlink API to send the signal
        Toast.makeText(this, code, Toast.LENGTH_SHORT).show()
    }

    private fun enterEditMode() {
        editControls.visibility = View.VISIBLE
        isEditingMode = true
        // todo
    }

    private fun exitEditMode() {
        editControls.visibility = View.GONE
        isEditingMode = false
        // todo
    }

}
