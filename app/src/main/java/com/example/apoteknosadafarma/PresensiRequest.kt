package com.example.apoteknosadafarma



data class PresensiRequest(
    val idKaryawan: Int,
    val tanggal: String, // Tambahkan tanggal
    val jamMasuk: String?, // Ganti nama atribut dari jamDatang menjadi jamMasuk
    val jamPulang: String?, // Tambahkan jamPulang
    val status: String,
    val file: String? // Tambahkan file
)






