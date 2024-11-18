package za.co.varsitycollege.st10215473.community.adapter

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.GeoPoint
import com.squareup.picasso.Picasso
import za.co.varsitycollege.st10215473.community.R
import za.co.varsitycollege.st10215473.community.data.Favourites
import za.co.varsitycollege.st10215473.community.data.ServiceProvider
import za.co.varsitycollege.st10215473.community.databinding.FavouritesCardviewBinding
import java.io.IOException
import java.util.Locale

class FavouritesAdapter(
    private val favouritesList: ArrayList<Favourites>,
    private val context: Context  // Add Context as a constructor parameter
) : RecyclerView.Adapter<FavouritesAdapter.ViewHolder>() {

    class ViewHolder(val binding: FavouritesCardviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FavouritesCardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return favouritesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = favouritesList[position]
        holder.apply {
            binding.apply {
                // Set title and description
                txtIndividualName.text = "${currentItem.name} ${currentItem.surname}"
                ratingBar2.rating = currentItem.rating.toFloat()
                txtFavouritesBio.text = currentItem.bio.toString()

                val profileUrl = currentItem.imageUrl
                if (profileUrl.isNullOrEmpty()) {
                    Glide.with(context)
                        .load(R.drawable.profile) // Default image resource
                        .into(imgFavouriteProfile)
                } else {
                    Glide.with(context)
                        .load(profileUrl)
                        .circleCrop()
                        .into(imgFavouriteProfile)
                }
            }
        }
    }



}