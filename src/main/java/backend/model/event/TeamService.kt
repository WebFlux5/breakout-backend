package backend.model.event

import backend.model.misc.EmailAddress
import backend.model.posting.Posting
import backend.model.user.Participant
import backend.model.user.User
import org.springframework.security.access.prepost.PreAuthorize

interface TeamService {

    /**
     * Create and save a new team
     *
     * @param creator: The creator and first of the two members of the team
     * @param name: The name of the team
     * @param description: The description of the team
     * @param event: The event a team belongs to
     *
     * @return The newly created Team
     */
    @Throws
    fun create(creator: Participant, name: String, description: String, event: Event): Team

    /**
     * Invite a participant to a team
     *
     * @param emailAddress: The email of participant to be invited
     * @param team: The team to invite the participant to
     */
    @Throws
    @PreAuthorize("#team.isMember(authentication.name)")
    open fun invite(emailAddress: EmailAddress, team: Team)

    open fun leave(team: Team, participant: Participant)

    /**
     * Persist a team to the database
     *
     * @param team: The team be be saved
     *
     * @return the saved team
     */
    @Throws
    fun save(team: Team): Team

    /**
     * Get Team from database
     *
     * @param id: The id of Team to get
     *
     * @return gotten team object
     */
    fun findOne(id: Long): Team?

    /**
     * Get Team Postings from database
     *
     * @param id: The id of Team to get postings for
     *
     * @return gotten postings list
     */
    fun findPostingsById(id: Long): List<Long>?

    /**
     * Get Team Postings from database only including location
     *
     * @param id: The id of Team to get postings for
     *
     * @return gotten postings list
     */
    fun findLocationPostingsById(id: Long): List<Posting>?

    /**
     * Get Team Posting from database with max distance
     *
     * @param id: The id of Team to get postings for
     *
     * @return gotten postings list
     */
    fun getPostingMaxDistanceById(id: Long): Posting?

    fun findInvitationsForUser(user: User): List<Invitation>

    fun findInvitationsByInviteCode(code: String): Invitation?

    fun getLinearDistanceForTeamFromPostings(teamId: Long): Double

    fun getActualDistanceForTeamFromPostings(teamId: Long): Double

}
