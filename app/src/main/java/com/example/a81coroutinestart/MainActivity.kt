package com.example.a81coroutinestart

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.a81coroutinestart.databinding.ActivityMainBinding
import kotlin.concurrent.thread

/**
 * TODO#1
 *
 * Вся работа View(ОТРИСОВКА UI и ВЗАИМОДЕЙСТВИЕ С ПОЛЬЗОВАТЕЛЕМ) происходит в главном потоке.
 *
 * Код, который выполняется последовательно - синхронный
 * Код, который выполняется параллельно - асинхронный
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    /**
     * TODO#4
     *
     * Может принимать не только объект типа Runnable, но и объект типа Message.
     * Runnable - методы post(Runnable) и postDelayed(Runnable, long delay)
     * Message - sendMessage(Message) и sendMessageDelayed(Message, long delay)
     *
     * Чтобы принять и обработать сообщение необходимо переопределить метод handleMessage(msg: Message)
     */
    @SuppressLint("HandlerLeak") // Просто чтобы не подсвечивалось варнингом
    private val handler = object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            print("HANDLE_MSG $msg")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.bStartDownload.setOnClickListener {
            loadData()
        }
        handler.sendMessage(Message.obtain(handler, 1, 2))
    }

    private fun loadData() {
        Log.d("MainActivity", "Load started $this")
        binding.pbProgress.isVisible = true
        binding.bStartDownload.isEnabled = false
        loadCity { city ->
            binding.tvLocation.text = city
            loadTemperature(city) { temperature ->
                binding.tvTemperature.text = temperature.toString()
                binding.pbProgress.isVisible = false
                binding.bStartDownload.isEnabled = true
                Log.d("MainActivity", "Load finished $this")
            }
        }
    }

    /**
     * TODO#2
     *
     * Стартуем загрузку(Thread.sleep(5000)), и после её завершения вызываем метод, который принимает
     * строку в качестве параметра(callBack.invoke(getString(R.string.city))).
     * Аналогично с методом loadTemperature() - в аргументах принимаем callback, вызываем его и передаём в
     * него значение.
     *
     * 1. Если выполнять долгие операции в главном потоке - приложение зависнет(ANR - application not responding).
     * 2. Долгие операции стоит выполнять в других потоках.
     * 3. Для того чтобы вернуть значение из другого потока - используем callback - методы, которые будут вызваны
     * после окончания загрузки.
     *
     * ПРОБЛЕМЫ callback:
     * CallBack HELL - много вложенных друг в друга callback - сложно читать и дебажить.
     * УТЕЧКА ПАМЯТИ - стартуем загрузку и переворачиваем экран, activity пересоздаётся и старая activity должна
     * быть уничтожена, но поскольку загрузка ещё не закончена, то на старую activity есть действующая ссылка и
     * соответственно она не может быть уничтожена.
     * У потоков НЕТ жизненного цикла - они будут работать до тех пор пока не завершат работу, из-за этого возможны
     * различные баги из-за утечки памяти.
     */
    private fun loadCity(callBack: (String) -> Unit) {
        thread {
            Thread.sleep(5000)
            Handler(Looper.getMainLooper()).post {
                callBack.invoke(getString(R.string.city))
            }
        }
    }

    /**
     * TODO#3
     *
     * Работать с view можно ТОЛЬКО ИЗ ГЛАВНОГО ПОТОКА.
     * Для того чтобы потоки могли передавать данные друг другу есть класс HANDLER.
     * Метод post у handler вызывается на том потоке, на котором создан HANDLER.
     *
     * LOOPER - класс очередь сообщений.
     * Handler() - с пустым конструктором - deprecated. Лучше явно указывать к какому Looper привязывать Handler. напр
     * Handler(Looper.getMainLooper()) - обработка сообщений в главном потоке
     * Handler(Looper.myLooper()) - обработка сообщений на потоке потоке вызова(Если создавать Handler() НЕ на главном
     * потоке, то предварительно у этого потока надо вызвать Looper.prepare())
     *
     * Так же вместо Handler(Looper.getMainLooper()) можно вызвать runOnUiThread{} - по сути одно и тоже.
     */
    private fun loadTemperature(city: String, callBack: (Int) -> Unit) {
        thread {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, getString(R.string.loading_temperature, city), Toast.LENGTH_SHORT).show()
            }
            Thread.sleep(5000)
            runOnUiThread {
                callBack.invoke(17)
            }
        }
    }
}