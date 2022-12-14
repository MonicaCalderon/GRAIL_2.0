package com.example.grail20

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.grail20.databinding.ActivityMainBinding
import com.google.android.material.imageview.ShapeableImageView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var vibrator: Vibrator
    private lateinit var btnSettings: Button
    private lateinit var btnCreateNotification: Button
    private lateinit var notificationRect: ShapeableImageView

    private var imageCapture:ImageCapture?=null

    // Settings
    private var camera: Boolean = false
    private var audioNotifications: Boolean = true
    private var visualNotifications: Boolean = true
    private var interventionMode: String = "low_intervention"
    private var pedestrianNetwork: String = "network_1"
    private var driverNetwork: String = "network_1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSettings()

        if(allPermissionsGranted()) {
            startCamera()
        }else{
            ActivityCompat.requestPermissions(
                this, Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }

        btnSettings = findViewById<Button>(R.id.btn_settings)
        btnSettings.setOnClickListener {
           val intent =  Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        btnCreateNotification = findViewById(R.id.btnCreateNotification );
        btnCreateNotification.setOnClickListener{
            userAlert("Bot??n pulsado")
        }

        }

    // Because of restart settings
    override fun onRestart() {
        super.onRestart()
        restartSettings()
    }

    private fun restartSettings(){
        loadSettings()
        startCamera()
    }

    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider
            .getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { mPreview->
                    mPreview.setSurfaceProvider(
                        binding.viewFinder.surfaceProvider
                    )
                }
            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = prefCamera(camera)

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector,
                    preview, imageCapture
                )
            }catch (e: Exception){
                Log.d(Constants.TAG, "startCamera Fail: ", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun loadSettings(){
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        camera = sp.getBoolean("camera", false)
        audioNotifications = sp.getBoolean("audio_notifications", true)
        visualNotifications = sp.getBoolean("visual_notifications", true)
        interventionMode = sp.getString("intervention_mode", "low_intervention").toString()
        pedestrianNetwork = sp.getString("pedestrian_network", "network_1").toString()
        driverNetwork = sp.getString("driver_network", "network_1").toString()
    }

    private fun prefCamera(camera: Boolean): CameraSelector {
        return if (camera) CameraSelector.DEFAULT_FRONT_CAMERA
        else CameraSelector.DEFAULT_BACK_CAMERA
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()) {
                startCamera()
            }else{
                Toast.makeText(this, getString(R.string.premissions_not_granted), Toast.LENGTH_SHORT)
                    .show()
            
            }
        }
    }

    private fun allPermissionsGranted() =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }

    // Function for future uses. Will be called by external alerts managed by the project
    private fun userAlert(alert : String){

        // TODO: complete for each scenarios
        when(interventionMode){
            "low_intervention" -> {
                audioNotification()
                visualNotification(alert)
                Toast.makeText(this, "Modo de intervenci??n BAJO", Toast.LENGTH_SHORT).show()
            }
            "medium_intervention" -> {
                audioNotification()
                visualNotification(alert)
                Toast.makeText(this, "Modo de intervenci??n MEDIO", Toast.LENGTH_SHORT).show()
            }
            "active_intervention" -> {
                audioNotification()
                visualNotification(alert)
                Toast.makeText(this, "Modo de intervenci??n ACTIVO", Toast.LENGTH_SHORT).show()
            }
            else -> Toast.makeText(this, "Error: el modo de intervenci??n no se lee correctamente", Toast.LENGTH_SHORT).show()
        }
    }


    private fun visualNotification(alert: String){
        if (visualNotifications) {
            // TODO: This part will be manage by external events
            //  implemented in the future for this project
            notificationRect = findViewById(R.id.notificationRect );

            notificationRect.visibility = View.VISIBLE


            //TODO: when driver reply, notification goes off
        }
    }

    private fun audioNotification(){
        if (audioNotifications) {
            val mp: MediaPlayer = MediaPlayer.create(this, R.raw.notification)
            mp.start()

            vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.EFFECT_HEAVY_CLICK))
        }
    }
}