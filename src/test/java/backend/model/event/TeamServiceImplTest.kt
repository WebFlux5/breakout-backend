package backend.model.event

import backend.Integration.IntegrationTest
import backend.configuration.CustomUserDetailsService
import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserRole
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

class TeamServiceImplTest : IntegrationTest() {

    @Autowired
    private lateinit var userDetailsService: CustomUserDetailsService

    private lateinit var event: Event

    @Before
    override fun setUp() {
        super.setUp()
        event = eventService.createEvent("BreakOut München", LocalDateTime.now(), "München", Coord(0.0, 1.1), 36)
    }

    @Test
    fun testCreate() {
        val participant = User.create("f@x.de", "lorem").addRole(Participant::class)
        userService.save(participant)

        val team = teamService.create(participant, "Team Awesome", "Das beste Team aus Dresden", event)
        val savedTeam = teamRepository.findAll().first()

        assertNotNull(savedTeam)
        assertEquals(team.id, savedTeam.id)
        assertEquals(participant.core.id, savedTeam.members.first().core.id)
    }

    @Test
    fun testInvite() {
        val creator = setAuthenticatedUser("user@mail.com", Participant::class.java).getRole(Participant::class)!!
        val team = teamService.create(creator, "name", "description", event)

        teamService.invite(EmailAddress("invitee@mail.de"), team)
        // TODO: Make sure an Email is sent!
    }

    @Test
    fun failToInvite() {
        setAuthenticatedUser("user@mail.com", Participant::class.java).getRole(Participant::class)
        val creator = userService.create("not@mail.com", "password").addRole(Participant::class)
        val team = teamService.create(creator, "name", "description", event)

        assertFails { teamService.invite(EmailAddress("test@mail.com"), team) }
    }

    private fun setAuthenticatedUser(email: String, role: Class<out UserRole>): User {
        val user = userService.create(email, "password").addRole(role.kotlin)
        val details = userDetailsService.loadUserByUsername(email)!! // Not null because otherwise exception is thrown
        val token = UsernamePasswordAuthenticationToken(details.username, details.password, details.authorities)
        SecurityContextHolder.getContext().authentication = token
        return user
    }
}
