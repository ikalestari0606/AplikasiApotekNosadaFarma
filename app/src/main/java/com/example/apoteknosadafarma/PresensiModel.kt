package com.example.apoteknosadafarma

import com.google.gson.annotations.SerializedName

data class PresensiModel(
    @SerializedName("id_presensi") val idPresensi: Int,
    @SerializedName("id_karyawan") val idKaryawan: Int,
    @SerializedName("jam_masuk") val jamMasuk: String,
    @SerializedName("jam_pulang") val jampulang: String,
    @SerializedName("status") val status: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timestamp") val timestamp: String
)
