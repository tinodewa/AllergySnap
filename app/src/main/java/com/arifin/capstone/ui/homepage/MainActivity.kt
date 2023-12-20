package com.arifin.capstone.ui.homepage

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.arifin.capstone.R
import com.arifin.capstone.database.response.ResponsePredict
import com.arifin.capstone.database.retrofit.ApiConfig
import com.arifin.capstone.databinding.ActivityMainBinding
import com.arifin.capstone.databinding.CustomDialogBinding
import com.arifin.capstone.helper.createCustomTempFile
import com.arifin.capstone.helper.reduceFileImage
import com.arifin.capstone.helper.rotateFile
import com.arifin.capstone.helper.uriToFile
import com.arifin.capstone.ui.settings.SettingsActivity
import com.arifin.capstone.ui.login.LoginActivity
import com.arifin.capstone.ui.result.ResultActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    private var selectedFile: File? = null
    private lateinit var progressBar: ProgressBar

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Action Bar
        setBoldTitle(getString(R.string.alergy_snap))

        mAuth = Firebase.auth

        val information = binding.information
        information.setOnClickListener {
            showInformation()
        }

        val user = mAuth.currentUser

        if (user != null) {
            val userName = user.displayName
            binding.nameUser.text = "Hi, $userName"
            Log.d("MainActivity", "User is signed in with Google. Display Name: $userName")
        }

        val usernameFromIntent = intent.getStringExtra("username")

        if (!usernameFromIntent.isNullOrEmpty()) {
            updateUIForUsernameLogin(usernameFromIntent)
        }

        applyAnimation()

        binding.addSamplePhoto.setOnClickListener {
            showImageOptions()
        }
        binding.buttonCheck.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                startCheckAllergy()
            }
        }
        progressBar = binding.progressBar

        setupView()
    }

    private fun showImageOptions() {
        val dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.dialogOption1.setOnClickListener {
            startCamera()
            dialog.dismiss()
        }

        dialogBinding.dialogOption2.setOnClickListener {
            startGallery()
            dialog.dismiss()
        }

        dialogBinding.dialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Pilih Gambar")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@MainActivity)
                selectedFile = myFile
                binding.imageDisplay.setImageURI(uri)
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun startCamera() {
        val intent =Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@MainActivity,"com.arifin.capstone.fileprovider", it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private lateinit var currentPhotoPath: String

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)

            myFile.let { file ->
                rotateFile(file)
                selectedFile = myFile
                binding.imageDisplay.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    private suspend fun startCheckAllergy() {
        selectedFile?.let { file ->
            val reducedFile = reduceFileImage(file)
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "file",
                reducedFile.name,
                reducedFile.asRequestBody("image/jpeg".toMediaType())
            )
            try {
                runOnUiThread { progressBar.visibility = View.VISIBLE }
                val response: ResponsePredict = ApiConfig.getApiServices().postImagePredict(imageMultipart)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (response.status?.statusCode == 201) {
                        val description = response.data?.descriptions ?: ""
                        val image = response.data?.imagePath ?: ""
                        val food = response.data?.food ?: ""
                        val ingredients = response.data?.ingredients ?: ""
                        val potential_allergen = response.data?.potentialAllergen ?: ""
                        val range = response.data?.range.toString()

                        Toast.makeText(
                            this@MainActivity,
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d("UploadImage", "Gambar berhasil diunggah: ${response.message}")

                        val intent = Intent(this@MainActivity, ResultActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra("resultDescription", description)
                        intent.putExtra("resultImage", image)
                        intent.putExtra("resultRange", range)
                        intent.putExtra("resultFood", food)
                        intent.putExtra("resultIngredients", ingredients)
                        intent.putExtra("resultPotentialAllergen", potential_allergen)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Gagal mengunggah gambar: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("UploadImage", "Gagal mengunggah gambar: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@MainActivity,
                        "Terjadi kesalahan saat mengunggah gambar: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("UploadImage", "Terjadi kesalahan saat mengunggah gambar: ${e.message}")
                }
            }
        } ?: run {
            runOnUiThread {
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.empty_img), Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUIForUsernameLogin(username: String) {
        binding.nameUser.text = "Hi, $username"
    }

    private fun setBoldTitle(title: String) {
        val spannableString = SpannableString(title)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.WHITE), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        supportActionBar?.title = spannableString
    }

    private fun applyAnimation() {
        val continuousFadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_out)
        binding.information.startAnimation(continuousFadeAnimation)
    }

    private fun showInformation() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Information")
        dialogBuilder.setMessage(R.string.usage)
        dialogBuilder.setPositiveButton("Continue Scan") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val settings = Intent(this, SettingsActivity::class.java)
                startActivity(settings)
                true
            }

            R.id.action_logout -> {
                showLogoutConfirmationDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            signOutAndStartSignInActivity()
        }
        builder.setNegativeButton("No") { _, _ ->
            // Do nothing, just close the dialog
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun signOutAndStartSignInActivity() {
        mAuth.signOut()
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_homepage, menu)
        return true
    }
}