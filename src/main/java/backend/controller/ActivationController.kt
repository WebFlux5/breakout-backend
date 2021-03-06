package backend.controller

import backend.controller.exceptions.NotFoundException
import backend.model.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/activation")
class ActivationController {

    private val userService: UserService

    @Autowired
    constructor(userService: UserService) {
        this.userService = userService
    }

    /**
     * GET /activation?={token}
     * Activates account with given token
     */
    @RequestMapping(method = arrayOf(GET))
    fun activateAccount(@RequestParam token: String): Map<String, String> {

        val user = userService.getUserByActivationToken(token) ?:
                throw NotFoundException("No user with token $token")

        userService.activate(user, token)
        return mapOf("message" to "success")
    }
}
