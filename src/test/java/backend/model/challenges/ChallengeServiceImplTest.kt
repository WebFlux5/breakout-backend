package backend.model.challenges

import backend.Integration.IntegrationTest
import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Address
import backend.model.user.Participant
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ChallengeServiceImplTest : IntegrationTest() {

    private lateinit var team: Team
    private lateinit var participant: Participant
    private lateinit var sponsor: Sponsor
    private lateinit var event: Event
    private lateinit var unregisteredSponsor: UnregisteredSponsor

    @Before
    override fun setUp() {
        super.setUp()
        event = eventService.createEvent("Title", LocalDateTime.now(), "Munich", Coord(0.0), 36)
        participant = userService.create("participant@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        sponsor = userService.create("sponsor@break-out.org", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        team = teamService.create(participant, "name", "description", event)
        unregisteredSponsor = UnregisteredSponsor(
                firstname = "firstname",
                lastname = "lastname",
                address = Address("1", "2", "3", "4", "5"),
                url = "www.test.de",
                company = "test",
                gender = "male",
                isHidden = false)
    }

    @Test
    fun testProposeChallengeRegisteredSponsor() {
        setAuthenticatedUser("sponsor@break-out.org")
        val challenge = challengeService.proposeChallenge(sponsor, team, Euro(50.0), "description")

        val found = challengeRepository.findOne(challenge.id)
        assertNotNull(found)
        assertEquals(sponsor.id, found.sponsor!!.id)
    }

    @Test
    fun testProposeChallenge1() {
        setAuthenticatedUser("participant@break-out.org")
        val challenge = challengeService.proposeChallenge(unregisteredSponsor, team, Euro(50.0), "description")

        val found = challengeRepository.findOne(challenge.id)
        assertNotNull(found)
        assertNotNull(found.unregisteredSponsor)
    }
}

fun Euro(value: Number): Money {
    return Money.of(value, "EUR")
}