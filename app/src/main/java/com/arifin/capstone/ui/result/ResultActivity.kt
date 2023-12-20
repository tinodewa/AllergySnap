package com.arifin.capstone.ui.result

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.arifin.capstone.R
import com.arifin.capstone.databinding.ActivityResultBinding
import com.arifin.capstone.ui.empty.EmptyActivity
import com.arifin.capstone.ui.homepage.MainActivity
import com.bumptech.glide.Glide
import com.ekn.gruzer.gaugelibrary.Range

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarResult)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setBoldTitle(getString(R.string.alergy_snap))

        val food = intent.getStringExtra("resultFood")
        val ingredients = intent.getStringExtra("resultIngredients")
        val potentialAllergen = intent.getStringExtra("resultPotentialAllergen")
        val description = intent.getStringExtra("resultDescription")

        binding.foodName.text = food
        binding.komposisi.text = ingredients
        binding.potensiAlergi.text = potentialAllergen
        binding.deskripsiAlergi.text = description

        val imageUri = intent.getStringExtra("resultImage")
        Glide.with(this)
            .load(imageUri)
            .into(binding.imageDisplay)

        binding.btnRescan.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }

        binding.res.setOnClickListener {
            val i = Intent(this, EmptyActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    private fun setBoldTitle(title: String) {
        val spannableString = SpannableString(title)
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            title.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.WHITE),
            0,
            title.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        supportActionBar?.title = spannableString
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
