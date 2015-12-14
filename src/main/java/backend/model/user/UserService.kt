package backend.model.user

import backend.controller.RequestBodies.PostUserBody

interface UserService {
    fun getUserById(id: Long): User?
    fun getUserByEmail(email: String): User?
    fun getAllUsers(): MutableIterable<UserCore>?;
    fun create(body: PostUserBody): User?
    fun save(user: User): User?
}