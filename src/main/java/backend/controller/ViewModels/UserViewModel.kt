package backend.controller.ViewModels

import backend.model.user.Participant
import backend.model.user.User
import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.validator.constraints.Email
import javax.validation.Valid

class UserViewModel() {

    @JsonIgnore
    var user: User? = null

    var password: String? = null
    var firstname: String? = null
    var lastname: String? = null

    @Email
    var email: String? = null
    var gender: String? = null
    var id: Long? = null
    var isBlocked: Boolean? = null

    @Valid
    var participant: ParticipantViewModel? = null

    constructor(user: User) : this() {
        this.user = user
        this.firstname = user.firstname
        this.lastname = user.lastname
        this.email = user.email
        this.gender = user.gender
        this.id = user.core!!.id
        this.isBlocked = user.isBlocked
        this.participant = if (user.hasRole(Participant::class.java)) ParticipantViewModel(user) else null
    }
}
