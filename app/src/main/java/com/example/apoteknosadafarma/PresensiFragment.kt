package com.example.apoteknosadafarma

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException

class PresensiFragment : Fragment() {

    private lateinit var listViewPresensi: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_presensi, container, false)

        // Mendapatkan referensi ke ListView
        listViewPresensi = view.findViewById(R.id.listViewPresensi)

        // Mendapatkan id_karyawan dari SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
        val idKaryawan = sharedPreferences.getInt("id_karyawan", -1)

        // Memanggil fungsi getPresensi dengan mengirimkan id_karyawan sebagai parameter
        getPresensi(idKaryawan)

        return view
    }

    private fun getPresensi(idKaryawan: Int) {
        // Membuat permintaan GET ke server PHP
        val url = "${Config.BASE_URL}/get_presensi.php?id_karyawan=$idKaryawan"
        val queue = Volley.newRequestQueue(requireContext())
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    // Decode data JSON dari server PHP
                    val gson = GsonBuilder().create()
                    val presensiList = gson.fromJson(response, Array<Presensi>::class.java).toList()
                    // Menampilkan data presensi di ListView
                    showPresensi(presensiList)
                } catch (e: JsonSyntaxException) {
                    Toast.makeText(requireContext(), "Terjadi kesalahan: $e", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Terjadi kesalahan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(stringRequest)
    }

    private fun showPresensi(presensiList: List<Presensi>) {
        // Membuat adapter kustom untuk ListView
        val adapter = PresensiAdapter(requireContext(), presensiList)
        // Menetapkan adapter ke ListView
        listViewPresensi.adapter = adapter
    }
}
