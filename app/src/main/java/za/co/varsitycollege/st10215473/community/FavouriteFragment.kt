package za.co.varsitycollege.st10215473.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.community.adapter.FavouritesAdapter
import za.co.varsitycollege.st10215473.community.data.Favourites

class FavouriteFragment : Fragment() {

    private lateinit var favouritesRecyclerView: RecyclerView
    private lateinit var favouritesAdapter: FavouritesAdapter
    private var favouritesList: ArrayList<Favourites> = arrayListOf() // Favourites data list

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourite, container, false)

        // Initialize Firestore and Firebase Auth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize RecyclerView
        favouritesRecyclerView = view.findViewById(R.id.rvFavourites)
        favouritesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize and set adapter
        favouritesAdapter = FavouritesAdapter(favouritesList, requireContext())
        favouritesRecyclerView.adapter = favouritesAdapter

        // Fetch and load favourites data
        loadFavourites()

        return view
    }

    // Method to load favourites from Firestore
    private fun loadFavourites() {
        val currentUserId = auth.currentUser?.uid ?: return

        // Reference to the consumer's favourites in Firestore
        firestore.collection("Consumer").document(currentUserId)
            .collection("Favourites")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val favourite = document.toObject(Favourites::class.java)
                    favouritesList.add(favourite)
                }
                // Notify the adapter about data changes
                favouritesAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                exception.printStackTrace()
            }
    }
}
