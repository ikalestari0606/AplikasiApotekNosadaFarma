package com.example.apoteknosadafarma

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*




class PresensiActivity : AppCompatActivity(), OnMapReadyCallback {

    private fun saveJamMasukToSharedPreferences(jamMasuk: String) {
        val sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("jam_masuk", jamMasuk)
        editor.apply()
    }

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var listViewPresensi: ListView
    private lateinit var buttonJamMasuk: Button
    private lateinit var buttonJamPulang: Button
    private lateinit var presensiAdapter: ArrayAdapter<String>

    private var jamMasuk: String? = null
    private var jamPulang: String? = null

    private val lokasiPresensi = LatLng(-7.912783, 112.045538) // Lokasi presensi yang ditetapkan
    private val BASE_URL = "http://192.168.0.108/nosadafarma/presensi.php/"  // Ganti dengan URL backend API Anda
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presensi)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        listViewPresensi = findViewById(R.id.listViewPresensi)
        buttonJamMasuk = findViewById(R.id.buttonJamMasuk)
        buttonJamPulang = findViewById(R.id.buttonJamPulang)

        buttonJamMasuk.setOnClickListener {
            // Set jam masuk saat tombol Jam Masuk diklik
            val calendar = Calendar.getInstance()
            val jam = calendar.get(Calendar.HOUR_OF_DAY)
            val menit = calendar.get(Calendar.MINUTE)
            val detik = calendar.get(Calendar.SECOND)
            jamMasuk = String.format("%02d:%02d:%02d", jam, menit, detik)
            // Simpan nilai jam masuk ke dalam SharedPreferences
            saveJamMasukToSharedPreferences(jamMasuk!!)
            // Buat API request untuk menyimpan data presensi
            val idKaryawan = getIdKaryawanFromSharedPreferences()
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val status = "Masuk"
            val request = PresensiRequest(idKaryawan, timestamp, jamMasuk!!, null, status, null)
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                .build()
            val apiService = retrofit.create(ApiService::class.java)
            apiService.presensi(request).enqueue(object : Callback<PresensiResponse> {
                override fun onResponse(call: Call<PresensiResponse>, response: Response<PresensiResponse>) {
                    val responseBody = response.body().toString() // Konversi respons ke string
                    Log.d("Response", "Server Response: $responseBody")
                    if (response.isSuccessful) {
                        val presensiData = "Tanggal: $timestamp, Jam Masuk: $jamMasuk"
                        presensiAdapter.add(presensiData)
                        presensiAdapter.notifyDataSetChanged()
                        showToast("Berhasil Absen")
                    } else {
                        // Log response code and error body if any
                        Log.e("Response", "Response code: ${response.code()}")
                        Log.e("Response", "Error body: ${response.errorBody()?.string()}")
                        showToast("Gagal, Tidak dapat menyimpan data presensi")
                    }
                }

                override fun onFailure(call: Call<PresensiResponse>, t: Throwable) {
                    // Log failure message
                    Log.e("Response", "Failure: ${t.message}")
                    showToast("Gagal, Terjadi kesalahan: ${t.message}")
                }
            })
        }

        buttonJamPulang.setOnClickListener {
            // Set jam pulang saat tombol Jam Pulang diklik
            jamPulang = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            // Panggil fungsi presensi saat tombol Jam Pulang diklik
            presensi()
        }

        val presensiList = mutableListOf<String>()
        presensiAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, presensiList)
        listViewPresensi.adapter = presensiAdapter
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiPresensi, 15f))
    }

    private fun presensi() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                val currentLocation = LatLng(location.latitude, location.longitude)
                val isInLocation = isLocationInPresensiLocation(currentLocation)
                if (isInLocation) {
                    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val idKaryawan = getIdKaryawanFromSharedPreferences() // Ambil ID karyawan dari SharedPreferences
                    val status = if (jamMasuk != null) "Masuk" else "Pulang" //
                    val request = PresensiRequest(idKaryawan, timestamp, jamMasuk, jamPulang, status, null)
                    val retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    val apiService = retrofit.create(ApiService::class.java)
                    apiService.presensi(request).enqueue(object : Callback<PresensiResponse> {
                        override fun onResponse(call: Call<PresensiResponse>, response: Response<PresensiResponse>) {
                            val responseBody = response.body().toString() // Konversi respons ke string
                            Log.d("Response", "Server Response: $responseBody") // Tambahkan ini untuk mencetak respons dari server
                            if (response.isSuccessful) {
                                val presensiData = "Tanggal: $timestamp, Jam Masuk: $jamMasuk, Jam Pulang: $jamPulang"
                                presensiAdapter.add(presensiData)
                                presensiAdapter.notifyDataSetChanged()
                                showToast("Berhasil Absen")
                            } else {
                                // Log response code and error body if any
                                Log.e("Response", "Response code: ${response.code()}")
                                Log.e("Response", "Error body: ${response.errorBody()?.string()}")
                                showToast("Gagal, Tidak dapat menyimpan data presensi")
                            }
                        }

                        override fun onFailure(call: Call<PresensiResponse>, t: Throwable) {
                            // Log failure message
                            Log.e("Response", "Failure: ${t.message}")
                            showToast("Gagal, Terjadi kesalahan: ${t.message}")
                        }
                    })

                } else {
                    showToast("Gagal, Tidak Berada di Lokasi Kerja")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Location", "Error getting location: ${e.message}")
                showToast("Gagal, Terjadi kesalahan dalam mendapatkan lokasi")
            }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    private fun isLocationInPresensiLocation(location: LatLng): Boolean {
        val distanceThreshold = 1000 // Set the distance threshold in meters
        val result = FloatArray(1)
        android.location.Location.distanceBetween(
            location.latitude, location.longitude,
            lokasiPresensi.latitude, lokasiPresensi.longitude,
            result
        )
        return result[0] <= distanceThreshold
    }

    // Fungsi untuk mengambil ID karyawan dari SharedPreferences
    private fun getIdKaryawanFromSharedPreferences(): Int {
        val sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("id_karyawan", -1) // -1 digunakan sebagai nilai default jika tidak ada ID karyawan yang tersimpan
    }

}
