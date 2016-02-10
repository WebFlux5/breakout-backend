package backend.model.post

import backend.model.BasicEntity
import backend.model.misc.Coords
import backend.model.user.UserCore
import org.jetbrains.annotations.Nullable
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class Post() : BasicEntity() {

    var text: String? = null

    lateinit var date: LocalDateTime

    @Embedded
    @AttributeOverrides(
            AttributeOverride(name = "latitude", column = Column(nullable = true)),
            AttributeOverride(name = "longitude", column = Column(nullable = true))
    )
    var postLocation: Coords? = null

    @ManyToOne
    var user: UserCore? = null

    @OrderColumn
    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    var media: MutableList<Media>? = ArrayList()


    constructor(text: String?, postLocation: Coords?, user: UserCore, media: MutableList<Media>?) : this() {
        this.text = text
        this.date = LocalDateTime.now()
        this.postLocation = postLocation
        this.user = user
        this.media = media
    }

}
