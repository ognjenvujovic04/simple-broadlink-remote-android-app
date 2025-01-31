package com.ognjen.broadlinkremote

import android.os.Bundle
import android.widget.ImageView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Link to activity_main.xml

        val views = listOf(
            findViewById<ImageView>(R.id.btnChannel1),  // ImageView for the first button
            findViewById<Button>(R.id.btnChannel2),
            findViewById<Button>(R.id.btnChannel3),
            findViewById<Button>(R.id.btnChannel4),
            findViewById<Button>(R.id.btnChannel5)
        )

        views.forEach { view ->
            view.setOnClickListener {
                // Handle click for ImageView and Buttons
                when (view.id) {
                    R.id.btnChannel1 -> {
                        Toast.makeText(this, "Image clicked!", Toast.LENGTH_SHORT).show()
                    }
                    R.id.btnChannel2 -> {
                        Toast.makeText(this, "Channel 2 clicked!", Toast.LENGTH_SHORT).show()
                    }
                    R.id.btnChannel3 -> {
                        Toast.makeText(this, "Channel 3 clicked!", Toast.LENGTH_SHORT).show()
                    }
                    R.id.btnChannel4 -> {
                        Toast.makeText(this, "Channel 4 clicked!", Toast.LENGTH_SHORT).show()
                    }
                    R.id.btnChannel5 -> {
                        Toast.makeText(this, "Channel 5 clicked!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
