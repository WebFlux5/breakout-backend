package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.EventService
import backend.model.event.TeamService
import backend.model.location.LocationService
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.model.user.UserService
import backend.util.localDateTimeOf
import backend.view.LocationView
import backend.view.TeamLocationView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.POST
import javax.validation.Valid

@RestController
@RequestMapping("/event/{eventId}")
open class LocationController {

    private val locationService: LocationService
    private val teamService: TeamService
    private val eventService: EventService
    private val userService: UserService
    private val logger: Logger

    @Autowired
    constructor(locationService: LocationService,
                teamService: TeamService,
                eventService: EventService,
                userService: UserService) {

        this.locationService = locationService
        this.teamService = teamService
        this.eventService = eventService
        this.userService = userService
        this.logger = LoggerFactory.getLogger(LocationController::class.java)
    }

    /**
     * GET /event/{eventId}/location/
     * Return a list of all locations for a specific event
     */
    @RequestMapping("/location/", method = arrayOf(GET))
    open fun getAllLocationsForEvent(@PathVariable eventId: Long,
                                     @RequestParam(value = "perTeam", required = false) perTeam: Int?): Iterable<TeamLocationView> {
        return locationService.findByEventId(eventId, perTeam ?: 20).map { data ->
            TeamLocationView(data.key, data.value)
        }
    }

    /**
     * GET /event/{eventId}/team/{teamId}/location/
     * Return a list of all locations for a certain team at a certain event
     */
    @RequestMapping("/team/{teamId}/location/", method = arrayOf(GET))
    open fun getAllLocationsForEventAndTeam(@PathVariable("eventId") eventId: Long,
                                            @PathVariable("teamId") teamId: Long,
                                            @RequestParam(value = "perTeam", required = false) perTeam: Int?): Iterable<LocationView> {
        return locationService.findByTeamId(teamId, perTeam ?: 20).map(::LocationView)
    }

    /**
     * POST /event/{eventId}/team/{teamId}/location/
     * Upload a new location for a specific team at a specific event
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/team/{teamId}/location/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createLocation(@PathVariable("eventId") eventId: Long,
                            @PathVariable("teamId") teamId: Long,
                            @AuthenticationPrincipal customUserDetails: CustomUserDetails,
                            @Valid @RequestBody locationView: LocationView): LocationView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val participant = user.getRole(Participant::class) ?: throw UnauthorizedException("user is no participant")
        val team = teamService.findOne(teamId) ?: throw NotFoundException("no team with id $teamId found")
        if (!team.isMember(participant)) throw UnauthorizedException("user is not part of team $teamId are therefor cannot upload locations on it's behalf")

        val coord = Coord(locationView.latitude, locationView.longitude)
        val location = locationService.create(coord, participant, localDateTimeOf(epochSeconds = locationView.date))

        return LocationView(location)
    }

    /**
     * POST /event/{eventId}/team/{teamId}/location/multiple/
     * Upload multiple new locations for a specific team at a specific event
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/team/{teamId}/location/multiple/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createMultipleLocation(@PathVariable("eventId") eventId: Long,
                                    @PathVariable("teamId") teamId: Long,
                                    @AuthenticationPrincipal customUserDetails: CustomUserDetails,
                                    @Valid @RequestBody locationViews: List<LocationView>): List<LocationView> {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val participant = user.getRole(Participant::class) ?: throw UnauthorizedException("user is no participant")
        val team = teamService.findOne(teamId) ?: throw NotFoundException("no team with id $teamId found")
        if (!team.isMember(participant)) throw UnauthorizedException("user is not part of team $teamId are therefor cannot upload locations on it's behalf")

        val savedLocationsAsLocationViews = locationViews.map {
            val coord = Coord(it.latitude, it.longitude)
            val savedLocation = locationService.create(coord, participant, localDateTimeOf(epochSeconds = it.date))
            LocationView(savedLocation)
        }

        return savedLocationsAsLocationViews
    }

}
