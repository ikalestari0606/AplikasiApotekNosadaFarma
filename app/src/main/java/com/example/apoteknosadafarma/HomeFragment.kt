import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.apoteknosadafarma.Config.Companion.BASE_URL
import com.example.apoteknosadafarma.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var listViewPresensi: ListView
    private lateinit var buttonJamMasuk: Button
    private lateinit var buttonJamPulang: Button
    private lateinit var sharedPreferences: SharedPreferences

    private var jamMasuk: String? = null
    private var jamPulang: String? = null

    private val lokasiPresensi = LatLng(-7.9031681, 112.0357137) // Lokasi presensi yang ditetapkan
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Hide the ActionBar
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        // Mendapatkan nama pengguna dari SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
        val nama = sharedPreferences.getString("nama", "") // Ambil nilai "nama" dari SharedPreferences

        // Menampilkan nama pengguna di TextView welcomeText
        val welcomeText = view.findViewById<TextView>(R.id.welcomeText)
        welcomeText.text = "Selamat datang, $nama" // Menampilkan nama dalam pesan selamat datang

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        listViewPresensi = view.findViewById(R.id.listViewPresensi)
        buttonJamMasuk = view.findViewById(R.id.buttonJamMasuk)
        buttonJamPulang = view.findViewById(R.id.buttonJamPulang)


        buttonJamMasuk.setOnClickListener {
            // Mendapatkan waktu saat ini dan menyimpannya sebagai jam masuk
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            jamMasuk = currentTime
            buttonJamMasuk.text = "Jam Masuk: $currentTime"

            // Lakukan proses absensi masuk
            processAttendance(jamMasuk, null)
        }

        buttonJamPulang.setOnClickListener {
            // Mendapatkan waktu saat ini dan menyimpannya sebagai jam pulang
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            jamPulang = currentTime
            buttonJamPulang.text = "Jam Pulang: $currentTime"

            // Lakukan proses absensi pulang
            processAttendance(null, jamPulang)
        }

        // Periksa status absensi dari SharedPreferences
        val jamMasuk = sharedPreferences.getString("jam_masuk", null)
        val jamPulang = sharedPreferences.getString("jam_pulang", null)

        // Jika sudah absen masuk, nonaktifkan tombol absen masuk dan tampilkan jam masuk
        if (jamMasuk != null) {
            buttonJamMasuk.isEnabled = false
            buttonJamMasuk.text = "Jam Masuk: $jamMasuk"
        }

        // Jika sudah absen pulang, nonaktifkan tombol absen pulang dan tampilkan jam pulang
        if (jamPulang != null) {
            buttonJamPulang.isEnabled = false
            buttonJamPulang.text = "Jam Pulang: $jamPulang"
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Periksa izin lokasi
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }
        // Dapatkan lokasi saat ini
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // Jika lokasi saat ini tersedia, perbarui peta
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    updateMapLocation(currentLocation)

                    // Menambahkan lingkaran warna merah di lokasi Anda
                    mMap.addCircle(
                        CircleOptions()
                            .center(currentLocation) // Mengatur pusat lingkaran ke lokasi Anda
                            .radius(100.0) // Menentukan radius lingkaran (dalam meter)
                            .strokeWidth(0f) // Mengatur lebar garis tepi lingkaran (0f untuk tidak ada garis tepi)
                            .fillColor(ContextCompat.getColor(requireContext(), R.color.red_transparent)) // Mengatur warna pengisian lingkaran
                    )
                } else {
                    // Lokasi saat ini tidak tersedia
                    showToast("Gagal mendapatkan lokasi saat ini.")
                }
            }
            .addOnFailureListener { e ->
                // Gagal mendapatkan lokasi
                Log.e("Location", "Error getting location: ${e.message}")
                showToast("Gagal mendapatkan lokasi: ${e.message}")
            }
    }

    private fun updateMapLocation(location: LatLng) {
        // Memperbarui peta dan memusatkan kamera ke lokasi baru
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    // Fungsi showToast untuk menampilkan pesan Toast
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun processAttendance(jamMasuk: String?, jamPulang: String?) {
        // Mendapatkan ID karyawan dari SharedPreferences
        val idKaryawan = sharedPreferences.getInt("id_karyawan", -1) // Ambil nilai "id_karyawan" dari SharedPreferences
        // Mendapatkan tanggal saat ini
        val tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Menentukan URL sesuai dengan tindakan (absensi masuk atau absensi pulang)
        val url = if (jamMasuk != null) {
            "$BASE_URL/insert_presensi.php" // Jika jamMasuk tidak null, lakukan insert presensi
        } else {
            "$BASE_URL/update_presensi.php" // Jika jamMasuk null (jamPulang tidak null), lakukan update presensi
        }

        // Mengirim data presensi ke server menggunakan Volley
        val stringRequest = object : StringRequest(Request.Method.POST, url,
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    // Handle response from server here
                    if (response != null && response.isNotEmpty()) {
                        // Jika respons tidak kosong, pertimbangkan berhasil
                        showToast("Response: $response")
                        Log.d("Presensi", "Presensi berhasil disimpan: $response")

                        // Simpan status absensi ke SharedPreferences dan nonaktifkan tombol yang sesuai
                        val editor = sharedPreferences.edit()
                        if (jamMasuk != null) {
                            editor.putBoolean("isCheckedIn", true)
                            buttonJamMasuk.isEnabled = false // Nonaktifkan tombol setelah berhasil absen masuk
                        }
                        if (jamPulang != null) {
                            editor.putBoolean("isCheckedOut", true)
                            buttonJamPulang.isEnabled = false // Nonaktifkan tombol setelah berhasil absen pulang
                        }
                        editor.apply()

                    } else {
                        // Jika respons kosong, pertimbangkan gagal
                        showToast("Gagal menyimpan presensi.")
                        Log.d("Presensi", "Gagal menyimpan presensi.")
                    }
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    // Handle error response here
                    showToast("Error: ${error?.message}")
                    Log.e("Presensi", "Error: ${error?.message}", error)
                }
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_karyawan"] = idKaryawan.toString()
                params["tanggal"] = tanggal
                params["jam_masuk"] = jamMasuk ?: ""
                params["jam_pulang"] = jamPulang ?: ""
                return params
            }
        }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(requireContext()).add(stringRequest)
    }

}