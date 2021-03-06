package backend.model.messaging

import backend.model.BasicEntity
import backend.model.user.UserAccount
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class Message : BasicEntity {

    private constructor() : super()

    @ManyToOne
    lateinit var creator: UserAccount

    @Column(columnDefinition = "TEXT")
    lateinit var text: String

    lateinit var date: LocalDateTime

    constructor(creator: UserAccount, text: String, date: LocalDateTime) : this() {
        this.creator = creator
        this.text = text
        this.date = date
    }
}
