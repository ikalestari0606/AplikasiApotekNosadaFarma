package com.example.apoteknosadafarma

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.apoteknosadafarma.R

class PresensiAdapter(context: Context, resource: Int, objects: List<PresensiModel>) :
    ArrayAdapter<PresensiModel>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_presensi, parent, false)

        val presensi = getItem(position)

        val namaKaryawanTextView: TextView = itemView.findViewById(R.id.namaKaryawanTextView)
        val jamMasukTextView: TextView = itemView.findViewById(R.id.jamMasukTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

        presensi?.let {

            jamMasukTextView.text = "Jam Masuk: ${it.jamMasuk}" // Menampilkan jam masuk
            statusTextView.text = "Status: ${it.status}" // Menampilkan status presensi
        }

        return itemView
    }
}
