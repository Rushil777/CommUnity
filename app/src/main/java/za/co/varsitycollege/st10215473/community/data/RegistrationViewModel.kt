package za.co.varsitycollege.st10215473.community.data

import androidx.lifecycle.ViewModel
import java.util.Date

class RegistrationViewModel : ViewModel() {
    var name: String? = null
    var surname: String? = null
    var phoneNumber: String? = null
    var idNumber: String? = null
    var email: String? = null
    var age: Int? = null
    var dateOfBirth: Date? = null
    var password: String? = null
}