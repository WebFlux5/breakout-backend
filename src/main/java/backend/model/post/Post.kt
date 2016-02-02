package backend.model.event

import backend.model.BasicEntity
import backend.model.misc.Coords
import backend.model.user.UserCore
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Post() : BasicEntity() {

    lateinit var text: String
    lateinit var date: LocalDateTime

    @Embedded
    lateinit var postLocation: Coords

    @ManyToOne
    var user: UserCore? = null

    constructor(text: String, postLocation: Coords, user: UserCore) : this() {
        this.text = text
        this.date = LocalDateTime.now()
        this.postLocation = postLocation
        this.user = user
    }

}