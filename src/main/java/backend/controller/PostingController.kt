package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.model.media.MediaService
import backend.model.misc.Coord
import backend.model.posting.PostingService
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.util.getSignedJwtToken
import backend.util.localDateTimeOf
import backend.view.CommentView
import backend.view.LikeView
import backend.view.LocationView
import backend.view.PostingView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.*
import javax.validation.Valid

@RestController
@RequestMapping("/posting")
open class PostingController {

    private val mediaService: MediaService
    private val postingService: PostingService
    private val configurationService: ConfigurationService
    private val logger: Logger
    private var JWT_SECRET: String
    private var PAGE_SIZE: Int
    private val userService: UserService

    @Autowired
    constructor(postingService: PostingService,
                mediaService: MediaService,
                configurationService: ConfigurationService,
                userService: UserService) {

        this.postingService = postingService
        this.mediaService = mediaService
        this.configurationService = configurationService
        this.logger = LoggerFactory.getLogger(PostingController::class.java)
        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")
        this.PAGE_SIZE = configurationService.getRequired("org.breakout.api.page_size").toInt()
        this.userService = userService
    }


    /**
     * POST /posting/
     * Creates new posting
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createPosting(@Valid @RequestBody body: PostingView,
                           @AuthenticationPrincipal customUserDetails: CustomUserDetails): PostingView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)

        val postingLocation = body.postingLocation
        val locationCoord = when (postingLocation) {
            is LocationView -> Coord(postingLocation.latitude, postingLocation.longitude)
            else -> null
        }

        val clientDate = localDateTimeOf(body.date ?: throw RuntimeException("Client date has not been given"))

        val posting = postingService.createPosting(user, body.text, body.uploadMediaTypes, locationCoord, clientDate)
        posting.media.forEach { it.uploadToken = getSignedJwtToken(JWT_SECRET, it.id.toString()) }

        return PostingView(posting)
    }

    /**
     * GET /posting/{id}/
     * Gets posting by id
     */
    @RequestMapping("/{id}/", method = arrayOf(GET))
    open fun getPosting(@PathVariable("id") id: Long, @RequestParam(value = "userid", required = false) userId: Long?): PostingView {
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        return PostingView(posting.hasLikesBy(userId))
    }

    /**
     * DELETE /posting/{id}/
     * Allows Admin to delete Posting
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/{id}/", method = arrayOf(DELETE))
    open fun adminDeletePosting(@PathVariable("id") id: Long): Map<String, String> {
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        postingService.delete(posting)
        return mapOf("message" to "success")
    }

    /**
     * DELETE /posting/{id}/comment/{commentId}/
     * Allows Admin to delete Comment
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/{id}/comment/{commentId}/", method = arrayOf(DELETE))
    open fun adminDeleteComment(@PathVariable("id") postingId: Long,
                                @PathVariable("commentId") commentId: Long): Map<String, String> {

        val posting = postingService.getByID(postingId) ?: throw NotFoundException("Posting with id $postingId not found")
        postingService.removeComment(from = posting, id = commentId)

        return mapOf("message" to "success")
    }


    /**
     * GET /posting/
     * Gets all postings
     */
    @RequestMapping("/", method = arrayOf(GET))
    open fun getAllPostings(@RequestParam(value = "page", required = false) page: Int?,
                            @RequestParam(value = "userid", required = false) userId: Long?): Iterable<PostingView> {
        return postingService.findAll(page ?: 0, PAGE_SIZE).map { PostingView(it.hasLikesBy(userId)) }
    }

    /**
     * POST /posting/{id}/comment/
     * creates Comment for Posting
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/comment/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createComment(@PathVariable("id") id: Long,
                           @Valid @RequestBody body: CommentView,
                           @AuthenticationPrincipal customUserDetails: CustomUserDetails): CommentView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")

        val comment = postingService.addComment(
                to = posting,
                from = user.account,
                at = localDateTimeOf(body.date!!),
                withText = body.text)

        return CommentView(comment)
    }


    /**
     * POST /posting/{id}/like/
     * creates Like for Posting
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/like/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createLike(@PathVariable("id") id: Long,
                        @Valid @RequestBody body: LikeView,
                        @AuthenticationPrincipal customUserDetails: CustomUserDetails): LikeView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        val like = postingService.like(posting, user.account, localDateTimeOf(body.date!!))

        return LikeView(like)
    }


    /**
     * DELETE /posting/{id}/like/
     * creates Like for Posting
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/like/", method = arrayOf(DELETE))
    open fun deleteLike(@PathVariable("id") id: Long,
                        @AuthenticationPrincipal customUserDetails: CustomUserDetails): Map<String, String> {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        postingService.unlike(by = user.account, from = posting)

        return mapOf("message" to "success")
    }

    /**
     * GET /posting/{id}/like/
     * Gets Likes for Posting
     */
    @RequestMapping("/{id}/like/", method = arrayOf(GET))
    open fun getLikesForPosting(@PathVariable("id") id: Long): List<LikeView> {
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        val likes = posting.likes
        return likes.map(::LikeView)
    }

    /**
     * GET /posting/hashtag/{hashtag}/
     * Gets Likes for Posting
     */
    @RequestMapping("/hashtag/{hashtag}/", method = arrayOf(GET))
    open fun getPostingsByHashtag(@RequestParam(value = "page", required = false) page: Int?,
                                  @PathVariable("hashtag") hashtag: String,
                                  @RequestParam(value = "userid", required = false) userId: Long?): List<PostingView> {
        val posting = postingService.findByHashtag(hashtag, page ?: 0, PAGE_SIZE)
        return posting.map { PostingView(it.hasLikesBy(userId)) }
    }
}
