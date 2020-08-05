package models

import play.api.libs.json._
import utils.PK

case class Plan
(
  id: PK[Plan] = PK[Plan](0L),
  programId: PK[Program],
  title: String,
  description: String
) extends Model

object Plan {
  implicit val planFormat: OFormat[Plan] = Json.format[Plan]
  def tupled = (Plan.apply _).tupled
}
