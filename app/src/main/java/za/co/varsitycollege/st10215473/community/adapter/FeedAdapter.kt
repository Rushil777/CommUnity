package za.co.varsitycollege.st10215473.community.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.community.ServiceProviderSelection
import za.co.varsitycollege.st10215473.community.data.Feed
import za.co.varsitycollege.st10215473.community.databinding.FeedCardviewBinding

class FeedAdapter(
    private val feedList: ArrayList<Feed>,
    private val context: Context
) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    class ViewHolder(val binding: FeedCardviewBinding) : RecyclerView.ViewHolder(binding.root) {
        var currentImageIndex = 0 // Track the current image index for this holder
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FeedCardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return feedList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = feedList[position]
        holder.apply {
            binding.apply {
                // Set title and description
                txtFeedName.text = "${currentItem.name} ${currentItem.surname}"
                txtFeedDescription.text = currentItem.description

                // Handle images
                if (currentItem.imageUrls.isNotEmpty()) {
                    imgFeedImage.visibility = View.VISIBLE
                    tapLeft.visibility = View.VISIBLE
                    tapRight.visibility = View.VISIBLE

                    // Initialize current image index
                    holder.currentImageIndex = 0

                    // Load the first image initially
                    loadImage(imgFeedImage, currentItem.imageUrls[holder.currentImageIndex])

                    imgFeedImage.setOnTouchListener { v, event ->
                        val width = v.width
                        when (event.action) {
                            MotionEvent.ACTION_UP -> {
                                if (event.x < width / 2) {
                                    // Left side tap - previous image
                                    showPreviousImage(holder, currentItem)
                                } else {
                                    // Right side tap - next image
                                    showNextImage(holder, currentItem)
                                }
                            }
                        }
                        true
                    }
                } else {
                    imgFeedImage.visibility = View.GONE
                    tapLeft.visibility = View.GONE
                    tapRight.visibility = View.GONE
                }

                if(currentItem.profileUrl.isNotEmpty()){
                    Glide.with(context)
                        .load(currentItem.profileUrl)
                        .transform(CircleCrop())
                        .into(imgFeedProfile)
                }

                imgFeedProfile.setOnClickListener {
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUserId != null) {
                        FirebaseFirestore.getInstance().collection("Consumer").document(currentUserId)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val intent = Intent(context, ServiceProviderSelection::class.java)
                                    intent.putExtra("selectedProviderId", currentItem.userId) // Pass the selected provider ID
                                    context.startActivity(intent)
                                } else {

                                }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(context, "Failed to verify user role: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    private fun showPreviousImage(holder: ViewHolder, feed: Feed) {
        if (feed.imageUrls.isNotEmpty()) {
            holder.currentImageIndex = if (holder.currentImageIndex > 0) {
                holder.currentImageIndex - 1
            } else {
                feed.imageUrls.size - 1 // Loop to the last image
            }
            loadImage(holder.binding.imgFeedImage, feed.imageUrls[holder.currentImageIndex])
        }
    }

    private fun showNextImage(holder: ViewHolder, feed: Feed) {
        if (feed.imageUrls.isNotEmpty()) {
            holder.currentImageIndex = if (holder.currentImageIndex < feed.imageUrls.size - 1) {
                holder.currentImageIndex + 1
            } else {
                0 // Loop back to the first image
            }
            loadImage(holder.binding.imgFeedImage, feed.imageUrls[holder.currentImageIndex])
        }
    }




    private fun loadImage(imageView: ImageView, url: String) {
        Glide.with(context)
            .load(url)
            .into(imageView)
    }

}
