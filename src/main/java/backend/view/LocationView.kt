package backend.view

import backend.model.location.Location
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.validation.constraints.NotNull

class LocationView {

    @NotNull
    val latitude: Double

    @NotNull
    val longitude: Double

    var distance: Double? = null

    @NotNull
    val date: Long

    constructor(location: Location) {
        this.latitude = location.coord.latitude
        this.longitude = location.coord.longitude
        this.distance = location.distance
        this.date = location.date.toEpochSecond(ZoneOffset.UTC)
    }

    @JsonCreator
    constructor(@JsonProperty("latitude") latitude: Double, @JsonProperty("longitude") longitude: Double, @JsonProperty("date") date: Long) {
        this.latitude = latitude
        this.longitude = longitude
        this.date = date
    }
}
