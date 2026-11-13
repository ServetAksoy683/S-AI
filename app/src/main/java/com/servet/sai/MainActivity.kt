package com.servet.sai

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var sv: SView
    private lateinit var out: TextView
    private lateinit var input: EditText
    private var rec: SpeechRecognizer? = null
    private val hist = ArrayList<Pair<String, String>>()

    private fun p() = getSharedPreferences("s", Context.MODE_PRIVATE)
    private fun hitap() = p().getString("hitap", "Efendim").orEmpty().ifBlank { "Efendim" }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun box() = GradientDrawable().apply {
        setColor(Color.BLACK); setStroke(dp(2), Color.WHITE)
    }

    private fun btn(t: String) = Button(this).apply {
        text = t
        setTextColor(Color.WHITE)
        background = box()
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        tts = TextToSpeech(this, this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(dp(20), dp(36), dp(20), dp(16))
        }

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        top.addView(TextView(this).apply {
            text = "S-AI"
            setTextColor(Color.WHITE)
            textSize = 22f
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        top.addView(btn("AYARLAR").apply {
            setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }
        })
        root.addView(top)

        sv = SView(this)
        sv.setOnClickListener { listen() }
        root.addView(sv, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.3f))

        out = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 17f
            text = ""
        }
        val sc = ScrollView(this).apply { addView(out) }
        root.addView(sc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val row = LinearLayout(this).apply { setPadding(0, dp(10), 0, dp(10)) }
        input = EditText(this).apply {
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            hint = "Yazın"
            setSingleLine(true)
            background = box()
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        row.addView(input, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(btn("GÖNDER").apply {
            setOnClickListener {
                val t = input.text.toString().trim()
                if (t.isNotEmpty()) { input.setText(""); handle(t) }
            }
        })
        root.addView(row)

        root.addView(btn("KONUŞ").apply {
            setOnClickListener { listen() }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)))

        setContentView(root)

        val perms = arrayOf(
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS, Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR, Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val need = perms.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (need.isNotEmpty()) requestPermissions(need.toTypedArray(), 1)

        out.text = "Emrinizi bekliyorum, ${hitap()}."
    }

    override fun onInit(s: Int) {
        if (s == TextToSpeech.SUCCESS) tts.language = Locale("tr", "TR")
    }

    private fun say(t: String) {
        runOnUiThread { out.text = t }
        if (p().getBoolean("voice", true)) tts.speak(t, TextToSpeech.QUEUE_FLUSH, null, "s")
    }

    private fun listen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            out.text = "Mikrofon izni gerekli."
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 2)
            return
        }
        tts.stop()
        rec?.destroy()
        rec = SpeechRecognizer.createSpeechRecognizer(this)
        rec!!.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(pa: Bundle?) { sv.active(true); out.text = "Dinliyorum..." }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bf: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(e: Int) { sv.active(false); out.text = "Duyamadım, tekrar deneyin." }
            override fun onPartialResults(pr: Bundle?) {}
            override fun onEvent(t: Int, pa: Bundle?) {}
            override fun onResults(r: Bundle?) {
                sv.active(false)
                val t = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (t != null) handle(t)
            }
        })
        rec!!.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
        )
    }

    private fun handle(t: String) {
        out.text = "Siz: $t"
        val l = local(t)
        if (l != null) { say(l); return }
        val ad = p().getString("ad", "").orEmpty()
        val sys = "Sen S-AI adında, Türkçe konuşan sesli bir asistansın. " +
            (if (ad.isNotBlank()) "Kullanıcının adı $ad. " else "") +
            "Kullanıcıya her zaman '${hitap()}' diye hitap et. " +
            "Kısa, net ve doğal konuş. Emoji ve markdown kullanma."
        val key = p().getString("key", "").orEmpty()
        val model = p().getString("model", "gemini-flash-latest").orEmpty()
            .ifBlank { "gemini-flash-latest" }
        Thread {
            val r = Ai.ask(key, model, sys, hist, t)
            hist.add(Pair(t, r))
            say(r)
        }.start()
    }

    private fun local(t: String): String? {
        val s = t.lowercase(Locale("tr"))
        val h = hitap()
        try {
            if (s.contains("fener")) {
                val on = !s.contains("kapat")
                val cm = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                cm.setTorchMode(cm.cameraIdList[0], on)
                return if (on) "Fener açıldı, $h." else "Fener kapatıldı, $h."
            }
            val num = Regex("([0-9][0-9 ]{6,}) ara").find(s)?.groupValues?.get(1)?.replace(" ", "")
            if (num != null) {
                startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$num")))
                return "$num aranıyor, $h."
            }
            if (s.endsWith(" aç")) {
                val name = s.removeSuffix(" aç").trim()
                val pm = packageManager
                val apps = pm.queryIntentActivities(
                    Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
                val hit = apps.firstOrNull {
                    it.loadLabel(pm).toString().lowercase(Locale("tr")).contains(name)
                }
                if (hit != null) {
                    val li = pm.getLaunchIntentForPackage(hit.activityInfo.packageName)
                    if (li != null) {
                        startActivity(li)
                        return "$name açılıyor, $h."
                    }
                }
            }
        } catch (e: Exception) {
            return "İşlem yapılamadı, $h."
        }
        return null
    }

    override fun onDestroy() {
        rec?.destroy()
        tts.shutdown()
        super.onDestroy()
    }
}
