package com.ognjen.broadlinkremote

import android.content.Context
import android.util.Log
import com.github.mob41.blapi.RM2Device
import com.github.mob41.blapi.mac.Mac
import com.google.gson.Gson
import java.io.File

class BroadlinkManager(private val context: Context) {

    private var broadlinkDevice: RM2Device? = null
    private val irCodes = mutableMapOf<String, ByteArray>()
    private val gson = Gson()
    private val irCodesFile = File(context.filesDir, "ir_codes.json")

    // Initialize the Broadlink device
    fun initialize(): Boolean {
        return try {
            broadlinkDevice = RM2Device("192.168.1.3", Mac("78:0f:77:17:ec:ee"));
            val authSuccess = broadlinkDevice?.auth() ?: false // Ensure auth() returns a Boolean
            loadIRCodes()
            authSuccess
        } catch (e: Exception) {
            Log.e("BroadLink", "Error discovering devices: ${e.message}", e)
            false
        }
    }

    // Enter learning mode
    fun enterLearningMode(channelId: String): Boolean {
        var retValue = true

        try {
            broadlinkDevice?.enterLearning()

            val irCode = broadlinkDevice?.checkData()
            if (irCode != null) {
                irCodes[channelId] = irCode
                saveIRCodes()
                retValue = true
            } else {
                retValue = false
            }
        } catch (e: Exception) {
            retValue = false
        }
        return retValue
    }

    // Save IR codes to a file
    private fun saveIRCodes() {
        try {
            val json = gson.toJson(irCodes)
            irCodesFile.writeText(json)
        } catch (e: Exception) {
            Log.e("BroadLink", "Error saving IR codes: ${e.message}", e)
        }
    }

    private fun loadIRCodes() {
        try {
            if (irCodesFile.exists()) {
                val json = irCodesFile.readText()
                val loadedCodes = gson.fromJson(json, Map::class.java) as Map<String, ByteArray>
                irCodes.clear()
                irCodes.putAll(loadedCodes)
            }
        } catch (e: Exception) {
            Log.e("BroadLink", "Error loading IR codes: ${e.message}", e)
        }
    }

    // Send an IR code for a specific channel todo
    fun sendIRCode(channelId: String): Boolean {
        return try {
            val irCode = irCodes[channelId]
            if (irCode != null) {
//                broadlinkDevice?.sendIRCode(irCode)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("BroadLink", "Error sending IR code: ${e.message}", e)
            false
        }
    }
}