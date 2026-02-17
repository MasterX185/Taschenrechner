package com.masterx185.Taschenrechner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }

        val viberSwitch = findViewById<MaterialSwitch>(R.id.switch_vibration)
        viberSwitch.isChecked = sharedPref.getBoolean("vibration_enabled", true)
        viberSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("vibration_enabled", isChecked).apply()
        }

        findViewById<Button>(R.id.btn_github_profile).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/masterx185")))
        }

        findViewById<Button>(R.id.btn_github_repo).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/masterx185/Taschenrechner")))
        }
    }
}