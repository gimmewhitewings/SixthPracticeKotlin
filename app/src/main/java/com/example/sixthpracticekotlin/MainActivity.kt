package com.example.sixthpracticekotlin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sixthpracticekotlin.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            val imageFile = File(filesDir, "downloaded_image.jpg")
            if (imageFile.exists()) {
                val savedImageBitmap = loadBitmapFromFile(imageFile)
                imageView.setImageBitmap(savedImageBitmap)
            }
            downloadButton.setOnClickListener {
                val url = textInputEditText.text.toString().trim()

                if (url.isNotEmpty()) {
                    val networkThreadDispatcher = newSingleThreadContext("Network")
                    val diskThreadDispatcher = newSingleThreadContext("Disk")
                    // launch download in lifecycle scope using newSingleThreadContext
                    lifecycleScope.launch {
                        val downloadedImage: Bitmap
                        withContext(networkThreadDispatcher) {
                            downloadedImage = downloadImage(url)
                        }
                        withContext(diskThreadDispatcher) {
                            saveImageLocally(downloadedImage)
                        }
                        withContext(Dispatchers.Main) {
                            imageView.setImageBitmap(downloadedImage)
                        }
                    }
                }
            }
        }
    }

    private fun loadBitmapFromFile(file: File): Bitmap {
        val inputStream = FileInputStream(file)
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun saveImageLocally(image: Bitmap) {
        val fileName = "downloaded_image.jpg"

        try {
            // Get the directory for saving images in internal storage
            val dir = filesDir

            // Create a File object for the image file
            val imageFile = File(dir, fileName)

            // Create an OutputStream to write the image data to the file
            val outputStream: OutputStream = FileOutputStream(imageFile)

            // Compress the bitmap and write it to the OutputStream
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

            // Close the OutputStream
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun downloadImage(imageUrl: String): Bitmap {
        val url = URL(imageUrl)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()

        val inputStream: InputStream = connection.inputStream
        return BitmapFactory.decodeStream(inputStream)
    }
}