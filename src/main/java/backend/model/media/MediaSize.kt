package backend.model.media

import backend.model.BasicEntity
import javax.persistence.*

@Entity
class MediaSize : BasicEntity {

    private constructor() : super()

    @ManyToOne
    var media: Media? = null

    lateinit var url: String

    var width: Int? = null

    var height: Int? = null

    var length: Int? = null

    var size: Long? = null

    @Enumerated(EnumType.STRING)
    var mediaType: MediaType? = null

    constructor(media: Media, url: String, width: Int, height: Int, length: Int, size: Long, type: String) : this() {
        this.media = media
        this.media?.sizes?.add(this)
        this.url = url
        this.width = width
        this.height = height
        this.length = length
        this.size = size
        this.mediaType = MediaType.valueOf(type.toUpperCase())
    }

    @PreRemove
    fun preRemove() {
        this.media?.sizes?.remove(this)
    }
}
