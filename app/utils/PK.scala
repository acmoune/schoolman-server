package utils

import play.api.libs.json.{Json, OFormat}
import slick.lifted.MappedTo

final case class PK[A](value: Long) extends AnyVal with MappedTo[Long]

object PK {
  implicit def pkFormat[A]: OFormat[PK[A]] = Json.format[PK[A]]
}
