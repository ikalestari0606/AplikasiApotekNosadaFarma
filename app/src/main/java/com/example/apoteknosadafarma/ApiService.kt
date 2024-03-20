package com.example.apoteknosadafarma

import com.example.apoteknosadafarma.model.LoginRequest
import com.example.apoteknosadafarma.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("presensi")
    fun presensi(@Body request: PresensiRequest): Call<PresensiResponse>

    @POST("login") // Sesuaikan dengan endpoint login yang ditetapkan oleh backend Anda
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}
