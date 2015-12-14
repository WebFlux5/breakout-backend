package backend.controller

import backend.controller.RequestBodies.PostUserBody
import backend.controller.RequestBodies.PutUserBody
import backend.model.user.UserCore
import backend.model.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/user")
class UserController {

    @Autowired
    private lateinit var userService: UserService

    /**
     * POST /user/
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/", method = arrayOf(RequestMethod.POST), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun addUser(@Valid @RequestBody body: PostUserBody): ResponseEntity<kotlin.Any> {
        try {
            var user = userService.create(body);
            return ResponseEntity(mapOf<String, Long>("id" to user!!.core!!.id!!), HttpStatus.CREATED)
        } catch (e: Exception) {
            // TODO: This catches all exceptions but should only catch those thrown if email already exists!
            return ResponseEntity(errorResponse("user with email ${body.email!!} already exists"), HttpStatus.BAD_REQUEST)
        }
    }

    /**
     * GET /user/
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun showUsers(): MutableIterable<UserCore>? {
        return userService.getAllUsers();
    }

    /**
     * PUT /user/id/
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(
            value = "/{id}/",
            method = arrayOf(RequestMethod.PUT),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun updateUser(
            @PathVariable(value = "id") id: String,
            @Valid @RequestBody body: PutUserBody): ResponseEntity<kotlin.Any>? {

        val user = userService.getUserById(id.toLong())

        if(user == null) {
            return ResponseEntity(errorResponse("user with id $id does not exist"), HttpStatus.BAD_REQUEST)
        }

        if(body.firstname != null) user.firstname = body.firstname
        if(body.lastname != null) user.lastname = body.lastname
        if(body.gender != null) user.gender = body.gender
        if(body.isBlocked != null) user.isBlocked = body.isBlocked!!

        userService.save(user)

        return ResponseEntity(user, HttpStatus.OK)
    }

//    @ExceptionHandler(Exception::class)
//    fun handle(e: Exception) {
//        e.printStackTrace()
//    }

    data class errorResponse(var error: String)

}