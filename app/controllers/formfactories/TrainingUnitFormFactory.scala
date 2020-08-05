package controllers.formfactories

import models.TrainingUnit
import play.api.data._
import play.api.data.Forms._

case class TrainingUnitFormData
(
  id: Option[Long] = None,
  trainingId: Long,
  title: String,
  syllabus: String,
  optional: Boolean,
  position: Int = 0
) extends FormData

object TrainingUnitFormFactory extends FormFactory[TrainingUnitFormData, TrainingUnit] {
  def emptyFormData: TrainingUnitFormData = TrainingUnitFormData(id=None, trainingId=0L, title="", syllabus="", optional = false)

  def formDataFrom(tu: TrainingUnit): TrainingUnitFormData =
    TrainingUnitFormData(Some(tu.id.value), tu.trainingId.value, tu.title, tu.syllabus, tu.optional, tu.position.getOrElse(0))

  def form: Form[TrainingUnitFormData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "trainingId" -> longNumber,
      "title" -> nonEmptyText(minLength = 4),
      "syllabus" -> nonEmptyText,
      "optional" -> boolean,
      "position" -> number
    )(TrainingUnitFormData.apply)(TrainingUnitFormData.unapply)
  )
}
