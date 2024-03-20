package com.example.apoteknosadafarma

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class DatabaseHelper(context: Context) {
    private val queue = Volley.newRequestQueue(context)
    private val url = "http://192.168.0.108/nosadafarma/presensi.php/"

    fun insertPresensi(presensi: PresensiRequest, callback: (Boolean) -> Unit) {
        val presensiData = JSONObject().apply {
            put("id_karyawan", presensi.idKaryawan)
            //put("nama", presensi.nama)
            put("tanggal", presensi.tanggal)
            put("jam_masuk", presensi.jamMasuk)
            put("jam_pulang", presensi.jamPulang)
            put("status", presensi.status)
            put("file", presensi.file) // File dapat berisi null jika tidak ada upload
        }

        val request = JsonObjectRequest(Request.Method.POST, url, presensiData,
            Response.Listener { response ->
                // Handle response jika perlu
                callback(true)
            },
            Response.ErrorListener { error ->
                // Handle error jika perlu
                callback(false)
            })

        queue.add(request)
    }
}
