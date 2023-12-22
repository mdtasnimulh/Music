package com.tasnim.chowdhury.music.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.ActivityAppSplashBinding
import com.tasnim.chowdhury.music.ui.MainActivity

@SuppressLint("CustomSplashScreen")
class AppSplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@AppSplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 1500)

        binding.splashMotiveTV.animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.splashOwnerName.animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
    }
}