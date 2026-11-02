package com.example.erronka

import android.content.Context
import org.json.JSONArray

class JsonIrakurlea(private val context: Context) {

    fun irakurriEkitaldiak(): List<Ekitaldia> {
        val testua = context.assets.open("Ekitaldiak.json")
            .bufferedReader()
            .use { it.readText() }
        val json = JSONArray(testua)

        val zerrenda = mutableListOf<Ekitaldia>()

        for (i in 0 until json.length()) {
            val o = json.getJSONObject(i)

            var lekua = o.optString("eventWhere")
            if (lekua.isEmpty()) lekua = o.optString("placename")

            var irudia = o.optString("eventImageUrl")
            if (irudia.isNotEmpty()) irudia = "https://www.kulturklik.euskadi.eus" + irudia

            val ekitaldia = Ekitaldia(
                id = o.getJSONObject("_id").getString("\$oid"),
                izena = o.optString("documentName").replace("\"", "").trim(),
                mota = o.optString("eventType").replace("  ", " ").trim(),
                hasieraData = o.optJSONObject("eventStartDate")?.optString("\$date")?.take(10) ?: "",
                amaieraData = o.optJSONObject("eventEndDate")?.optString("\$date")?.take(10) ?: "",
                lurraldea = o.optString("eventTerritoryName"),
                herria = o.optString("eventTownName"),
                lekua = lekua,
                helbidea = o.optString("address"),
                ordua = o.optString("eventTimeTable"),
                prezioa = o.optString("eventPrice").replace("?", "€"),
                hizkuntzak = o.optString("eventLanguages"),
                irudia = irudia,
                esteka = o.optString("friendlyUrl")
            )
            zerrenda.add(ekitaldia)
        }

        return zerrenda
    }
}