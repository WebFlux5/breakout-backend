package backend.view

import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.SponsoringStatus.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

class SponsoringView() {

    var id: Long? = null

    var eventId: Long? = null

    var teamId: Long? = null

    var team: String? = null

    @NotNull
    var amountPerKm: Double? = null

    @NotNull
    var limit: Double? = null

    var sponsorId: Long? = null

    var status: String? = null

    @Valid
    var unregisteredSponsor: UnregisteredSponsorView? = null

    constructor(sponsoring: Sponsoring) : this() {
        this.id = sponsoring.id
        this.eventId = sponsoring.team?.event?.id
        this.teamId = sponsoring.team?.id
        this.team = sponsoring.team?.name
        this.amountPerKm = sponsoring.amountPerKm.numberStripped.toDouble()
        this.limit = sponsoring.limit.numberStripped.toDouble()
        this.sponsorId = sponsoring.sponsor?.id

        this.status = when (sponsoring.status) {
            ACCEPTED -> "accepted"
            REJECTED -> "rejected"
            PROPOSED -> "proposed"
        }

        sponsoring.unregisteredSponsor?.let { this.unregisteredSponsor = UnregisteredSponsorView(it) }
    }
}

