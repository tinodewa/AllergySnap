package com.arifin.capstone.ui.empty

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arifin.capstone.R
import com.arifin.capstone.databinding.ActivityEmptyBinding
import com.arifin.capstone.ui.homepage.MainActivity

class EmptyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmptyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmptyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRescan.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }
}