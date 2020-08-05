package api.dto

import models._
import play.api.libs.json._

case class DepartmentView(value: Department, programs: Seq[Program])

object DepartmentView {
  implicit val departmentViewFormat: OFormat[DepartmentView] = Json.format[DepartmentView]
}
