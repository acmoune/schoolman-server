package models

import play.api.libs.json._
import utils.PK

case class Training
(
  id: PK[Training] = PK[Training](0L),
  programId: PK[Program],
  title: String,
  description: String,
  requiredOptionalUnits: Int = 0,
  prerequisites: Option[String] = None,
  qualifications: Option[String] = None,
  link: Option[String] = None,
  banner: Option[String] = None,
  position: Option[Int] = None
) extends Model

object Training {
  implicit val trainingFormat: OFormat[Training] = Json.format[Training]
  def tupled = (Training.apply _).tupled
}
