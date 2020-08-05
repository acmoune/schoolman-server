package controllers.formfactories

import models.Bill
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

case class BillFormData
(
  id: Option[Long] = None,
  trainingSessionFeeId: Long,
  enrollmentId: Long,
  amount: Double
) extends FormData

object BillFormFactory extends FormFactory[BillFormData, Bill] {
  override def emptyFormData: BillFormData = BillFormData(
    id = None,
    trainingSessionFeeId = 0L,
    enrollmentId = 0L,
    amount = 0
  )

  override def formDataFrom(b: Bill): BillFormData = BillFormData(
    id = Some(b.id.value),
    trainingSessionFeeId = b.trainingSessionFeeId.value,
    enrollmentId = b.enrollmentId.value,
    amount = b.amount
  )

  override def form: Form[BillFormData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "trainingSessionFeeId" -> longNumber,
      "enrollmentId" -> longNumber,
      "amount" -> of(doubleFormat)
    )(BillFormData.apply)(BillFormData.unapply)
  )
}
