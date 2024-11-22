package za.co.varsitycollege.st10215473.community.ServiceReg

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import za.co.varsitycollege.st10215473.community.CustomerReg.CustomerStep4Fragment
import za.co.varsitycollege.st10215473.community.R
import za.co.varsitycollege.st10215473.community.data.RegistrationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ServiceStep3Fragment : Fragment() {
    private lateinit var dayPicker: NumberPicker
    private lateinit var monthPicker: NumberPicker
    private lateinit var yearPicker: NumberPicker
    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var backButton: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_service_step3, container, false)
        registrationViewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]

        backButton = view.findViewById(R.id.btnStep3Back)
        val btnNext3 = view.findViewById<MaterialButton>(R.id.cusNext3)
        val age = view.findViewById<EditText>(R.id.edtAge)
        dayPicker = view.findViewById(R.id.dayPicker)
        monthPicker = view.findViewById(R.id.monthPicker)
        yearPicker = view.findViewById(R.id.yearPicker)

        dayPicker.minValue = 1
        dayPicker.maxValue = 31

        monthPicker.minValue = 1
        monthPicker.maxValue = 12

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = currentYear - 100
        yearPicker.maxValue = currentYear

        // Pre-select today's date
        val calendar = Calendar.getInstance()
        dayPicker.value = calendar.get(Calendar.DAY_OF_MONTH)
        monthPicker.value = calendar.get(Calendar.MONTH) + 1 // Months are zero-indexed
        yearPicker.value = calendar.get(Calendar.YEAR)

        backButton.setOnClickListener{
            activity?.onBackPressed()
        }

        btnNext3.setOnClickListener{
            val selectedDay = dayPicker.value
            val selectedMonth = monthPicker.value
            val selectedYear = yearPicker.value
            val ageString = age.text.toString()

            val dobString = "$selectedDay/$selectedMonth/$selectedYear"
            val dob: Date? = parseDateOfBirth(dobString)

            val ageToInt: Int = ageString.toIntOrNull() ?: run {
                Toast.makeText(requireContext(), "Invalid age input", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registrationViewModel.age = ageToInt
            registrationViewModel.dateOfBirth = dob

            navigateToNextStep()
        }

        return view
    }

    private fun navigateToNextStep() {
        val fragment = ServiceStep4Fragment() // Replace with your next fragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment) // Ensure fragmentContainer is the ID of your container
            .addToBackStack(null) // Add to backstack for back navigation
            .commit()
    }

    private fun parseDateOfBirth(dobString: String): Date? {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.parse(dobString)
        } catch (e: Exception) {
            null // Return null if parsing fails
        }
    }

}