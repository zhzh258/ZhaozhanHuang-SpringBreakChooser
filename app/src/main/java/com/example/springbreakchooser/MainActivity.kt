package com.example.springbreakchooser

import android.content.ActivityNotFoundException
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.springbreakchooser.databinding.ActivityMainBinding
import kotlin.random.Random


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding;
    private lateinit var sensorManager: SensorManager
    private var lastSensorChangedTime: Long = 0;
    private var lastAccel: Array<Float> = arrayOf(0f, 0f, 0f)
    private val SHAKE_THRESHOLD = 800

    private val languages = arrayOf("Spanish", "French", "Chinese")
    private var selectedLanguageIndex = 0

    private fun promptSpeechInput(languageCode: String) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something in ${languages[selectedLanguageIndex]}")

        @Suppress("DEPRECATION")
        startActivityForResult(intent, SPEECH_REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            binding.editText.setText(result?.get(0) ?: "")
        }
    }

    companion object {
        private const val SPEECH_REQUEST_CODE = 111
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val diffTime = System.currentTimeMillis() - lastSensorChangedTime;
        if(diffTime > 100) {
            lastSensorChangedTime = System.currentTimeMillis();
            val x: Float = event!!.values[0];
            val y: Float = event.values[1];
            val z: Float = event.values[2];
            val xx = lastAccel[0];
            val yy = lastAccel[1];
            val zz = lastAccel[2];
            lastAccel = arrayOf(x, y, z);
            val speed: Float =
                Math.abs(x + y + z - xx - yy - zz) / diffTime * 10000
            if(speed > SHAKE_THRESHOLD){
                handleShowMap();
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        lastSensorChangedTime = System.currentTimeMillis();

        val languageSpinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, languages)
        languageSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinner.adapter = languageSpinnerAdapter
        binding.languageSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedLanguageIndex = position

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedLanguageIndex = 0;
            }
        }

        binding.button.setOnClickListener {
            handleShowMap()
        }

        binding.editText.setOnClickListener(View.OnClickListener {
            when(languages[selectedLanguageIndex]) {
                "Chinese" -> promptSpeechInput("zh-CN")
                "French" -> promptSpeechInput("fr-FR")
                "Spanish" -> promptSpeechInput("es-ES")
                else -> promptSpeechInput("en-US")
            }
        })
        binding.editText.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                when(languages[selectedLanguageIndex]) {
                    "Chinese" -> promptSpeechInput("zh-CN")
                    "French" -> promptSpeechInput("fr-FR")
                    "Spanish" -> promptSpeechInput("es-ES")
                    else -> promptSpeechInput("en-US")
                }
            }
        })
    }

    private fun getLocation(language: String): Pair<String, String> {
        return when(language) {
            "Spanish" -> if (Random.nextBoolean()) Pair("geo:19.4326,-99.1332", "Mexico City, Mexico - Best taco") // Mexico City, Mexico
                        else Pair("geo:-34.6037,-58.3816", "Buenos Aires, Argentina - Good airs") // Buenos Aires, Argentina
            "French" -> if (Random.nextBoolean()) Pair("geo:48.8566,2.3522", "Paris, France - See the Eiffel Tower")  // Paris, France
                        else Pair("geo:-20.8821,55.4504", "Saint-Denis, Réunion (French overseas region) - Tropical") // Saint-Denis, Réunion (French overseas region)
            "Chinese" -> if (Random.nextBoolean()) Pair("geo:39.9042,116.4074", "Beijing, China - Visit the Great Wall") // Beijing, China
                        else Pair("geo:31.2304,121.4737", "Shanghai, China - Modern city") // Shanghai, China
            else -> Pair("geo:0,0", "N/A")
        }
    }

    private fun handleShowMap(){
        val language = languages[selectedLanguageIndex];
        val locationUri = getLocation(language).first;
        val locationTitle = getLocation(language).second;
        val latitude = locationUri.removePrefix("geo:").split(",")[0].toDouble()
        val longitude = locationUri.removePrefix("geo:").split(",")[1].toDouble()
        val intent = Intent(this, MapsActivity::class.java).apply {
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("title", locationTitle)
            putExtra("language", when (language) {
                "Chinese" -> "zh"
                "French" -> "fr"
                "Spanish" -> "es"
                else -> "zh"
            })
        }
        startActivity(intent)
    }
}