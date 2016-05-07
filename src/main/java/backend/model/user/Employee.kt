@file:JvmName("Employee")

package backend.model.user

import javax.persistence.*

@Entity
@DiscriminatorValue("EMPLOYEE")
class Employee : UserRole {

    @Embedded
    var address: Address? = null

    @Column(name = "emp_tshirtsize")
    var tshirtSize: String? = null
    var title: String? = null
    var phonenumber: String? = null

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(core: UserCore) : super(core) {
    }

    override fun getAuthority(): String = "EMPLOYEE"
}
