// FolderAdapter.kt
package com.example.myapplication.folderactivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

// FolderAdapter class to display folders in a RecyclerView
class FolderAdapter(
    private val folders: MutableList<String>,  // List of folder names
    private val onClick: (String) -> Unit    // Lambda function to handle folder click events
) :
    RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    // Creates a new ViewHolder for each folder item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_folder,
            parent,
            false
        ) // Inflate the folder item layout
        return FolderViewHolder(view)  // Return the ViewHolder
    }

    // Binds the folder data to the ViewHolder
    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]  // Get the folder name at the current position
        holder.folderNameTextView.text = folder  // Set the folder name in the TextView
        holder.itemView.setOnClickListener { onClick(folder) }  // Set click listener for the folder item
    }

    // Returns the total number of folders
    override fun getItemCount() = folders.size

    // ViewHolder class to hold the views for each folder item
    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderNameTextView: TextView =
            itemView.findViewById(R.id.folderNameTextView)  // Get the folder name TextView
    }
}
