package com.ognjen.broadlinkremote

import android.util.Base64
import android.util.Log
import com.github.mob41.blapi.RM2Device
import com.github.mob41.blapi.mac.Mac
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class BroadlinkManager(private val filePath: String) {

    private var broadlinkDevice: RM2Device? = null

    // Stores all learned IR codes with their real remote button names
    private val allIrCodes = mutableMapOf<String, ByteArray>()

    // Maps app buttons to sequences of real remote button codes
    private val btnIrCodes = mutableMapOf<String, List<String>>()

    private val gson = Gson()
    private val allCodesFile = File("$filePath/all_codes.json")
    private val btnCodesFile = File("$filePath/btn_mappings.json")

    fun initialize(): Boolean {
        return try {
            broadlinkDevice = RM2Device("192.168.1.3", Mac("78:0f:77:17:ec:ee"))
            val authSuccess = broadlinkDevice?.auth() ?: false
            loadAllIRCodes()
            loadBtnIRCodes()
            authSuccess
        } catch (e: Exception) {
            Log.e("BroadLink", "Initialization error: ${e.message}", e)
            false
        }
    }

    fun enterLearningMode(remoteButtonName: String): Boolean {
        return try {
            broadlinkDevice?.enterLearning()
            val irCode = broadlinkDevice?.checkData()

            irCode?.let {
                allIrCodes[remoteButtonName] = it
                saveAllIRCodes()
                true
            } ?: false
        } catch (e: Exception) {
            Log.e("BroadLink", "Learning error: ${e.message}", e)
            false
        }
    }

    private fun saveAllIRCodes() {
        try {
            val encoded = allIrCodes.mapValues {
                Base64.encodeToString(it.value, Base64.DEFAULT)
            }
            allCodesFile.writeText(gson.toJson(encoded))
        } catch (e: Exception) {
            Log.e("BroadLink", "Save all codes error: ${e.message}", e)
        }
    }

    private fun saveBtnIRCodes() {
        try {
            btnCodesFile.writeText(gson.toJson(btnIrCodes))
        } catch (e: Exception) {
            Log.e("BroadLink", "Save btn codes error: ${e.message}", e)
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
            Log.e("BroadLink", "Load all codes error: ${e.message}", e)
        }
    }

    private fun loadBtnIRCodes() {
        try {
            if (btnCodesFile.exists()) {
                val type = object : TypeToken<Map<String, List<String>>>() {}.type
                btnIrCodes.clear()
                btnIrCodes.putAll(gson.fromJson(btnCodesFile.readText(), type))
            }
        } catch (e: Exception) {
            Log.e("BroadLink", "Load btn codes error: ${e.message}", e)
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
                    // broadlinkDevice?.sendIRCode(code)
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