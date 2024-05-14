package com.example.apoteknosadafarma


import HomeFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.apoteknosadafarma.databinding.ActivityLoginBinding
import com.example.apoteknosadafarma.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(HomeFragment())
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.menu_home -> replaceFragment(HomeFragment())
                R.id.menu_presensi -> replaceFragment(PresensiFragment())
                R.id.menu_absensi -> replaceFragment(AbsensiFragment())
                R.id.menu_gaji -> replaceFragment(GajiFragment())
                else -> {

                }
            }
            true
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }



}
