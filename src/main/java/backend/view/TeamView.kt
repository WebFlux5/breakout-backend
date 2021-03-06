package backend.view

import backend.model.event.Team
import backend.util.data.DonateSums
import java.util.*

class TeamView() {

    var id: Long? = null

    var name: String? = null

    var event: Long? = null

    var description: String? = null

    var hasStarted: Boolean? = null

    var members: MutableList<BasicUserView>? = null

    var profilePic: MediaView? = null

    var invoiceId: Long? = null

    var hasFullyPaid: Boolean? = null

    var isFull: Boolean? = null

    var distance: Double? = null

    var donateSum: DonateSums? = null

    constructor(team: Team) : this() {
        this.id = team.id
        this.name = team.name
        this.event = team.event.id
        this.description = team.description
        this.members = ArrayList()
        team.members.forEach { this.members!!.add(BasicUserView(it)) }
        this.profilePic = MediaView(team.profilePic)
        this.invoiceId = team.invoice?.id
        this.hasStarted = team.hasStarted
        this.hasFullyPaid = team.invoice?.isFullyPaid()
        this.isFull = team.isFull()
    }

    constructor(team: Team, distance: Double, donateSum: DonateSums) : this() {
        this.id = team.id
        this.name = team.name
        this.event = team.event.id
        this.description = team.description
        this.members = ArrayList()
        team.members.forEach { this.members!!.add(BasicUserView(it)) }
        this.profilePic = MediaView(team.profilePic)
        this.invoiceId = team.invoice?.id
        this.hasStarted = team.hasStarted
        this.hasFullyPaid = team.invoice?.isFullyPaid()
        this.isFull = team.isFull()
        this.distance = distance
        this.donateSum = donateSum
    }
}
