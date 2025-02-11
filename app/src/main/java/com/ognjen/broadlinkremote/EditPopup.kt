package com.ognjen.broadlinkremote

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

class EditPopup(
    private val context: Context,
    private val broadlinkManager: BroadlinkManager
) {
    fun showEditPopup(channelId: String) {
        try {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_edit_button)

            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

            val title = dialog.findViewById<TextView>(R.id.title)
            val mappingsList = dialog.findViewById<ListView>(R.id.mappingsList)
            val btnAddNew = dialog.findViewById<Button>(R.id.btnAddNew)
            val btnSave = dialog.findViewById<Button>(R.id.btnSave)

            title.text = "Edit Channel $channelId Mappings"

            try {
                val currentCodes = broadlinkManager.getButtonMappings(channelId)
                val codesAdapter = IRCodesAdapter(context, currentCodes.toMutableList())
                mappingsList.adapter = codesAdapter

                btnAddNew.setOnClickListener {
                    try {
                        showCodeSelectionDialog(codesAdapter, channelId)
                    } catch (e: Exception) {
                        Log.e("BroadlinkError", "Error showing code selection dialog: ${e.message}", e)
                        Toast.makeText(context, "Failed to show code selection", Toast.LENGTH_SHORT).show()
                    }
                }

                btnSave.setOnClickListener {
                    try {
                        broadlinkManager.updateButtonMapping(channelId, codesAdapter.getCodes())
                        dialog.dismiss()
                    } catch (e: Exception) {
                        Log.e("BroadlinkError", "Error saving button mappings: ${e.message}", e)
                        Toast.makeText(context, "Failed to save changes", Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.show()
            } catch (e: Exception) {
                Log.e("BroadlinkError", "Error setting up edit popup: ${e.message}", e)
                Toast.makeText(context, "Failed to load button mappings", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        } catch (e: Exception) {
            Log.e("BroadlinkError", "Fatal error showing edit popup: ${e.message}", e)
            Toast.makeText(context, "Failed to open edit window", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCodeSelectionDialog(adapter: IRCodesAdapter, channelId: String) {
        try {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.popup_code_selection)

            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

            val codesList = dialog.findViewById<ListView>(R.id.codesList)

            fun refreshCodesList() {
                try {
                    val allCodes = broadlinkManager.getKnownRemoteButtons().toList()
                    val codesAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, allCodes)
                    codesList.adapter = codesAdapter
                } catch (e: Exception) {
                    Log.e("BroadlinkError", "Error refreshing codes list: ${e.message}", e)
                    Toast.makeText(context, "Failed to refresh codes list", Toast.LENGTH_SHORT).show()
                }
            }

            // Initial list population
            refreshCodesList()

            // Add learn new code button
            val btnLearn = Button(context)
            btnLearn.text = "Learn New Code"
            btnLearn.setOnClickListener {
                try {
                    showLearnNewCodeDialog { success ->
                        if (success) {
                            refreshCodesList() // Refresh the list after successful learning
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BroadlinkError", "Error showing learn code dialog: ${e.message}", e)
                    Toast.makeText(context, "Failed to start learning mode", Toast.LENGTH_SHORT).show()
                }
            }
            codesList.addFooterView(btnLearn)

            // Handle code selection
            codesList.setOnItemClickListener { _, _, position, _ ->
                try {
                    val allCodes = broadlinkManager.getKnownRemoteButtons().toList()
                    if (position < allCodes.size) {
                        adapter.addCode(allCodes[position])
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    Log.e("BroadlinkError", "Error selecting code: ${e.message}", e)
                    Toast.makeText(context, "Failed to select code", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e("BroadlinkError", "Fatal error showing code selection: ${e.message}", e)
            Toast.makeText(context, "Failed to open code selection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLearnNewCodeDialog(onLearnComplete: (Boolean) -> Unit) {
        try {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Learn New IR Code")

            val input = EditText(context)
            input.hint = "Enter code name"
            builder.setView(input)

            builder.setPositiveButton("Start Learning") { dialog, _ ->
                try {
                    val codeName = input.text.toString()
                    if (codeName.isNotEmpty()) {
                        if (broadlinkManager.enterLearningModeTest(codeName)) {
                            Toast.makeText(context, "Code learned successfully", Toast.LENGTH_SHORT).show()
                            onLearnComplete(true)
                        } else {
                            Toast.makeText(context, "Failed to learn code", Toast.LENGTH_SHORT).show()
                            onLearnComplete(false)
                        }
                    }
                    dialog.dismiss()
                } catch (e: Exception) {
                    Log.e("BroadlinkError", "Error during learning mode: ${e.message}", e)
                    Toast.makeText(context, "Error during learning mode", Toast.LENGTH_SHORT).show()
                    onLearnComplete(false)
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
                onLearnComplete(false)
            }

            builder.show()
        } catch (e: Exception) {
            Log.e("BroadlinkError", "Fatal error showing learn dialog: ${e.message}", e)
            Toast.makeText(context, "Failed to start learning mode", Toast.LENGTH_SHORT).show()
            onLearnComplete(false)
        }
    }

    private class IRCodesAdapter(
        private val context: Context,
        private val codes: MutableList<String>
    ) : BaseAdapter() {

        private val inflater = LayoutInflater.from(context)

        override fun getCount(): Int = codes.size

        override fun getItem(position: Int): String = codes[position]

        override fun getItemId(position: Int): Long = position.toLong()

        fun addCode(code: String) {
            try {
                codes.add(code)
                notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("BroadlinkError", "Error adding code to adapter: ${e.message}", e)
                Toast.makeText(context, "Failed to add code", Toast.LENGTH_SHORT).show()
            }
        }

        fun removeCode(position: Int) {
            try {
                codes.removeAt(position)
                notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("BroadlinkError", "Error removing code from adapter: ${e.message}", e)
                Toast.makeText(context, "Failed to remove code", Toast.LENGTH_SHORT).show()
            }
        }

        fun getCodes(): List<String> = codes.toList()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            try {
                val view = convertView ?: inflater.inflate(R.layout.item_ir_code, parent, false)

                val codeName = view.findViewById<TextView>(R.id.codeName)
                val btnRemove = view.findViewById<ImageButton>(R.id.btnRemove)

                codeName.text = getItem(position)

                btnRemove.setOnClickListener {
                    try {
                        removeCode(position)
                    } catch (e: Exception) {
                        Log.e("BroadlinkError", "Error handling remove button click: ${e.message}", e)
                        Toast.makeText(context, "Failed to remove code", Toast.LENGTH_SHORT).show()
                    }
                }

                return view
            } catch (e: Exception) {
                Log.e("BroadlinkError", "Error creating view in adapter: ${e.message}", e)
                // Return an empty view as fallback
                return View(context)
            }
        }
    }
}