package com.example.apoteknosadafarma

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

// PresensiAdapter.kt
class PresensiAdapter(context: Context, presensiList: List<Presensi>) : ArrayAdapter<Presensi>(context, 0, presensiList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.item_presensi, parent, false)
        }

        // Mendapatkan objek Presensi pada posisi tertentu
        val presensi = getItem(position)

        // Mengatur teks untuk TextView
        val tanggalTextView = listItemView!!.findViewById<TextView>(R.id.textViewTanggal)
        tanggalTextView.text = presensi?.tanggal

        val jamTextView = listItemView.findViewById<TextView>(R.id.textViewJam)
        jamTextView.text = "${presensi?.jam_masuk} - ${presensi?.jam_pulang}"

        return listItemView
    }
}


