package com.ognjen.broadlinkremote

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat

class PopupManager(
    private val context: Context,
    private val overlay: View,
    private val handleClick: (Context, String, BroadlinkManager) -> Unit,
    private val broadlinkManager: BroadlinkManager) {

    fun showArenaPopup(anchorView: View, isEditingMode: Boolean) {
        showPopup(anchorView, isEditingMode, R.layout.popup_arena, mapOf(
            R.id.arena1 to "Arena1",
            R.id.arena2 to "Arena2",
            R.id.arena3 to "Arena3",
            R.id.arena1premium to "Arena1Premium",
            R.id.arena2premium to "Arena2Premium",
            R.id.arena3premium to "Arena3Premium"
        ))
    }

    fun showSkPopup(anchorView: View, isEditingMode: Boolean) {
        showPopup(anchorView, isEditingMode, R.layout.popup_sk, mapOf(
            R.id.sportklub1 to "SK1",
            R.id.sportklub2 to "SK2",
            R.id.sportklub3 to "SK3"
        ))
    }

    private fun showPopup(anchorView: View, isEditingMode: Boolean, layoutRes: Int, channels: Map<Int, String>) {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(layoutRes, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            elevation = 10f
            setOnDismissListener { overlay.visibility = View.GONE }
        }

        overlay.visibility = View.VISIBLE

        channels.forEach { (viewId, signal) ->
            popupView.findViewById<ImageView>(viewId).apply {
                setOnClickListener {
                    handleClick(context, signal, broadlinkManager)
                    popupWindow.dismiss()
                }

                // Apply blue border in editing mode
                if (isEditingMode) {
                    foreground = context.getDrawable(R.drawable.border_blue_popups)
                }
            }
        }

        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0)

        // Auto-close the popup after 5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (popupWindow.isShowing) popupWindow.dismiss()
        }, 5000)
    }
}
