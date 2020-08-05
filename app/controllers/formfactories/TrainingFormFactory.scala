package controllers.formfactories

import models.Training
import play.api.data._
import play.api.data.Forms._

case class TrainingFormData
(
  id: Option[Long] = None,
  programId: Long,
  title: String,
  description: String,
  requiredOptionalUnits: Int,
  prerequisites: Option[String] = None,
  qualifications: Option[String] = None,
  link: Option[String] = None,
  banner: Option[String] = None,
  position: Int = 0
) extends FormData

object TrainingFormFactory extends FormFactory[TrainingFormData, Training] {
  def emptyFormData: TrainingFormData = TrainingFormData(None, 0L, "", "", 0, None, None, None, None)

  def formDataFrom(t: Training): TrainingFormData =
    TrainingFormData(
      Some(t.id.value),
      t.programId.value,
      t.title,
      t.description,
      t.requiredOptionalUnits,
      t.prerequisites,
      t.qualifications,
      t.link,
      t.banner,
      t.position.getOrElse(0)
    )

  def form: Form[TrainingFormData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "programId" -> longNumber,
      "title" -> nonEmptyText(minLength = 4),
      "description" -> nonEmptyText,
      "requiredOptionalUnits" -> number,
      "prerequisites" -> optional(nonEmptyText),
      "qualifications" -> optional(nonEmptyText),
      "link" -> optional(nonEmptyText),
      "banner" -> optional(nonEmptyText),
      "position" -> number
    )(TrainingFormData.apply)(TrainingFormData.unapply)
  )
}
