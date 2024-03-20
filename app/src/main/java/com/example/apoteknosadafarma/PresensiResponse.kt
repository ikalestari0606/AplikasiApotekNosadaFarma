package com.example.apoteknosadafarma

import com.google.gson.annotations.SerializedName

data class PresensiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("id_presensi")
    val idPresensi: Int?,


)

