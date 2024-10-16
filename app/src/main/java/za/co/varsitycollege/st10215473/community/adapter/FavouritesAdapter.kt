package za.co.varsitycollege.st10215473.community.adapter

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.GeoPoint
import com.squareup.picasso.Picasso
import za.co.varsitycollege.st10215473.community.R
import za.co.varsitycollege.st10215473.community.data.ServiceProvider
import za.co.varsitycollege.st10215473.community.databinding.FavouritesCardviewBinding
import java.io.IOException
import java.util.Locale

class FavouritesAdapter(
    private val serviceProviderList: ArrayList<ServiceProvider>,
    private val context: Context  // Add Context as a constructor parameter
) : RecyclerView.Adapter<FavouritesAdapter.ViewHolder>() {

    class ViewHolder(val binding: FavouritesCardviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FavouritesCardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return serviceProviderList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = serviceProviderList[position]
        holder.apply {
            binding.apply {
                // Set title and description
                txtCompanyName.text = currentItem.businessName
                txtIndividualName.text = currentItem.name

                // Set image if available, otherwise set a default image
                if (!currentItem.imageUrl.isNullOrEmpty()) {
                    Picasso.get().load(currentItem.imageUrl).into(imgReportHistory)
                } else {
                    imgReportHistory.setImageResource(R.drawable.logo)
                }
            }
        }
    }



}
