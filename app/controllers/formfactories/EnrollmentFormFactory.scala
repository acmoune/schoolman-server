package controllers.formfactories

import models._
import play.api.data._
import play.api.data.Forms._

case class EnrollmentFormData
(
  id: Option[Long] = None,
  accountId: Long,
  trainingSessionId: Long,
  notes: Option[String]
) extends FormData

object EnrollmentFormFactory extends FormFactory[EnrollmentFormData, Enrollment] {
  def emptyFormData: EnrollmentFormData = EnrollmentFormData(None, 0L, 0L, None)

  def formDataFrom(e: Enrollment): EnrollmentFormData = EnrollmentFormData(
    Some(e.id.value),
    e.accountId.value,
    e.trainingSessionId.value,
    e.notes
  )

  def form: Form[EnrollmentFormData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "accountId" -> longNumber,
      "trainingSessionId" -> longNumber,
      "notes" -> optional(text)
    )(EnrollmentFormData.apply)(EnrollmentFormData.unapply)
  )
}
