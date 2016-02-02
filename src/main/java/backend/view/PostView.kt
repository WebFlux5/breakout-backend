package backend.view

import backend.model.event.Post
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

class PostView() {

    var id: Long? = null

    @NotNull
    var text: String? = null

    var date: Long? = null

    @Valid
    var postLocation: PostView.Coords? = null

    @Valid
    var user: UserView? = null

    constructor(post: Post) : this() {
        this.id = post.id
        this.text = post.text
        this.date = post.date.toEpochSecond(ZoneOffset.UTC)
        this.postLocation = PostView.Coords()
        this.postLocation!!.latitude = post.postLocation.latitude
        this.postLocation!!.longitude = post.postLocation.longitude
        this.user = UserView(post.user!!.core)
    }

    class Coords() {

        @NotNull
        var latitude: Double? = null

        @NotNull
        var longitude: Double? = null
    }

}