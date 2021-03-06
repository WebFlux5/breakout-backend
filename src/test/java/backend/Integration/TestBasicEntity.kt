package backend.Integration

import backend.model.event.Event
import backend.model.misc.Coord
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertNotNull


class TestBasicEntity : IntegrationTest() {

    @Test
    fun hasCreatedTimestamp() {
        val event = Event("Awesome Event", LocalDateTime.now(), "Munich", Coord(0.0, 0.0), 36)
        eventRepository.save(event)
        val foundEvent = eventRepository.findAll().first()
        assertNotNull(foundEvent.createdAt)
    }
}
