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

class MainActivity : AppCompatActivity() {

    // Data collections
    private val folders = mutableListOf("Work Notes", "Personal", "Ideas")
    private val images = mutableListOf<ImageItem>()

    // Adapters
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var imageAdapter: ImageAdapter

    // View state
    private var currentFolder = ""

    // Request codes
    private companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_PICK = 2
        const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val titleView = findViewById<TextView>(R.id.titleTextView) // Corrected ID
        val fab = findViewById<FloatingActionButton>(R.id.addButton) // Corrected ID

        // Setup RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Initialize adapters
        folderAdapter = FolderAdapter(folders) { folderName ->
            openFolder(folderName.toString(), recyclerView, titleView)
        }

        imageAdapter = ImageAdapter(images) { image ->
            shareImage(image)
        }

        // Set initial view
        showFolderView(recyclerView, titleView)

        // Setup FAB click listener
        fab.setOnClickListener {
            if (currentFolder.isEmpty()) {
                showAddFolderDialog()
            } else {
                checkPermissionsAndShowImageOptions()
            }
        }

        // Check for permissions
        checkPermissions()
    }

    private fun showFolderView(recyclerView: RecyclerView, titleView: TextView) {
        currentFolder = ""
        titleView.text = "My Folders"
        recyclerView.adapter = folderAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    private fun openFolder(folderName: String, recyclerView: RecyclerView, titleView: TextView) {
        currentFolder = folderName
        titleView.text = folderName
        images.clear() // Clear previous images

        // In a real app, you would load images for this folder from storage here

        recyclerView.adapter = imageAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 3)
    }

    private fun showAddFolderDialog() {
        val input = EditText(this).apply {
            hint = "Enter folder name"
            setSingleLine()
        }

        AlertDialog.Builder(this)
            .setTitle("New Folder")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                input.text.toString().takeIf { it.isNotBlank() }?.let { name ->
                    if (folders.contains(name)) {
                        Toast.makeText(this, "Folder already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        folders.add(name)
                        folderAdapter.notifyItemInserted(folders.size - 1)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkPermissionsAndShowImageOptions() {
        if (hasRequiredPermissions()) {
            showImageSourceDialog()
        } else {
            requestPermissions()
        }
    }

    private fun showImageSourceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Add Image")
            .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                when (which) {
                    0 -> dispatchTakePictureIntent()
                    1 -> dispatchPickImageIntent()
                }
            }
            .show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.run {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } ?: Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dispatchPickImageIntent() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { intent ->
            intent.resolveActivity(packageManager)?.run {
                startActivityForResult(intent, REQUEST_IMAGE_PICK)
            } ?: Toast.makeText(this, "No gallery app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareImage(image: Image) {
        try {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, image.uri)
                startActivity(Intent.createChooser(this, "Share Image"))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> handleCameraResult(data)
                REQUEST_IMAGE_PICK -> handleGalleryResult(data)
            }
        }
    }

    private fun handleCameraResult(data: Intent?) {
        val imageUri = data?.data
        if (imageUri != null) {
            addImageToCollection(imageUri)
        } else {
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleGalleryResult(data: Intent?) {
        val imageUri = data?.data
        if (imageUri != null) {
            addImageToCollection(imageUri)
        } else {
            Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addImageToCollection(uri: Uri) {
        val newImage = ImageItem("Image ${images.size + 1}", uri)
        images.add(newImage)
        imageAdapter.notifyItemInserted(images.size - 1)
    }

    override fun onBackPressed() {
        if (currentFolder.isNotEmpty()) {
            showFolderView(
                findViewById(R.id.recyclerView),
                findViewById(R.id.titleTextView) // Corrected ID
            )
        } else {
            super.onBackPressed()
        }
    }

    // Permission handling
    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissions() {
        if (!hasRequiredPermissions()) {
            requestPermissions()
        }
    }

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

data class ImageItem(val name: String, val uri: Uri)