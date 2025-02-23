package com.ognjen.broadlinkremote

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


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
        broadlinkManager.initialize()


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
            handleClick(this, "Power", broadlinkManager)
        }

        btnOnOff.setOnLongClickListener(View.OnLongClickListener {
            Toast.makeText(this, "Hold for 5 seconds to enter editing mode", Toast.LENGTH_SHORT).show()
            true
        })

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
            // todo change the Broadlink paramerter (mtel or total)
            handleClick(this, "Refresh", broadlinkManager)
        }

        val views = listOf(
            findViewById<ImageView>(R.id.btnChannel1),
            findViewById<ImageView>(R.id.btnChannel2),
            findViewById<ImageView>(R.id.btnChannel3),
            findViewById<ImageView>(R.id.btnChannel4),
//            findViewById<Button>(R.id.btnChannel5)
        )

        views.forEach { view ->
            view.setOnClickListener {
                when (view.id) {
                    R.id.btnChannel1 -> popupManager.showSkPopup(view, isEditingMode)
                    R.id.btnChannel2 -> popupManager.showArenaPopup(view, isEditingMode)
                    R.id.btnChannel3 -> handleClick(this, "Bn", broadlinkManager)
                    R.id.btnChannel4 -> handleClick(this, "Rts", broadlinkManager)
                }
            }
        }
    }



    // Handles button clicks
    private fun handleClick(context: Context, buttonId: String, broadlinkManager: BroadlinkManager) {
        if (isEditingMode){
            editPopup.showEditPopup(buttonId)
        } else {
            broadlinkManager.sendIRCode(buttonId)
            Toast.makeText(context, "$buttonId clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enterEditMode() {
        editControls.visibility = View.VISIBLE
        isEditingMode = true

        val buttonContainer = findViewById<LinearLayout>(R.id.buttonContainer)

        val buttons = listOf(
            findViewById<ImageView>(R.id.btnChannel1),
            findViewById<ImageView>(R.id.btnChannel2),
            findViewById<ImageView>(R.id.btnChannel3),
            findViewById<ImageView>(R.id.btnChannel4)
        )

        findViewById<Button>(R.id.btnTvBox1).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnTvBox2).visibility = View.VISIBLE

        val newHeight = resources.getDimensionPixelSize(R.dimen.edit_mode_button_height)
        val containerMarginBottom = resources.getDimensionPixelSize(R.dimen.edit_mode_button_margin_bottom)

        buttons.forEach {
            it.foreground = ContextCompat.getDrawable(this, R.drawable.border_blue_main)
            it.layoutParams.height = newHeight
            it.requestLayout()
        }

        val containerParams = buttonContainer.layoutParams as ViewGroup.MarginLayoutParams
        containerParams.bottomMargin = containerMarginBottom  // Add margin to container
        buttonContainer.layoutParams = containerParams
        buttonContainer.requestLayout()


        btnOnOff.foreground = ContextCompat.getDrawable(this, R.drawable.border_blue_circle)
        btnRefresh.foreground = ContextCompat.getDrawable(this, R.drawable.border_blue_circle)
    }


    private fun exitEditMode() {
        editControls.visibility = View.GONE
        isEditingMode = false

        val buttonContainer = findViewById<LinearLayout>(R.id.buttonContainer)

        val buttons = listOf(
            findViewById<ImageView>(R.id.btnChannel1),
            findViewById<ImageView>(R.id.btnChannel2),
            findViewById<ImageView>(R.id.btnChannel3),
            findViewById<ImageView>(R.id.btnChannel4)
        )

        findViewById<Button>(R.id.btnTvBox1).visibility = View.GONE
        findViewById<Button>(R.id.btnTvBox2).visibility = View.GONE

        val originalHeight = resources.getDimensionPixelSize(R.dimen.default_button_height)
        val noMargin = 0

        buttons.forEach {
            it.foreground = ContextCompat.getDrawable(this, R.drawable.border_white_main)
            it.layoutParams.height = originalHeight
            it.requestLayout()
        }

        val containerParams = buttonContainer.layoutParams as ViewGroup.MarginLayoutParams
        containerParams.bottomMargin = noMargin  // Remove container margin
        buttonContainer.layoutParams = containerParams
        buttonContainer.requestLayout()

        btnOnOff.foreground = ContextCompat.getDrawable(this, R.drawable.border_white_circle)
        btnRefresh.foreground = ContextCompat.getDrawable(this, R.drawable.border_white_circle)
    }




}
