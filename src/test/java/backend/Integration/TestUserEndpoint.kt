package backend.Integration

import backend.model.user.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*
import kotlin.collections.mapOf
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.text.toByteArray

class TestUserEndpoint : IntegrationTest() {

    private fun url(): String = "/user/"

    private fun url(id: Int): String = "/user/${id.toString()}/"

    @Autowired
    lateinit var userService: UserService

    @Before
    override fun setUp() = super.setUp() // this will delete all users from the test database

    // TODO: Restricted Access based on roles

    /**
     * GET /user/
     */
    @Test
    fun getUser() {
        userService.create(getDummyPostUserBody())
        userService.create(getDummyPostUserBody())
        mockMvc.perform(get("/user/"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0].firstname").exists())
                .andExpect(jsonPath("$[0].lastname").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].gender").exists())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[1].firstname").exists())
                .andExpect(jsonPath("$[1].lastname").exists())
                .andExpect(jsonPath("$[1].email").exists())
                .andExpect(jsonPath("$[1].gender").exists())
                .andExpect(jsonPath("$[1].passwordHash").doesNotExist())
    }

    /**
     * POST /user/
     * Create user with email and password
     */
    @Test
    fun postUser() {

        val json = mapOf(
                "email" to "a@x.de",
                "password" to "password"
        ).toJsonString()

        mockMvc.perform(post(url(), json))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())

        val user = userRepository.findByEmail("a@x.de")
        assertNotNull(user)
        assertEquals(user.email, "a@x.de")
    }

    /**
     * POST /user/
     * Reject invalid email
     */
    @Test
    fun postUserRejectInvalidEmail() {

        val json = mapOf(
                "email" to "asd.de",
                "password" to "password"
        ).toJsonString()

        mockMvc.perform(post(url(), json))
                .andExpect(status().isBadRequest)
                .andExpect(content().string(""))
    }

    /**
     * POST /user/
     * Reject existing email
     */
    @Test
    fun postUserRejectExistingEmail() {
        val json = mapOf(
                "email" to "a@x.de",
                "password" to "password"
        ).toJsonString()

        mockMvc.perform(post(url(), json))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())


        mockMvc.perform(post(url(), json))
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("user with email a@x.de already exists"))
    }

    /**
     * PUT /user/:id/
     * Modify the data of a user
     */
    @Test
    fun putUserId() {

        val credentials = createUser()

        // Update user
        val json = mapOf(
                "firstname" to "Florian",
                "lastname" to "Schmidt",
                "gender" to "Male",
                "blocked" to true
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(credentials.id))
                .andExpect(jsonPath("$.email").value("a@x.de"))
                .andExpect(jsonPath("$.firstname").value("Florian"))
                .andExpect(jsonPath("$.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.blocked").value(false)) // Expect that a user can't block itself!

        // TODO: Check that some values such as passwordHash aren't shown!
        // TODO: Test response if user does not exist
        // TODO: Can't override existing properties with null!
    }

    @Test
    fun makeUserParticipant() {

        val credentials = createUser()

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

        val request = MockMvcRequestBuilders
                .put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(credentials.id))
                .andExpect(jsonPath("$.id").value(credentials.id))
                .andExpect(jsonPath("$.email").value("a@x.de"))
                .andExpect(jsonPath("$.firstname").value("Florian"))
                .andExpect(jsonPath("$.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.blocked").value(false))
                .andExpect(jsonPath("$.participant").exists())
                .andExpect(jsonPath("$.participant.tshirtsize").value("XL"))
                .andExpect(jsonPath("$.participant.hometown").value("Dresden"))
                .andExpect(jsonPath("$.participant.phonenumber").value("01234567890"))
                .andExpect(jsonPath("$.participant.emergencynumber").value("0987654321"))
                .andReturn().response.contentAsString
    }

    @Test
    fun failToMakeUserParticipantIfUnauthorized() {

        val credentials = createUser()
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

        val request = MockMvcRequestBuilders
                .put("/user/${credentials.id}/")
                .header("Authorization", "Bearer thisIsAnInvalidAccessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun getUserId() {

        // Create user
        var json = mapOf(
                "email" to "a@x.de",
                "password" to "password",
                "firstname" to "Florian",
                "lastname" to "Schmidt"
        ).toJsonString()

        val resultPut = mockMvc.perform(post(url(), json))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andReturn()

        val response: Map<String, kotlin.Any> = ObjectMapper()
                .reader(Map::class.java)
                .readValue(resultPut.response.contentAsString)

        val id = response["id"] as Int

        mockMvc.perform(get(url(id)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.firstname").exists())
                .andExpect(jsonPath("$.lastname").exists())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
    }

    private fun createUser(): Credentials {
        // Create user
        val json = mapOf(
                "email" to "a@x.de",
                "password" to "password"
        ).toJsonString()

        val createResponseString = mockMvc.perform(post(url(), json))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andReturn().response.contentAsString

        val createResponse: Map<String, kotlin.Any> = ObjectMapper()
                .reader(Map::class.java)
                .readValue(createResponseString)

        val id = createResponse["id"] as Int

        val credentials = Base64.getEncoder().encodeToString("breakout_app:123456789".toByteArray())

        val request = MockMvcRequestBuilders
                .post("/oauth/token")
                .param("password", "password")
                .param("username", "a@x.de")
                .param("scope", "read write")
                .param("grant_type", "password")
                .param("client_secret", "123456789")
                .param("client_id", "breakout_app")
                .header("Authorization", "Basic $credentials")
                .accept(MediaType.APPLICATION_JSON_VALUE)

        val oauthResponseString = mockMvc.perform(request).andReturn().response.contentAsString
        val oauthResponse: Map<String, kotlin.Any> = ObjectMapper()
                .reader(Map::class.java)
                .readValue(oauthResponseString)

        val accessToken = oauthResponse["access_token"] as String
        val refreshToken = oauthResponse["refresh_token"] as String

        return Credentials(id, accessToken, refreshToken)

    }

    class Credentials(val id: Int, val accessToken: String, val refreshToken: String)

}
