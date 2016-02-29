package backend.model.event

import backend.model.misc.Coord
import backend.model.posting.Posting
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EventServiceImpl @Autowired constructor(val repository: EventRepository) : EventService {

    override fun findAll(): Iterable<Event> = repository.findAll()

    override fun createEvent(title: String, date: LocalDateTime, city: String, startingLocation: Coord, duration: Int): Event {
        val event = Event(title, date, city, startingLocation, duration)
        return repository.save(event)
    }

    override fun findPostingsById(id: Long): List<Long>? = repository.findPostingsById(id)

    override fun findLocationPostingsById(id: Long): List<Posting>? = repository.findLocationPostingsById(id)
}
