package com.example.apoteknosadafarma

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject


    class PresensiFragment : Fragment() {

        private lateinit var listViewPresensi: ListView
        private lateinit var presensiAdapter: ArrayAdapter<String>
        private val BASE_URL = "http://192.168.0.101/nosadafarma/get_presensi.php"

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_presensi, container, false)

            listViewPresensi = view.findViewById(R.id.listViewPresensi)

            // Ambil ID karyawan dari Shared Preferences
            val idKaryawan = requireActivity().getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
                .getInt("id_karyawan", -1)

            // Buat URL dengan menambahkan ID karyawan ke BASE_URL
            val url = "$BASE_URL?id_karyawan=$idKaryawan"

            // Ambil data presensi dari server menggunakan Volley
            fetchPresensiFromServer(url)

            return view
        }

        private fun fetchPresensiFromServer(url: String) {
            // Buat request JSON menggunakan Volley
            val jsonArrayRequest = JsonArrayRequest(
                Request.Method.GET, url, null,
                { response ->
                    // Handle response from server here
                    response?.let {
                        // Iterate through the JSON array and extract presensi details
                        val presensiList = mutableListOf<String>()
                        for (i in 0 until it.length()) {
                            val presensiObject: JSONObject = it.getJSONObject(i)
                            val tanggal = presensiObject.getString("tanggal")
                            val jamMasuk = presensiObject.getString("jam_masuk")
                            val jamPulang = presensiObject.getString("jam_pulang")

                            // Format the information
                            val detailPresensi = "Presensi: $tanggal\n" +
                                    "Jam Masuk: $jamMasuk - Jam Pulang: $jamPulang"
                            presensiList.add(detailPresensi)
                        }

                        // Set up adapter for the ListView
                        presensiAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, presensiList)
                        listViewPresensi.adapter = presensiAdapter
                    }
                },
                { error ->
                    // Handle error response here
                    val errorMessage = "Error: ${error?.message}"
                    val presensiList = mutableListOf<String>()
                    presensiList.add(errorMessage)
                    // Set up adapter for the ListView
                    presensiAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, presensiList)
                    listViewPresensi.adapter = presensiAdapter
                }
            )

            // Add the request to the RequestQueue.
            Volley.newRequestQueue(requireContext()).add(jsonArrayRequest)
        }

        companion object {
            @JvmStatic
            fun newInstance() = PresensiFragment()
        }
    }


