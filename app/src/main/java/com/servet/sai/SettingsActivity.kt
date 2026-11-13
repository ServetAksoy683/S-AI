package com.servet.sai

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*

class SettingsActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        val p = getSharedPreferences("s", MODE_PRIVATE)
        val d = resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()
        fun box() = GradientDrawable().apply {
            setColor(Color.BLACK); setStroke(dp(2), Color.WHITE)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(dp(20), dp(36), dp(20), dp(24))
        }
        val sc = ScrollView(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(root)
        }

        root.addView(TextView(this).apply {
            text = "AYARLAR"
            setTextColor(Color.WHITE)
            textSize = 28f
            setPadding(0, 0, 0, dp(16))
        })

        fun field(label: String, key: String, def: String): EditText {
            root.addView(TextView(this).apply {
                text = label
                setTextColor(Color.LTGRAY)
                textSize = 13f
                setPadding(0, dp(14), 0, dp(4))
            })
            val e = EditText(this).apply {
                setText(p.getString(key, def))
                setTextColor(Color.WHITE)
                setSingleLine(true)
                background = box()
                setPadding(dp(12), dp(10), dp(12), dp(10))
            }
            root.addView(e)
            return e
        }

        val ad = field("Adınız", "ad", "")
        val hitap = field("Size nasıl hitap etsin", "hitap", "Efendim")
        val key = field("API anahtarı (Gemini)", "key", "")
        val model = field("Model", "model", "gemini-flash-latest")

        val voice = CheckBox(this).apply {
            text = "Sesli cevap ver"
            setTextColor(Color.WHITE)
            isChecked = p.getBoolean("voice", true)
            setPadding(0, dp(16), 0, dp(8))
        }
        root.addView(voice)

        root.addView(Button(this).apply {
            text = "KAYDET"
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply { topMargin = dp(20) }
            setOnClickListener {
                p.edit()
                    .putString("ad", ad.text.toString().trim())
                    .putString("hitap", hitap.text.toString().trim())
                    .putString("key", key.text.toString().trim())
                    .putString("model", model.text.toString().trim())
                    .putBoolean("voice", voice.isChecked)
                    .apply()
                finish()
            }
        })
        setContentView(sc)
    }
}
