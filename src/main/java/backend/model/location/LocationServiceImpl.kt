package backend.model.location

import backend.model.user.Participant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class LocationServiceImpl : LocationService {

    private val locationRepository: LocationRepository

    @Autowired
    constructor(locationRepository: LocationRepository) {
        this.locationRepository = locationRepository
    }

    override fun findAll(): Iterable<Location> {
        return locationRepository.findAll()
    }

    override fun save(location: Location): Location {
        return locationRepository.save(location)
    }

    @Transactional
    override fun create(point: Point, particpant: Participant, date: LocalDateTime): Location {
        val location = Location(point, particpant, date)
        return locationRepository.save(location)
    }

    override fun findByTeamId(id: Long): Iterable<Location> {
        return locationRepository.findByTeamId(id)
    }

}
