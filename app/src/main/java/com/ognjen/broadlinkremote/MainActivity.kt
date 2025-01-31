package com.ognjen.broadlinkremote

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Link to activity_main.xml

        val buttons = listOf(
            findViewById<Button>(R.id.btnChannel1),
            findViewById<Button>(R.id.btnChannel2),
            findViewById<Button>(R.id.btnChannel3),
            findViewById<Button>(R.id.btnChannel4),
            findViewById<Button>(R.id.btnChannel5)
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                Toast.makeText(this, "Pressed: ${button.text}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
