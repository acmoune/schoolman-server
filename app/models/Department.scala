package models

import play.api.libs.json._
import utils.PK

case class Department
(
  id: PK[Department] = PK[Department](0),
  title: String,
  position: Option[Int] = None
) extends Model

object Department {
  implicit val departmentFormat: OFormat[Department] = Json.format[Department]
  def tupled = (Department.apply _).tupled
}
