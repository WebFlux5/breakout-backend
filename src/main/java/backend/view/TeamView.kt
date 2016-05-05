package backend.view

import backend.model.event.Team
import org.hibernate.validator.constraints.NotEmpty
import java.util.*

class TeamView() {

    var id: Long? = null

    @NotEmpty
    var name: String? = null

    var event: Long? = null

    var description: String = ""

    var members: MutableList<BasicUserView>? = null

    var profilePic: MediaView? = null

    var invoiceId: Long? = null

    constructor(team: Team) : this() {
        this.id = team.id
        this.name = team.name
        this.event = team.event.id
        this.description = team.description
        this.members = ArrayList()
        team.members.forEach { this.members!!.add(BasicUserView(it)) }
        this.profilePic = MediaView(team.profilePic)
        this.invoiceId = team.invoice?.id
    }
}
