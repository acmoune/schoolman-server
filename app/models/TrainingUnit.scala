package models

import play.api.libs.json._
import utils.PK

case class TrainingUnit
(
  id: PK[TrainingUnit] = PK[TrainingUnit](0L),
  trainingId: PK[Training],
  title: String,
  syllabus: String,
  optional: Boolean = false,
  position: Option[Int] = None
) extends Model

object TrainingUnit {
  implicit val tuFormat: OFormat[TrainingUnit] = Json.format[TrainingUnit]
  def tupled = (TrainingUnit.apply _).tupled
}
