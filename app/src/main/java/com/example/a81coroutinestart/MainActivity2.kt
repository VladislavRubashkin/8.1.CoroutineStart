package com.example.a81coroutinestart

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.a81coroutinestart.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class MainActivity2 : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    /**
     * TODO#5
     *
     * Если activity уничтожается - то все запросы должны отменяться.
     * lifecycleScope - эта область будет отменена, когда Lifecycle будет уничтожен. CoroutineScope привязан к
     * Lifecycle этого LifecycleOwner(этой activity).
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.bStartDownload.setOnClickListener {
            lifecycleScope.launch {
                loadData()
            }
//            loadDataWithOutCoroutine(0, null)
        }
    }

    /**
     * TODO#7
     *
     * suspend - функция вызывается только из корутины или другой suspend - функции
     */
    private suspend fun loadData() {
        Log.d("MainActivity", "Load started $this")
        binding.pbProgress.isVisible = true
        binding.bStartDownload.isEnabled = false
        val city = loadCity()
        binding.tvLocation.text = city
        val temperature = loadTemperature(city)
        binding.tvTemperature.text = temperature.toString()
        binding.pbProgress.isVisible = false
        binding.bStartDownload.isEnabled = true
        Log.d("MainActivity", "Load finished $this")
    }

    /**
     * TODO#6
     *
     * Необходимо что-бы данный метод стал прерываемым. Доходил до строчки Thread.sleep(5000), выходил из этого
     * метода, выполнял какую-то другую работу и через 5 сек возвращался в этот метод, и выполнял его уже со
     * строчки return "Moscow".
     *
     * Помечаем функцию ключевым словом suspend
     */
    private suspend fun loadCity(): String {
        delay(5000)
        return "Moscow"
    }


    private suspend fun loadTemperature(city: String): Int {
        Toast.makeText(this, getString(R.string.loading_temperature, city), Toast.LENGTH_SHORT).show()
        delay(5000)
        return 17
    }

    /**
     * TODO#8
     *
     * Аналог метода loadData(), НО БЕЗ КОРУТИН.
     * Корутины под капотом используют state-машину, это её очень упрощённый аналог. С помощью шагов мы можем
     * возвращаться к выполнению этой функции, но уже не с начала, а с определённого места. То есть один и тот же
     * метод можно вызывать с разными состояниями.
     *
     * В coroutine "под капотом" работают callback - здесь методы loadCityWithOutCoroutine() и loadTemperatureWithOutCoroutine()
     * также содержат callback.
     *
     * suspend-функции никогда не должны блокировать поток.
     */
    private fun loadDataWithOutCoroutine(step: Int = 0, obj: Any? = null) {
        when (step) {
            0 -> {
                binding.pbProgress.isVisible = true
                binding.bStartDownload.isEnabled = false
                loadCityWithOutCoroutine {
                    loadDataWithOutCoroutine(1, it)
                }
            }
            1 -> {
                val city = obj as String
                binding.tvLocation.text = city
                loadTemperatureWithOutCoroutine(city) {
                    loadDataWithOutCoroutine(2, it)
                }
            }
            2 -> {
                val temperature = obj as Int
                binding.tvTemperature.text = temperature.toString()
                binding.pbProgress.isVisible = false
                binding.bStartDownload.isEnabled = true
            }
        }
    }

    private fun loadCityWithOutCoroutine(callback: (String) -> Unit) {
        thread {
            Thread.sleep(5000)
            runOnUiThread {
                callback.invoke("Moscow")
            }
        }
    }

    private fun loadTemperatureWithOutCoroutine(city: String, callback: (Int) -> Unit) {
        thread {
            runOnUiThread {
                Toast.makeText(this, getString(R.string.loading_temperature, city), Toast.LENGTH_SHORT).show()
            }
            Thread.sleep(5000)
            runOnUiThread {
                callback.invoke(17)
            }
        }

    }
}
