package backend.Integration

import backend.model.event.Event
import backend.model.event.Team
import backend.model.location.Location
import backend.model.location.Point
import backend.model.misc.Coord
import backend.model.posting.Posting
import backend.model.user.Participant
import backend.model.user.User
import backend.services.ConfigurationService
import com.auth0.jwt.Algorithm
import com.auth0.jwt.JWTSigner
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class TestTeamEndpoint : IntegrationTest() {

    @Autowired
    lateinit var configurationService: ConfigurationService
    lateinit var JWT_SECRET: String
    lateinit var event: Event
    lateinit var team: Team
    lateinit var creatorCredentials: Credentials
    lateinit var creator: User
    lateinit var inviteeCredentials: Credentials
    lateinit var invitee: User
    val APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8"


    @Before
    override fun setUp() {
        super.setUp()

        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")
        event = eventService.createEvent(
                title = "Breakout München",
                date = LocalDateTime.now(),
                city = "Munich",
                startingLocation = Coord(0.0, 0.0),
                duration = 36)

        creatorCredentials = createUser(this.mockMvc, userService = userService)
        inviteeCredentials = createUser(this.mockMvc, email = "invitee@mail.com", userService = userService)
        makeUserParticipant(creatorCredentials)
        makeUserParticipant(inviteeCredentials)
        creator = userRepository.findOne(creatorCredentials.id.toLong()).getRole(Participant::class)!!
        invitee = userRepository.findOne(inviteeCredentials.id.toLong())
        team = teamService.create(creator as Participant, "name", "description", event)

        val firstLocation = Location(Point(1.0, 1.0), creator.getRole(Participant::class)!!)
        val secondLocation = Location(Point(1.2, 2.0), creator.getRole(Participant::class)!!)

        postingService.save(Posting("test", firstLocation, creator.core, null, 156.899568))
        postingService.save(Posting("test", secondLocation, creator.core, null, 259.16669))

    }

    @Test
    fun testCreateTeam() {

        val body = mapOf("name" to "Team awesome", "description" to "Our team is awesome").toJsonString()

        val request = post("/event/${event.id}/team/")
                // TODO: Stop using inviteeCredentials just because creator already is part of a team because of setUp()
                // TODO: This is just a workaround and the tests should be refactored to be structured properly
                .header("Authorization", "Bearer ${inviteeCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.profilePic.type").exists())
                .andExpect(jsonPath("$.profilePic.id").exists())
                .andExpect(jsonPath("$.profilePic.uploadToken").exists())
                .andExpect(jsonPath("$.event").value(event.id!!.toInt()))
                .andExpect(jsonPath("$.name").value("Team awesome"))
                .andExpect(jsonPath("$.description").value("Our team is awesome"))
                .andExpect(jsonPath("$.members").isArray)
                .andExpect(jsonPath<MutableCollection<out Any>>("$.members", hasSize(1)))
                .andReturn().response.contentAsString

        print(response)
    }

    @Test
    fun testCreateTeamAndAddMediaSizesWithValidToken() {

        val postData = mapOf(
                "url" to "https://aws.amazon.com/bla.jpg",
                "width" to 400,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/media/${team.profilePic.id}/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to team.profilePic.id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .content(postData)

        val response = mockMvc.perform (request)
                .andExpect(status().isCreated)
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.width").exists())
                .andExpect(jsonPath("$.height").exists())
                .andExpect(jsonPath("$.length").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.type").exists())
                .andReturn().response.contentAsString


        println(response)

        val requestMedia = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/event/${event.id}/team/${team.id}/")
                .contentType(MediaType.APPLICATION_JSON)

        val responseMedia = mockMvc.perform (requestMedia)
                .andExpect(status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.members").exists())
                .andExpect(jsonPath("$.profilePic").exists())
                .andExpect(jsonPath("$.profilePic.id").exists())
                .andExpect(jsonPath("$.profilePic.type").exists())
                .andExpect(jsonPath("$.profilePic.sizes").exists())
                .andExpect(jsonPath("$.profilePic.sizes").isArray)
                .andExpect(jsonPath("$.profilePic.sizes[0]").exists())
                .andExpect(jsonPath("$.profilePic.sizes[0].id").exists())
                .andExpect(jsonPath("$.profilePic.sizes[0].url").exists())
                .andExpect(jsonPath("$.profilePic.sizes[0].width").exists())
                .andExpect(jsonPath("$.profilePic.sizes[0].height").exists())
                .andExpect(jsonPath("$.profilePic.sizes[0].length").exists())
                .andExpect(jsonPath("$.profilePic.sizes[0].size").exists())
                .andExpect(jsonPath("$.profilePic.sizes[0].type").exists())
                .andReturn().response.contentAsString

        println(responseMedia)
    }


    @Test
    fun failToCreateTeamIfUserIsNoParticipant() {
        val body = mapOf("name" to "Team awesome", "description" to "This team is awesome").toJsonString()

        val request = post("event/${event.id}/team")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        mockMvc.perform(request).andExpect(status().isNotFound)
    }

    @Test
    fun testGetTeamById() {
        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/event/${event.id}/team/${team.id}/")
                .contentType(MediaType.APPLICATION_JSON)

        val response = mockMvc.perform (request)
                .andExpect(status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.members").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun testGetTeamPostingsById() {
        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/event/${event.id}/team/${team.id}/posting/")
                .contentType(MediaType.APPLICATION_JSON)

        val response = mockMvc.perform (request)
                .andExpect(status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[1]").exists())
                .andExpect(jsonPath("$[2]").doesNotExist())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun testGetTeamDistanceById() {
        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/event/${event.id}/team/${team.id}/distance/")
                .contentType(MediaType.APPLICATION_JSON)

        val response = mockMvc.perform (request)
                .andExpect(status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.distance").exists())
                .andExpect(jsonPath("$.actualdistance").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun testInviteUser() {
        val body = mapOf("email" to invitee.email).toJsonString()

        val request = post("/event/${event.id}/team/${team.id}/invitation/")
                .header("Authorization", "Bearer ${creatorCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        mockMvc.perform(request)
                .andExpect(status().isCreated)
    }

    @Test
    fun joinTeam() {

        // TODO: Is this a good practice? How to do integration tests...
        testInviteUser()

        val body = mapOf("email" to invitee.email).toJsonString()
        val joinRequest = post("/event/${event.id}/team/${team.id}/member/")
                .header("Authorization", "Bearer ${inviteeCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        // Join team
        mockMvc.perform(joinRequest).andExpect(status().isCreated)
    }

    private fun makeUserParticipant(credentials: Credentials) {

        // Update user with role participant
        val json = mapOf(
                "firstname" to "Florian",
                "lastname" to "Schmidt",
                "gender" to "Male",
                "blocked" to false,
                "participant" to mapOf(
                        "tshirtsize" to "XL",
                        "hometown" to "Dresden",
                        "phonenumber" to "01234567890",
                        "emergencynumber" to "0987654321"
                )
        ).toJsonString()

        val request = put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(credentials.id))
                .andExpect(jsonPath("$.firstname").value("Florian"))
                .andExpect(jsonPath("$.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.blocked").value(false))
                .andExpect(jsonPath("$.participant").exists())
                .andExpect(jsonPath("$.participant.tshirtsize").value("XL"))
                .andExpect(jsonPath("$.participant.hometown").value("Dresden"))
                .andExpect(jsonPath("$.participant.phonenumber").value("01234567890"))
                .andExpect(jsonPath("$.participant.emergencynumber").value("0987654321"))
    }

}
