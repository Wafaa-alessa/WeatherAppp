package com.example.weatherappp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var CITY = "10"
    private val API = "4c0780eba8d3a8649fe16a6de6a7795e"

    private lateinit var buttonError: Button

    private lateinit var Zip: RelativeLayout
    private lateinit var textZip: EditText
    private lateinit var buttonZip: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonError = findViewById(R.id.buttonError)
        buttonError.setOnClickListener {
            CITY = "10"
            requestAPI()
        }
        Zip = findViewById(R.id.Zip)
        textZip = findViewById(R.id.textZip)
        buttonZip = findViewById(R.id.buttonZip)
        buttonZip.setOnClickListener {
            CITY = textZip.text.toString();
            requestAPI()
            textZip.text.clear()
            // Hide Keyboard
            val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
            imm?.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
            Zip.isVisible = false
        }
        requestAPI()
    }

    private fun requestAPI(){
        println("CITY: $CITY")
        CoroutineScope(Dispatchers.IO).launch{
            updateStatus(-1)
            val data = async {
                fetchWeatherData()
            }.await()
            if(data.isNotEmpty()){
                updateWeatherData(data)
                updateStatus(0)
            }else{
                updateStatus(1)
            }
        }
    }
    private suspend fun updateWeatherData(result: String){
        withContext(Dispatchers.Main){
            val jsonObj = JSONObject(result)
            val main = jsonObj.getJSONObject("main")
            val sys = jsonObj.getJSONObject("sys")
            val wind = jsonObj.getJSONObject("wind")
            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

            val lastUpdate:Long = jsonObj.getLong("dt")
            val lastUpdateText = "Updated at: " + SimpleDateFormat(
                "dd/MM/yyyy hh:mm a",
                Locale.ENGLISH).format(Date(lastUpdate*1000))
            val currentTemperature = main.getString("temp")
            val temp = try{
                currentTemperature.substring(0, currentTemperature.indexOf(".")) + "°C"
            }catch(e: Exception){
                currentTemperature + "°C"
            }
            val minTemperature = main.getString("temp_min")
            val tempMin = "Low: " + minTemperature.substring(0, minTemperature.indexOf("."))+"°C"
            val maxTemperature = main.getString("temp_max")
            val tempMax = "High: " + maxTemperature.substring(0, maxTemperature.indexOf("."))+"°C"
            val pressure = main.getString("pressure")
            val humidity = main.getString("humidity")

            val sunrise:Long = sys.getLong("sunrise")
            val sunset:Long = sys.getLong("sunset")
            val windSpeed = wind.getString("speed")
            val weatherDescription = weather.getString("description")

            val address = jsonObj.getString("name")+", "+sys.getString("country")

            findViewById<TextView>(R.id.textAddress).text = address
            findViewById<TextView>(R.id.textAddress).setOnClickListener {
                Zip.isVisible = true
            }
            findViewById<TextView>(R.id.textUpdate).text =  lastUpdateText
            findViewById<TextView>(R.id.textStatus).text = weatherDescription.capitalize(Locale.getDefault())
            findViewById<TextView>(R.id.textTemperature).text = temp
            findViewById<TextView>(R.id.textLow).text = tempMin
            findViewById<TextView>(R.id.textHigh).text = tempMax
            findViewById<TextView>(R.id.textSunrise).text = SimpleDateFormat("hh:mm a",
                Locale.ENGLISH).format(Date(sunrise*1000))
            findViewById<TextView>(R.id.textSunset).text = SimpleDateFormat("hh:mm a",
                Locale.ENGLISH).format(Date(sunset*1000))
            findViewById<TextView>(R.id.tvWind).text = windSpeed
            findViewById<TextView>(R.id.textPressure).text = pressure
            findViewById<TextView>(R.id.textHumidity).text = humidity
            findViewById<LinearLayout>(R.id.textRefresh).setOnClickListener { requestAPI() }
        }
    }

    private fun fetchWeatherData(): String{
        var response = ""
        try {
            response = URL("https://api.openweathermap.org/data/2.5/weather?zip=$CITY&units=metric&appid=$API")
                .readText(Charsets.UTF_8)
        }catch (e: Exception){
            println("Error: $e")
        }
        return response
    }

    private suspend fun updateStatus(state: Int){
//        states: -1 = loading, 0 = loaded, 1 = error
        withContext(Dispatchers.Main){
            when{
                state < 0 -> {
                    findViewById<ProgressBar>(R.id.pbProgress).visibility = View.VISIBLE
                    findViewById<RelativeLayout>(R.id.rela).visibility = View.GONE
                    findViewById<LinearLayout>(R.id.error).visibility = View.GONE
                }
                state == 0 -> {
                    findViewById<ProgressBar>(R.id.pbProgress).visibility = View.GONE
                    findViewById<RelativeLayout>(R.id.rela).visibility = View.VISIBLE
                }
                state > 0 -> {
                    findViewById<ProgressBar>(R.id.pbProgress).visibility = View.GONE
                    findViewById<LinearLayout>(R.id.error).visibility = View.VISIBLE
                }
            }
        }
    }
}

