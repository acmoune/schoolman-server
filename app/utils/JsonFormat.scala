package utils

import java.time.{Instant, LocalDate, LocalTime, ZoneOffset}

import play.api.libs.json.{Format, JsString, JsSuccess, Reads, Writes}

object JsonFormat {
  val customLocalDateFormat: Format[LocalDate] = Format(
    Reads(js => JsSuccess(Instant.parse(js.as[String]).atZone(ZoneOffset.UTC).toLocalDate)),
    Writes(d => JsString(d.atTime(LocalTime.of(23, 0)).atZone(ZoneOffset.UTC).toInstant.toString))
  )
}
