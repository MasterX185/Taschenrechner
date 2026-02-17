package com.masterx185.Taschenrechner

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.PathInterpolator
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.DynamicColors
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private lateinit var historyView: TextView
    private lateinit var buttonGrid: GridLayout
    private var ausdruck: String = ""
    private var istErgebnisDa: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(this.application)
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setContentView(R.layout.activity_main)

        display = findViewById(R.id.textViewDisplay)
        historyView = findViewById(R.id.textViewHistory)
        buttonGrid = findViewById(R.id.buttonGrid)

        setupButtons()
    }

    private fun applyExpressiveMorph(pressedView: View) {
        val btn = pressedView as? MaterialButton ?: return
        val originalRadius = 80
        val targetRadius = 20
        val springInterp = PathInterpolator(0.3f, 0f, 0.1f, 1.2f)

        ValueAnimator.ofInt(originalRadius, targetRadius, originalRadius).apply {
            duration = 450
            interpolator = springInterp
            addUpdateListener { btn.cornerRadius = it.animatedValue as Int }
            start()
        }

        for (i in 0 until buttonGrid.childCount) {
            val child = buttonGrid.getChildAt(i)
            if (child != btn) {
                val diffX = child.x - btn.x
                val diffY = child.y - btn.y
                val dist = sqrt(diffX.pow(2) + diffY.pow(2))
                if (dist < 400 && dist > 0) {
                    val factor = 20f
                    child.animate()
                        .translationX((diffX / dist) * factor)
                        .translationY((diffY / dist) * factor)
                        .setDuration(150)
                        .withEndAction {
                            child.animate().translationX(0f).translationY(0f).setDuration(300).setInterpolator(springInterp).start()
                        }.start()
                }
            }
        }
    }

    private fun setupButtons() {
        val buttons = mapOf(
            R.id.btn_0 to "0", R.id.btn_1 to "1", R.id.btn_2 to "2", R.id.btn_3 to "3",
            R.id.btn_4 to "4", R.id.btn_5 to "5", R.id.btn_6 to "6", R.id.btn_7 to "7",
            R.id.btn_8 to "8", R.id.btn_9 to "9", R.id.btn_comma to ".",
            R.id.btn_open_bracket to "(", R.id.btn_close_bracket to ")",
            R.id.button_plus to "+", R.id.button_minus to "-",
            R.id.btn_mult to "*", R.id.btn_div to "/",
            R.id.btn_pow to "^", R.id.btn_root to "sqrt("
        )

        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        buttons.forEach { (id, wert) ->
            findViewById<Button>(id).setOnClickListener {
                if (sharedPref.getBoolean("vibration_enabled", true)) {
                    it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                applyExpressiveMorph(it)
                addText(wert)
            }
        }

        findViewById<Button>(R.id.button_clear).setOnClickListener {
            applyExpressiveMorph(it); ausdruck = ""; display.text = "0"; historyView.text = ""
        }

        findViewById<Button>(R.id.button_gleich).setOnClickListener {
            applyExpressiveMorph(it); berechne()
        }

        findViewById<Button>(R.id.btn_settings_open).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun addText(t: String) {
        // Wenn man "sqrt" tippt und davor eine Zahl steht, füge ein "*" ein
        if (t == "sqrt(" && ausdruck.isNotEmpty() && ausdruck.last().isDigit()) {
            ausdruck += "*"
        }

        if (istErgebnisDa && t !in listOf("+", "-", "*", "/")) ausdruck = t else ausdruck += t
        istErgebnisDa = false
        display.text = ausdruck.replace("sqrt(", "√(").replace(".", ",")
    }

    private fun berechne() {
        try {
            val res = eval(ausdruck)
            historyView.text = "${ausdruck.replace("sqrt(", "√(")} ="
            display.text = if (res % 1 == 0.0) res.toLong().toString() else "%.4f".format(res)
            istErgebnisDa = true
        } catch (e: Exception) { display.text = "Fehler" }
    }

    private fun eval(str: String): Double {
        return object : Any() {
            var pos = -1; var ch = 0
            fun nextChar() { ch = if (++pos < str.length) str[pos].code else -1 }
            fun eat(c: Int): Boolean { while (ch == ' '.code) nextChar(); if (ch == c) { nextChar(); return true }; return false }
            fun parse(): Double { nextChar(); return parseExpression() }
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) { if (eat('+'.code)) x += parseTerm() else if (eat('-'.code)) x -= parseTerm() else return x }
            }
            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) { if (eat('*'.code)) x *= parseFactor() else if (eat('/'.code)) x /= parseFactor() else return x }
            }
            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()
                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else if (eat('s'.code)) { // sqrt
                    nextChar(); nextChar(); nextChar()
                    x = sqrt(parseFactor())
                } else throw RuntimeException("Unerwartetes Zeichen: " + ch.toChar())

                if (eat('^'.code)) x = x.pow(parseFactor())

                if ((ch >= '0'.code && ch <= '9'.code) || ch == '('.code || ch == 's'.code) {
                    x *= parseFactor()
                }

                return x
            }
        }.parse()
    }
}