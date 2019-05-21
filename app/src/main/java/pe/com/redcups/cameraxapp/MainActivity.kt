package pe.com.redcups.cameraxapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS =  arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity(), LifecycleOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(allPermissionsGranted()){
            view_finder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        view_finder.addOnLayoutChangeListener{ _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private fun startCamera() {

        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1,1))
            setTargetResolution(Size(640, 640))
        }.build()

        val preview = Preview(previewConfig)

        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setTargetAspectRatio(Rational(1,1))

                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)
        capture_button.setOnClickListener{
            //Set path to save the photo
            val file = File(externalMediaDirs.first(),
                "${System.currentTimeMillis()}.jpg")
            // perform picture capture
            imageCapture.takePicture(file,
                object: ImageCapture.OnImageSavedListener{
                    override fun onError(useCaseError: ImageCapture.UseCaseError,
                                         message: String, cause: Throwable?) {
                        val msg = "Photo capture failed: $message"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        Log.e("CameraXApp", msg)
                        cause?.printStackTrace()
                    }

                    override fun onImageSaved(file: File) {
                        val msg = "Photo capture succeeded: ${file.absolutePath}"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        Log.d("CameraXApp", msg)}
                })
        }

        preview.setOnPreviewOutputUpdateListener {
            view_finder.surfaceTexture = it.surfaceTexture
            Toast.makeText(this, "Permissions not granted by the user", Toast.LENGTH_SHORT)
                .show()
            updateTransform()
        }
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    override fun onStop() {
        super.onStop()
        Toast.makeText(this, "Unbound", Toast.LENGTH_SHORT)
            .show()
        CameraX.unbindAll()
    }

    override fun onPause() {
        super.onPause()
        Toast.makeText(this, "Unbound", Toast.LENGTH_SHORT)
            .show()
        CameraX.unbindAll()
    }

    private fun updateTransform() {
        val matrix = Matrix()

        val centerX = view_finder.width / 2f
        val centerY = view_finder.height / 2f

        val rotationDegrees = when(view_finder.display.rotation){
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }

        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        view_finder.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                view_finder.post{
                    startCamera()
                }
            } else {
                Toast.makeText(this, "Permissions not granted by the user", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}
