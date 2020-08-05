package controllers.formfactories

import java.time.LocalDate

import models.{TrainingSession, TrainingSessionState}
import play.api.data._
import play.api.data.format.Formats._
import play.api.data.Forms._

case class TrainingSessionFormData
(
  id: Option[Long] = None,
  trainingPlanId: Long,
  title: String,
  startDate: Option[LocalDate] = None,
  status: String
) extends FormData

object TrainingSessionFormFactory extends FormFactory[TrainingSessionFormData, TrainingSession] {
  def emptyFormData: TrainingSessionFormData =
    TrainingSessionFormData(
      id = None,
      trainingPlanId = 0L,
      title = "",
      startDate = None,
      status = TrainingSessionState.Opened.toString
    )

  def formDataFrom(tss: TrainingSession): TrainingSessionFormData =
    TrainingSessionFormData(
      id = Some(tss.id.value),
      trainingPlanId = tss.trainingPlanId.value,
      title = tss.title,
      startDate = tss.startDate,
      status = tss.status.toString
    )

  def form: Form[TrainingSessionFormData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "trainingPlanId" -> longNumber,
      "title" -> nonEmptyText,
      "startDate" -> optional(of(localDateFormat)),
      "status" -> nonEmptyText
    )(TrainingSessionFormData.apply)(TrainingSessionFormData.unapply)
  )
}
