package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.exceptions.DomainException
import backend.model.event.TeamSummaryProjection
import backend.model.event.TeamService
import backend.model.user.Participant
import backend.model.user.UserService
import backend.view.TeamEntryFeeInvoiceView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/team")
open class TeamControllerV2 @Autowired constructor(
        val userService: UserService,
        val teamService: TeamService
) {


    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{teamId}/startingfee", method = arrayOf(GET))
    open fun getInvoiceForTeam(@PathVariable teamId: Long,
                               @AuthenticationPrincipal customUserDetails: CustomUserDetails): TeamEntryFeeInvoiceView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val team = teamService.findOne(teamId) ?: throw NotFoundException("Team with id $teamId not found")

        user.getRole(Participant::class)?.let {
            if (team.isMember(it)) {
                val invoice = team.invoice ?: throw DomainException("Team has no invoice")
                return TeamEntryFeeInvoiceView(invoice)
            } else {
                throw UnauthorizedException("Participant is not a member of team $teamId")
            }
        }

        throw UnauthorizedException("User is no participant")
    }

    @RequestMapping("/", method = arrayOf(GET))
    open fun getAllTeamsOverview(): Iterable<TeamSummaryProjection> {
        val teams = teamService.findAllTeamSummaryProjections()
        return teams
    }
}
