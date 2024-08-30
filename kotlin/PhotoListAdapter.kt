package com.bignerdranch.android.photogallery

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bignerdranch.android.photogallery.api.GalleryItem
import com.bignerdranch.android.photogallery.databinding.ListItemGalleryBinding

//Listing 20.31 Adding a ViewHolder implementation
class PhotoViewHolder(
    private val binding: ListItemGalleryBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(galleryItem: GalleryItem, onItemClicked: (Uri) -> Unit) {
        binding.itemImageView.load(galleryItem.url) {
            placeholder(R.drawable.bill_up_close)
        }
        //23.2 Firing an implicit intent when an item is pressed
        binding.root.setOnClickListener { onItemClicked(galleryItem.photoPageUri) }
    }
}

//Listing 20.32 Adding a RecyclerView.Adapter Implementation
//provides PhotoViewHolders as needed based on a list of Gallery Items
class PhotoListAdapter(
    private val galleryItems: List<GalleryItem>,
    //23.3 Binding PhotoViewHolder
    private val onItemClicked: (Uri) -> Unit
) : RecyclerView.Adapter<PhotoViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemGalleryBinding.inflate(inflater, parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = galleryItems[position]
        //23.3 Binding PhotoViewHolder
        holder.bind(item, onItemClicked)
    }

    override fun getItemCount() = galleryItems.size
}