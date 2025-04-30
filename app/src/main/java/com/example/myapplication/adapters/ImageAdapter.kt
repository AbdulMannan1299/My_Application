// ImageAdapter.kt
package com.example.myapplication.imageadapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ImageItem
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class ImageAdapter(private val images: MutableList<ImageItem>, private val onClick: (ImageItem) -> Unit) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_image,
            parent,
            false
        ) // Replace with your image item layout
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        holder.imageTitleTextView.text = image.name
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
//        holder.imageDateTimeTextView.text = dateFormat.format(image.date)

        // Load image manually using AsyncTask
        LoadBitmapTask(holder.imageImageView).execute(image.uri)

        holder.itemView.setOnClickListener { onClick(image) }
    }

    override fun getItemCount() = images.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageImageView: ImageView =
            itemView.findViewById(R.id.imageImageView) // Replace with your ImageView ID
        val imageTitleTextView: TextView =
            itemView.findViewById(R.id.imageTitleTextView) // Replace with your title TextView ID
        val imageDateTimeTextView: TextView =
            itemView.findViewById(R.id.imageDateTimeTextView) // Replace with your date/time TextView ID
    }

    // AsyncTask to load Bitmap in the background
    private class LoadBitmapTask(private val imageView: ImageView) :
        AsyncTask<Uri, Void, Bitmap>() {

        override fun doInBackground(vararg params: Uri?): Bitmap? {
            val uri = params[0] ?: return null
            var inputStream: InputStream? = null
            try {
                inputStream = imageView.context.contentResolver.openInputStream(uri)
                return BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    // Ignore
                }
            }
            return null
        }

        override fun onPostExecute(result: Bitmap?) {
            if (result != null) {
                imageView.setImageBitmap(result)
            }
        }
    }
}