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

    @NotNull
    val date: Long

    constructor(location: Location) {
        this.latitude = location.point.latitude
        this.longitude = location.point.longitude
        this.date = location.date.toEpochSecond(ZoneOffset.UTC)
    }

    @JsonCreator
    constructor(@JsonProperty("latitude") latitude: Double, @JsonProperty("longitude") longitude: Double, @JsonProperty("date") date: Long) {
        this.latitude = latitude
        this.longitude = longitude
        this.date = date
    }
}
