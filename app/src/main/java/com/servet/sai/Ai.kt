package com.servet.sai

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object Ai {
    private fun msg(role: String, text: String) =
        JSONObject().put("role", role)
            .put("parts", JSONArray().put(JSONObject().put("text", text)))

    fun ask(key: String, model: String, sys: String,
            hist: List<Pair<String, String>>, text: String): String {
        if (key.isBlank()) return "Ayarlardan API anahtarını girmeniz gerekiyor."
        try {
            val contents = JSONArray()
            for ((u, m) in hist.takeLast(10)) {
                contents.put(msg("user", u))
                contents.put(msg("model", m))
            }
            contents.put(msg("user", text))
            val body = JSONObject()
                .put("system_instruction",
                    JSONObject().put("parts", JSONArray().put(JSONObject().put("text", sys))))
                .put("contents", contents)
            val u = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$key"
            val c = URL(u).openConnection() as HttpURLConnection
            c.requestMethod = "POST"
            c.setRequestProperty("Content-Type", "application/json")
            c.doOutput = true
            c.connectTimeout = 20000
            c.readTimeout = 60000
            c.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = c.responseCode
            val s = (if (code in 200..299) c.inputStream else c.errorStream)
                .bufferedReader().readText()
            if (code !in 200..299) return "Hata $code: " + s.take(200)
            return JSONObject(s).getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts")
                .getJSONObject(0).getString("text").trim()
        } catch (e: Exception) {
            return "Bağlantı hatası: " + e.message
        }
    }
}
