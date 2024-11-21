package za.co.varsitycollege.st10215473.community

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import za.co.varsitycollege.st10215473.community.HubPages.Cleaning
import za.co.varsitycollege.st10215473.community.HubPages.ElectricalActivity
import za.co.varsitycollege.st10215473.community.HubPages.GardeningActivity
import za.co.varsitycollege.st10215473.community.HubPages.HandymanActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HubFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class HubFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_hub, container, false)

        // Set OnClickListener for buttons
        val openCleaningPage = view.findViewById<ImageButton>(R.id.btnCleaning)
        val openHandyManPage = view.findViewById<ImageButton>(R.id.btnHandyman)
        val openGardeningPage = view.findViewById<ImageButton>(R.id.btnGardening)
        val openElectricalPage = view.findViewById<ImageButton>(R.id.btnElectrical)
        val openPestPage = view.findViewById<ImageButton>(R.id.btnPestControl)
        val openBeautyPage = view.findViewById<ImageButton>(R.id.btnBeauty)
        val openPetPage = view.findViewById<ImageButton>(R.id.btnPet)
        val openEducationPage = view.findViewById<ImageButton>(R.id.btnEducation)
        val openVehiclePage = view.findViewById<ImageButton>(R.id.btnVehicle)
        val openCateringPage = view.findViewById<ImageButton>(R.id.btnCooking)
        val openHealthPage = view.findViewById<ImageButton>(R.id.btnHealth)
        val openMusicPage = view.findViewById<ImageButton>(R.id.btnEntertainment)
        val openOtherPage = view.findViewById<ImageButton>(R.id.btnOther)

        openCleaningPage.setOnClickListener {
            val intent = Intent(requireContext(), Cleaning::class.java)
            startActivity(intent)
        }
        openHandyManPage.setOnClickListener {
            val intent = Intent(requireContext(), HandymanActivity::class.java)
            startActivity(intent)
        }
        openGardeningPage.setOnClickListener {
            val intent = Intent(requireContext(), GardeningActivity::class.java)
            startActivity(intent)
        }
        openElectricalPage.setOnClickListener {
            val intent = Intent(requireContext(), ElectricalActivity::class.java)
            startActivity(intent)
        }


        openPestPage.setOnClickListener {
            Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }
        openBeautyPage.setOnClickListener {
            Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }
        openPetPage.setOnClickListener {
            Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }
        openEducationPage.setOnClickListener {
            Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }
        openVehiclePage.setOnClickListener {
            Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }
        openCateringPage.setOnClickListener {
            Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }
        openHealthPage.setOnClickListener {
            Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }
        openMusicPage.setOnClickListener {
            Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }
        openOtherPage.setOnClickListener {
            Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HubFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HubFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}