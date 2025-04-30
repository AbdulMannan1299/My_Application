package com.example.myapplication
// MainActivity.kt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.folderactivity.FolderAdapter // Corrected import path
import com.example.myapplication.imageadapter.ImageAdapter

// Data class to represent an image item
data class ImageItem(val name: String, val uri: Uri)

// MainActivity class, the main activity of the application
class MainActivity : AppCompatActivity() {

    // Data collections
    private val folders =
        mutableListOf("Work Notes", "Personal", "Ideas")  // List of folder names, now mutable
    private val images =
        mutableListOf<ImageItem>()
    // Adapters
    private lateinit var folderAdapter: FolderAdapter  // Adapter for displaying folders
    private lateinit var imageAdapter: ImageAdapter    // Adapter for displaying images

    // View state
    private var currentFolder =
        ""  // Stores the name of the currently selected folder

    // Request codes for starting activities and requesting permissions
    private companion object {
        const val REQUEST_IMAGE_CAPTURE = 1  // Request code for taking a photo
        const val REQUEST_IMAGE_PICK = 2    // Request code for picking an image from gallery
        const val PERMISSION_REQUEST_CODE =
            100 // Request code for requesting permissions
    }

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Set the layout for the activity

        // Initialize UI components
        val recyclerView =
            findViewById<RecyclerView>(R.id.recyclerView)  // Get the RecyclerView from the layout
        val titleView =
            findViewById<TextView>(R.id.titleTextView)    // Get the TextView for the title
        val fab =
            findViewById<FloatingActionButton>(R.id.addButton)  // Get the FloatingActionButton

        // Setup RecyclerView
        recyclerView.layoutManager =
            GridLayoutManager(this, 2)  // Set the layout manager for the RecyclerView

        // Initialize adapters
        folderAdapter = FolderAdapter(folders) { folderName ->  // Initialize the folder adapter
            openFolder(
                folderName,
                recyclerView,
                titleView
            ) // Set the click listener to open the folder
        }

        imageAdapter = ImageAdapter(images) { imageItem: ImageItem ->  // Initialize the image adapter.  Explicitly type the parameter
            shareImage(imageItem)            // Set the click listener to share the image
        }

        // Set initial view
        showFolderView(
            recyclerView,
            titleView
        )            // Show the folder view initially

        // Setup FAB click listener
        fab.setOnClickListener {           // Set click listener for the FloatingActionButton
            if (currentFolder.isEmpty()) {
                showAddFolderDialog()  // If no folder is selected, show the add folder dialog
            } else {
                checkPermissionsAndShowImageOptions()  // Otherwise, check permissions and show image options
            }
        }

        // Check for permissions
        checkPermissions()                   // Check for necessary permissions
    }

    // Function to show the folder view
    private fun showFolderView(recyclerView: RecyclerView, titleView: TextView) {
        currentFolder = ""                 // Reset the current folder
        titleView.text = "My Folders"      // Set the title to "My Folders"
        recyclerView.adapter =
            folderAdapter      // Set the adapter for the RecyclerView
        recyclerView.layoutManager =
            GridLayoutManager(this, 2)  // Set the layout manager for the RecyclerView
    }

    // Function to open a folder and show its images
    private fun openFolder(folderName: String, recyclerView: RecyclerView, titleView: TextView) {
        currentFolder = folderName           // Set the current folder
        titleView.text = folderName         // Set the title to the folder name
        images.clear()                       // Clear the list of images
        // In a real app, you would load images for this folder from storage here
        recyclerView.adapter =
            imageAdapter       // Set the adapter for the RecyclerView
        recyclerView.layoutManager =
            GridLayoutManager(this, 3)  // Set the layout manager for the RecyclerView
    }

    // Function to show a dialog to add a new folder
    private fun showAddFolderDialog() {
        val input = EditText(this).apply {  // Create an EditText for input
            hint = "Enter folder name"
            setSingleLine()
        }

        AlertDialog.Builder(this)           // Create an AlertDialog
            .setTitle("New Folder")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->  // Set the positive button
                val folderName =
                    input.text.toString().trim() // Get the folder name from the input
                if (folderName.isNotBlank()) {  // Check if the folder name is not blank
                    if (folders.contains(folderName)) {
                        Toast.makeText(
                            this,
                            "Folder already exists",
                            Toast.LENGTH_SHORT
                        ).show() // Show a toast if the folder name already exists
                    } else {
                        folders.add(folderName)                         // Add the folder name to the list
                        folderAdapter.notifyItemInserted(folders.size - 1)  // Notify the adapter that a new item has been inserted
                    }
                }
            }
            .setNegativeButton("Cancel", null)  // Set the negative button
            .show()
    }

    // Function to check permissions and show image options
    private fun checkPermissionsAndShowImageOptions() {
        if (hasRequiredPermissions()) {      // Check if the required permissions are granted
            showImageSourceDialog()    // If granted, show the image source dialog
        } else {
            requestPermissions()         // Otherwise, request the permissions
        }
    }

    // Function to show a dialog to choose the image source
    private fun showImageSourceDialog() {
        AlertDialog.Builder(this)           // Create an AlertDialog
            .setTitle("Add Image")
            .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->  // Set the items
                when (which) {
                    0 -> dispatchTakePictureIntent()  // If "Take Photo" is selected, dispatch the take picture intent
                    1 -> dispatchPickImageIntent()    // If "Choose from Gallery" is selected, dispatch the pick image intent
                }
            }
            .show()
    }

    // Function to dispatch the take picture intent
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->  // Create an intent to capture an image
            intent.resolveActivity(packageManager)?.run {  // Check if there is an app that can handle the intent
                startActivityForResult(
                    intent,
                    REQUEST_IMAGE_CAPTURE
                )  // Start the activity for result
            } ?: Toast.makeText(
                this,
                "No camera app found",
                Toast.LENGTH_SHORT
            ).show()  // Show a toast if no camera app is found
        }
    }

    // Function to dispatch the pick image intent
    private fun dispatchPickImageIntent() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { intent ->  // Create an intent to pick an image
            intent.resolveActivity(packageManager)?.run {  // Check if there is an app that can handle the intent
                startActivityForResult(
                    intent,
                    REQUEST_IMAGE_PICK
                )    // Start the activity for result
            } ?: Toast.makeText(
                this,
                "No gallery app found",
                Toast.LENGTH_SHORT
            ).show()  // Show a toast if no gallery app is found
        }
    }

    // Function to share an image
    private fun shareImage(image: ImageItem) {
        try {
            Intent(Intent.ACTION_SEND).apply {  // Create an intent to share an image
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, image.uri)  // Put the image URI as an extra
                startActivity(Intent.createChooser(this, "Share Image"))  // Start the activity for result
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to handle activity results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {  // Check if the result code is OK
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> handleCameraResult(data)  // Handle camera result
                REQUEST_IMAGE_PICK -> handleGalleryResult(data)    // Handle gallery result
            }
        }
    }

    // Function to handle the result of taking a photo
    private fun handleCameraResult(data: Intent?) {
        val imageUri = data?.data  // Get the image URI from the intent
        if (imageUri != null) {
            addImageToCollection(imageUri)  // Add the image to the collection
        } else {
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to handle the result of picking an image from the gallery
    private fun handleGalleryResult(data: Intent?) {
        val imageUri = data?.data  // Get the image URI from the intent
        if (imageUri != null) {
            addImageToCollection(imageUri)  // Add the image to the collection
        } else {
            Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to add an image to the collection
    private fun addImageToCollection(uri: Uri) {
        val newImage =
            ImageItem("Image ${images.size + 1}", uri)  // Create a new ImageItem
        images.add(newImage)                               // Add the image to the list
        imageAdapter.notifyItemInserted(images.size - 1)    // Notify the adapter
    }

    // Function to handle back button press
    override fun onBackPressed() {
        if (currentFolder.isNotEmpty()) {  // If a folder is currently open
            showFolderView(
                findViewById(R.id.recyclerView),
                findViewById(R.id.titleTextView)
            )  // Show the folder view
        } else {
            super.onBackPressed()          // Otherwise, call the super class's onBackPressed()
        }
    }

    // Permission handling

    // Function to check if the required permissions are granted
    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to check permissions
    private fun checkPermissions() {
        if (!hasRequiredPermissions()) {  // If the required permissions are not granted
            requestPermissions()       // Request the permissions
        }
    }

    // Function to request permissions
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    // Function to handle permission request results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
