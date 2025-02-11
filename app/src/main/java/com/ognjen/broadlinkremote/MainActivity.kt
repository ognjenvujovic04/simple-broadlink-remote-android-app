package com.ognjen.broadlinkremote

import android.Manifest
import android.annotation.SuppressLint
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
    private lateinit var editPopup: EditPopup
    private lateinit var editControls: LinearLayout
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnOnOff: ImageButton
    private lateinit var btnRefresh: ImageButton
    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("ClickableViewAccessibility")
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

        // Initialize the top buttons
        btnOnOff = findViewById(R.id.btnOnOff)
        btnRefresh = findViewById(R.id.btnRefresh)

        // Initialize broadlinkManager
        // todo real address of the file
        broadlinkManager = BroadlinkManager(this)

        editPopup = EditPopup(this, broadlinkManager)


        btnSave.setOnClickListener {
            broadlinkManager.saveBtnIRCodes()
            exitEditMode()
        }

        btnCancel.setOnClickListener {
            broadlinkManager.loadBtnIRCodes()
            exitEditMode()
        }

        val overlay: View = findViewById(R.id.overlay)
        popupManager = PopupManager(this, overlay, ::handleClick, broadlinkManager)

        btnOnOff.setOnClickListener {
            handleClick(this, "On/Off", broadlinkManager)
        }

        // 5s holding functionality with icon change after 1.5s
        btnOnOff.setOnTouchListener(object : View.OnTouchListener {
            private val editModeDuration = 5000L // 5 seconds to enter edit mode
            private val iconChangeDuration = 1000L // 1 second to change icon
            private var isHeld = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isHeld = true
                        handler.postDelayed({
                            if (isHeld) {
                                btnOnOff.setImageResource(R.drawable.settings_icon)
                                btnOnOff.setBackgroundResource(R.drawable.settings_background)
                            }
                        }, iconChangeDuration)

                        handler.postDelayed({
                            if (isHeld) {
                                // Reset before entering edit mode
                                btnOnOff.setImageResource(R.drawable.power_icon)
                                btnOnOff.setBackgroundResource(R.drawable.power_background)
                                enterEditMode()
                                Toast.makeText(this@MainActivity, "Entered Edit Mode", Toast.LENGTH_SHORT).show()
                            }
                        }, editModeDuration)
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isHeld = false
                        handler.removeCallbacksAndMessages(null)
                        // Reset icon on release
                        btnOnOff.setImageResource(R.drawable.power_icon)
                        btnOnOff.setBackgroundResource(R.drawable.power_background)
                    }
                }
                return false
            }
        })

        btnRefresh.setOnClickListener {
            handleClick(this, "Refresh", broadlinkManager)
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
                    R.id.btnChannel3 -> handleClick(this, "Bn", broadlinkManager)
                    R.id.btnChannel4 -> handleClick(this, "Rts", broadlinkManager)
                    R.id.btnChannel5 -> {
                        editPopup.showEditPopup("btn-1")
                    }
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

    // Handles button clicks
    private fun handleClick(context: Context, buttonId: String, broadlinkManager: BroadlinkManager) {
        if (isEditingMode){
            // todo, placehodler for editing popup
            Toast.makeText(context, "Editing $buttonId", Toast.LENGTH_SHORT).show()
        } else {
            // todo, placeholder for broadlinkManager
            Toast.makeText(context, "$buttonId clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enterEditMode() {
        editControls.visibility = View.VISIBLE
        isEditingMode = true

        // Change all buttons' borders to blue
        val buttons = listOf(
            findViewById<Button>(R.id.btnChannel5),
            findViewById<ImageView>(R.id.btnChannel1),
            findViewById<ImageView>(R.id.btnChannel2),
            findViewById<ImageView>(R.id.btnChannel3),
            findViewById<ImageView>(R.id.btnChannel4)
        )

        buttons.forEach { it.foreground = ContextCompat.getDrawable(this, R.drawable.border_blue_main) }

        btnOnOff.foreground = ContextCompat.getDrawable(this, R.drawable.border_blue_circle)
        btnRefresh.foreground = ContextCompat.getDrawable(this, R.drawable.border_blue_circle)

}

    private fun exitEditMode() {
        editControls.visibility = View.GONE
        isEditingMode = false

        // Restore original white border
        val buttons = listOf(
            findViewById<Button>(R.id.btnChannel5),
            findViewById<ImageView>(R.id.btnChannel1),
            findViewById<ImageView>(R.id.btnChannel2),
            findViewById<ImageView>(R.id.btnChannel3),
            findViewById<ImageView>(R.id.btnChannel4)
        )

        buttons.forEach { it.foreground = ContextCompat.getDrawable(this, R.drawable.border_white_main) }

        btnOnOff.foreground = ContextCompat.getDrawable(this, R.drawable.border_white_circle)
        btnRefresh.foreground = ContextCompat.getDrawable(this, R.drawable.border_white_circle)
    }



}
