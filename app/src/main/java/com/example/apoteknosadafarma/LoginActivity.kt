package com.example.apoteknosadafarma

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.apoteknosadafarma.databinding.ActivityLoginBinding
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var requestQueue: RequestQueue
    private val url = "http://192.168.0.108/nosadafarma/login.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        requestQueue = Volley.newRequestQueue(this)

        binding.btnLogin.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val username = binding.edtUsername.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()

        // Validasi input
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Username dan password harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                Log.d("Response", "Response JSON: $response") // Tambahkan logging di sini
                try {
                    val jsonObject = JSONObject(response)

                    // Check if the response contains the "id_karyawan" key
                    if (jsonObject.has("id_karyawan")) {
                        val idKaryawan = jsonObject.getInt("id_karyawan")
                        val nama = jsonObject.getString("nama")
                        // Simpan ID karyawan dan nama karyawan ke SharedPreferences
                        saveCredentialsToSharedPreferences(idKaryawan, nama)
                        // Jika login berhasil, arahkan ke halaman Presensi
                        val intent = Intent(this@LoginActivity, PresensiActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e("Response", "No value for id_karyawan")
                        Toast.makeText(this@LoginActivity, "Gagal, username atau password salah", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Log.e("Response", "Error parsing JSON: ${e.message}")
                    Toast.makeText(this@LoginActivity, "Gagal, terjadi kesalahan", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Log.e("Response", error.message ?: "Unknown error")
                Toast.makeText(this@LoginActivity, "Gagal, terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["password"] = password
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

    // Fungsi untuk menyimpan ID karyawan dan nama karyawan ke SharedPreferences
    private fun saveCredentialsToSharedPreferences(idKaryawan: Int, nama: String) {
        val sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("id_karyawan", idKaryawan)
        editor.putString("nama", nama)
        editor.apply()
    }
}
