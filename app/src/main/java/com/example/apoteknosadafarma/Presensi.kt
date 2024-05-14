package com.example.apoteknosadafarma

data class Presensi(
    val id_presensi: Int,
    val id_karyawan: Int,
    val tanggal: String,
    val jam_masuk: String,
    val jam_pulang: String
)