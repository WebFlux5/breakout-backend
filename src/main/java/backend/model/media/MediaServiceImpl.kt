package backend.model.media

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MediaServiceImpl @Autowired constructor(val repository: MediaRepository, val mediaSizeRepository: MediaSizeRepository) : MediaService {

    override fun save(media: Media): Media = repository.save(media)

    override fun findAll(): Iterable<Media> = repository.findAll()

    override fun createMedia(type: String): Media {
        val media = Media(type)
        return repository.save(media)
    }

    override fun getByID(id: Long): Media? {
        return repository.findById(id)
    }

    override fun deleteSizes(media: Media) {
        media.sizes.clear()
        repository.save(media)
    }
}
