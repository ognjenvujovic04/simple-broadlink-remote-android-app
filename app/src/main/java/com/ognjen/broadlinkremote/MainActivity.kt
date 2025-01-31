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
            findViewById<ImageView>(R.id.btnChannel2),
            findViewById<ImageView>(R.id.btnChannel3),
            findViewById<ImageView>(R.id.btnChannel4),
            findViewById<Button>(R.id.btnChannel5)
        )

        views.forEach { view ->
            view.setOnClickListener {
                // Handle click for ImageView and Buttons
                when (view.id) {
                    R.id.btnChannel1 -> {
                        Toast.makeText(this, "Sportklub clicked!", Toast.LENGTH_SHORT).show()
                    }
                    R.id.btnChannel2 -> {
                        Toast.makeText(this, "Arenasport clicked!", Toast.LENGTH_SHORT).show()
                    }
                    R.id.btnChannel3 -> {
                        Toast.makeText(this, "Bn clicked!", Toast.LENGTH_SHORT).show()
                    }
                    R.id.btnChannel4 -> {
                        Toast.makeText(this, "Rts clicked!", Toast.LENGTH_SHORT).show()
                    }
                    R.id.btnChannel5 -> {
                        Toast.makeText(this, "Channel 5 clicked!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
