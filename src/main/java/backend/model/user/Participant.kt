@file:JvmName("Participant")

package backend.model.user

import backend.model.event.Team
import backend.model.location.Location
import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
@DiscriminatorValue("PARTICIPANT")
class Participant : UserRole {

    var emergencynumber: String = ""
    var tshirtsize: String? = null
    var hometown: String? = null
    var phonenumber: String? = null
    var birthdate: LocalDate? = null

    @ManyToOne
    var currentTeam: Team? = null

    @OneToMany(mappedBy = "uploader")
    val locations: MutableList<Location> = ArrayList()

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(account: UserAccount) : super(account)

    override fun getAuthority(): String = "PARTICIPANT"

    @PreRemove
    fun preRemove() {
        this.locations.forEach { it.uploader = null }
        this.locations.clear()
    }
}
