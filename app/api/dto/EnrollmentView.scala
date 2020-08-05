package api.dto

import java.time.LocalDate

import models.TrainingSessionState
import play.api.libs.json.{Json, OFormat}

case class EnrollmentView
(
  enrollmentId: Long,
  accountId: Long,
  sessionTitle: String,
  trainingTitle: String,
  planTitle: String,
  programTitle: String,
  departmentTitle: String,
  startDate: Option[LocalDate],
  duration: String,
  sessionStatus: TrainingSessionState.TrainingSessionState,
  bills: Seq[BillView]
)

object EnrollmentView {
  implicit val paymentViewFormat: OFormat[PaymentView] = Json.format[PaymentView]
  implicit val billViewFormat: OFormat[BillView] = Json.format[BillView]
  implicit val enrollmentViewFormat: OFormat[EnrollmentView] = Json.format[EnrollmentView]
}