package backend.model.user

import backend.model.BasicEntity
import backend.model.media.Media
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import javax.persistence.DiscriminatorColumn
import javax.persistence.Entity
import javax.persistence.Inheritance
import javax.persistence.ManyToOne
import kotlin.reflect.KClass

@Entity
@Inheritance
@DiscriminatorColumn(name = "ROLE_NAME")
abstract class UserRole() : BasicEntity(), User, GrantedAuthority {

    @ManyToOne
    @JsonIgnore
    override lateinit var core: UserCore

    constructor(core: UserCore) : this() {
        this.core = core
    }

    // From here on: delegation of methods to core

    override var firstname: String?
        get() = this.core.firstname
        set(firstname) {
            this.core.firstname = firstname
        }

    override var lastname: String?
        get() = this.core.lastname
        set(lastname) {
            this.core.lastname = lastname
        }

    override var email: String
        get() = this.core.email
        set(email) {
            this.core.email = email
        }

    override var isBlocked: Boolean
        get() = this.core.isBlocked
        set(isBlocked) {
            this.core.isBlocked = isBlocked
        }

    override var passwordHash: String
        get() = this.core.passwordHash
        set(password) {
            this.core.passwordHash = password
        }

    override var gender: String?
        get() = this.core.gender
        set(gender) {
            this.core.gender = gender
        }

    override var profilePic: Media
        get() = this.core.profilePic
        set(profilePic) {
            this.core.profilePic = profilePic
        }

    override fun <T : UserRole> addRole(clazz: KClass<T>): T = this.core.addRole(clazz)
    override fun <T : UserRole> getRole(clazz: KClass<T>): T? = this.core.getRole(clazz)
    override fun <T : UserRole> hasRole(clazz: KClass<T>): Boolean = this.core.hasRole(clazz)
    override fun <T : UserRole> removeRole(clazz: KClass<T>): T? = this.core.removeRole(clazz)

    override fun activate(token: String) = this.core.activate(token)
    override fun isActivationTokenCorrect(token: String): Boolean = this.core.isActivationTokenCorrect(token)
    override fun createActivationToken(): String = this.core.createActivationToken()
    override fun isActivated(): Boolean = this.core.isActivated()

    companion object {

        @Throws(Exception::class)
        fun createFor(clazz: Class<out UserRole>, core: UserCore): UserRole {
            val o = clazz.newInstance();
            o.core = core;
            return o;
        }
    }
}
