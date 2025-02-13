package com.ognjen.broadlinkremote

import android.content.Context
import android.util.Base64
import android.util.Log
import com.github.mob41.blapi.RM2Device
import com.github.mob41.blapi.mac.Mac
import com.github.mob41.blapi.pkt.cmd.rm2.SendDataCmdPayload
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class BroadlinkManager(private val context: Context) {

    private var broadlinkDevice: RM2Device? = null

    // Stores all learned IR codes with their real remote button names
    private val allIrCodes = mutableMapOf<String, ByteArray>()

    // Maps app buttons to sequences of real remote button codes
    private val btnIrCodes = mutableMapOf<String, List<String>>()

    private val gson = Gson()
    private val allCodesFile = File(context.filesDir, "ir_codes/all_codes.json")
    private val btnCodesFile = File(context.filesDir, "ir_codes/btn_mappings.json")

    fun initialize(): Boolean {
        var ret = false
        Thread {
            try {
                val device = RM2Device("192.168.1.8", Mac("78:0f:77:17:ec:ee"))
                device.auth()

                broadlinkDevice = device
                loadAllIRCodes()
                loadBtnIRCodes()

                ret = true

            } catch (e: Exception) {
                Log.e("BroadlinkError", "Initialization error: ${e.message}", e)
                ret = false
            }
        }.start()

        return ret
    }

    fun enterLearningMode(remoteButtonName: String): Boolean {
        var ret = false
        Thread {
            try {
                broadlinkDevice?.enterLearning()
                val irCode = broadlinkDevice?.checkData()

                irCode?.let {
                    allIrCodes[remoteButtonName] = it
                    saveAllIRCodes()
                    ret = true
                } ?: false
            } catch (e: Exception) {
                Log.e("BroadlinkError", "Learning error: ${e.message}", e)
                ret = false
            }
        }.start()
        return ret
    }

    fun saveAllIRCodes() {
        try {
            val irCodesDir = File(context.filesDir, "ir_codes")
            if (!irCodesDir.exists()) {
                irCodesDir.mkdirs()
            }

            val allCodesFile = File(irCodesDir, "all_codes.json") // Ensure correct path
            val encoded = allIrCodes.mapValues {
                Base64.encodeToString(it.value, Base64.DEFAULT)
            }
            allCodesFile.writeText(gson.toJson(encoded))

        } catch (e: Exception) {
            Log.e("BroadlinkError", "Save all codes error: ${e.message}", e)
        }
    }

    fun saveBtnIRCodes() {
        try {
            val irCodesDir = File(context.filesDir, "ir_codes")
            if (!irCodesDir.exists()) {
                irCodesDir.mkdirs()
            }

            val btnCodesFile = File(irCodesDir, "btn_mappings.json") // Ensure correct path
            btnCodesFile.writeText(gson.toJson(btnIrCodes))

        } catch (e: Exception) {
            Log.e("BroadlinkError", "Save btn codes error: ${e.message}", e)
        }
    }


    private fun loadAllIRCodes() {
        try {
            if (allCodesFile.exists()) {
                val type = object : TypeToken<Map<String, String>>() {}.type
                val loaded = gson.fromJson<Map<String, String>>(allCodesFile.readText(), type)
                allIrCodes.clear()
                loaded.forEach { (name, code) ->
                    allIrCodes[name] = Base64.decode(code, Base64.DEFAULT)
                }
            }
        } catch (e: Exception) {
            Log.e("BroadlinkError", "Load all codes error: ${e.message}", e)
        }
    }

    fun loadBtnIRCodes() {
        try {
            if (btnCodesFile.exists()) {
                val type = object : TypeToken<Map<String, List<String>>>() {}.type
                btnIrCodes.clear()
                btnIrCodes.putAll(gson.fromJson(btnCodesFile.readText(), type))
            }
        } catch (e: Exception) {
            Log.e("BroadlinkError", "Load btn codes error: ${e.message}", e)
        }
    }

    fun updateButtonMapping(buttonId: String, sequence: List<String>) {
        btnIrCodes[buttonId] = sequence
        saveBtnIRCodes()
    }

    fun getButtonMappings(buttonId: String): List<String> {
        return btnIrCodes[buttonId] ?: emptyList()
    }

    fun getKnownRemoteButtons(): Set<String> {
        return allIrCodes.keys
    }

    fun sendIRCode(channelId: String): Boolean {
        return try {
            val sequence = btnIrCodes[channelId] ?: return false
            sequence.forEach { buttonName ->
                allIrCodes[buttonName]?.let { code ->
                    // Todo send code to broadlink device
                     broadlinkDevice?.sendCmdPkt(SendDataCmdPayload(code))
                    Log.d("BroadLink", "Sent code for $buttonName")
                }
            }
            true
        } catch (e: Exception) {
            Log.e("BroadLink", "Send error: ${e.message}", e)
            false
        }
    }

}