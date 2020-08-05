package controllers.formfactories

import models.{Department, Program}
import play.api.data._
import play.api.data.Forms._

case class ProgramFormData
(
  id: Option[Long],
  departmentId: Long,
  title: String,
  description: String,
  logo: Option[String] = None,
  link: Option[String] = None,
  position: Int = 0
) extends FormData

object ProgramFormFactory extends FormFactory[ProgramFormData, Program] {
  def emptyFormData: ProgramFormData = ProgramFormData(None, 0L, "", "", None, None)

  def formDataFrom(prog: Program): ProgramFormData =
    ProgramFormData(
      id = Some(prog.id.value),
      departmentId = prog.departmentId.value,
      title = prog.title,
      description = prog.description,
      logo = prog.logo,
      link = prog.link,
      position = prog.position.getOrElse(0)
    )

  def form: Form[ProgramFormData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "departmentId" -> longNumber,
      "title" -> nonEmptyText(minLength = 4),
      "description" -> nonEmptyText,
      "logo" -> optional(nonEmptyText),
      "link" -> optional(nonEmptyText),
      "position" -> number
    )(ProgramFormData.apply)(ProgramFormData.unapply)
  )
}
