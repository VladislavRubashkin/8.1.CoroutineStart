package com.example.a81coroutinestart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.a81coroutinestart.databinding.ActivityMainBinding
import kotlin.concurrent.thread

/**
 * TODO#1
 *
 * Вся работа View происходит в главном потоке.
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.bStartDownload.setOnClickListener {
            loadData()
        }
    }

    private fun loadData() {
        binding.pbProgress.isVisible = true
        binding.bStartDownload.isEnabled = false
        val city = loadCity()
        binding.tvLocation.text = city
        val temperature = loadTemperature(city)
        binding.tvTemperature.text = temperature.toString()
        binding.pbProgress.isVisible = false
        binding.bStartDownload.isEnabled = true
    }

    /**
     * TODO#2
     *
     * Код, который выполняется последовательно - синхронный
     */
    private fun loadCity(): String {
        Thread.sleep(5000)
        return getString(R.string.city)
    }

    private fun loadTemperature(city: String): Int {
        Toast.makeText(this, getString(R.string.loading_temperature, city), Toast.LENGTH_SHORT).show()
        Thread.sleep(5000)
        return 17
    }
}