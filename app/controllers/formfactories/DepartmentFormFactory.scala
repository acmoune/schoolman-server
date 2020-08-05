package controllers.formfactories

import models.Department
import play.api.data._
import play.api.data.Forms._

case class DepartmentFormData(id: Option[Long], title: String, position: Int = 0) extends FormData

object DepartmentFormFactory extends FormFactory[DepartmentFormData, Department] {
  def emptyFormData: DepartmentFormData = DepartmentFormData(None, "")

  def formDataFrom(dept: Department): DepartmentFormData =
    DepartmentFormData(
      id = Some(dept.id.value),
      title = dept.title,
      position = dept.position.getOrElse(0)
    )

  override def form: Form[DepartmentFormData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText(minLength = 4),
      "position" -> number
    )(DepartmentFormData.apply)(DepartmentFormData.unapply)
  )
}
