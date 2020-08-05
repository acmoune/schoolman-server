package models

import play.api.libs.json._
import utils.PK

case class Program
(
  id: PK[Program] = PK[Program](0L),
  departmentId: PK[Department],
  title: String,
  description: String,
  logo: Option[String] = None,
  link: Option[String] = None,
  position: Option[Int] = None
) extends Model

object Program {
  implicit val programFormat: OFormat[Program] = Json.format[Program]
  def tupled = (Program.apply _).tupled
}
