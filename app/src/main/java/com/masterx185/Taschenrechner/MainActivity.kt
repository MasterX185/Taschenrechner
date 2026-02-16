package com.masterx185.Taschenrechner

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private var anzeigeText: String = ""
    private var ersteZahl: Double = 0.0
    private var operation: String = ""
    private var istErgebnisDa: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.textViewDisplay)

        // Zahlen-Buttons ID-Liste
        val zahlen = mapOf(
            R.id.btn_0 to "0", R.id.btn_1 to "1", R.id.btn_2 to "2",
            R.id.btn_3 to "3", R.id.btn_4 to "4", R.id.button_fuenf to "5",
            R.id.btn_6 to "6", R.id.btn_7 to "7", R.id.btn_8 to "8", R.id.btn_9 to "9"
        )
        zahlen.forEach { (id, wert) -> findViewById<Button>(id).setOnClickListener { zahlGedrueckt(wert) } }

        // Operatoren
        findViewById<Button>(R.id.button_plus).setOnClickListener { opGedrueckt("+") }
        findViewById<Button>(R.id.button_minus).setOnClickListener { opGedrueckt("-") }
        findViewById<Button>(R.id.btn_mult).setOnClickListener { opGedrueckt("*") }
        findViewById<Button>(R.id.btn_div).setOnClickListener { opGedrueckt("/") }
        findViewById<Button>(R.id.btn_pow).setOnClickListener { opGedrueckt("^") }
        findViewById<Button>(R.id.btn_root_n).setOnClickListener { opGedrueckt("√") }

        findViewById<Button>(R.id.btn_pi).setOnClickListener {
            anzeigeText = PI.toString()
            display.text = anzeigeText
            istErgebnisDa = true
        }

        findViewById<Button>(R.id.btn_wurzel).setOnClickListener {
            if (anzeigeText.isNotEmpty()) {
                val ergebnis = sqrt(anzeigeText.toDouble())
                anzeigeText = ergebnis.toString()
                display.text = anzeigeText
                istErgebnisDa = true
            }
        }

        findViewById<Button>(R.id.button_gleich).setOnClickListener { berechne() }
        findViewById<Button>(R.id.button_clear).setOnClickListener { clear() }
    }

    private fun zahlGedrueckt(z: String) {
        if (istErgebnisDa) { anzeigeText = z; istErgebnisDa = false }
        else { anzeigeText += z }
        display.text = anzeigeText
    }

    private fun opGedrueckt(op: String) {
        if (anzeigeText.isNotEmpty() && !anzeigeText.contains(" ")) {
            ersteZahl = anzeigeText.toDouble()
            operation = op
            anzeigeText += " $op " // Erzeugt z.B. "5 √ "
            display.text = anzeigeText
        }
    }

    private fun berechne() {
        try {
            val teile = anzeigeText.split(" ")
            if (teile.size < 3) return

            val zweiteZahl = teile[2].toDouble()
            var ergebnis = 0.0

            when (operation) {
                "+" -> ergebnis = ersteZahl + zweiteZahl
                "-" -> ergebnis = ersteZahl - zweiteZahl
                "*" -> ergebnis = ersteZahl * zweiteZahl
                "/" -> ergebnis = ersteZahl / zweiteZahl
                "^" -> ergebnis = ersteZahl.pow(zweiteZahl)
                "√" -> ergebnis = zweiteZahl.pow(1.0 / ersteZahl) // n-te Wurzel
            }

            // Ergebnis runden auf 4 Stellen, damit es schöner aussieht
            anzeigeText = if (ergebnis % 1 == 0.0) ergebnis.toLong().toString() else "%.4f".format(ergebnis)
            display.text = anzeigeText
            istErgebnisDa = true
        } catch (e: Exception) {
            display.text = "Fehler"
            anzeigeText = ""
        }
    }

    private fun clear() {
        anzeigeText = ""; display.text = "0"; istErgebnisDa = false
    }
}