package controllers.formfactories

import java.time.LocalDate

import models.Payment
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

case class PaymentFormData
(
  id: Option[Long] = None,
  billId: Long,
  amount: Double,
  date: LocalDate
) extends FormData

object PaymentFormFactory extends FormFactory[PaymentFormData, Payment] {
  override def emptyFormData: PaymentFormData = PaymentFormData(None, 0L, 0.0, LocalDate.now)
  override def formDataFrom(p: Payment): PaymentFormData = PaymentFormData(Some(p.id.value), p.billId.value, p.amount, p.date)

  override def form: Form[PaymentFormData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "billId" -> longNumber,
      "amount" -> of(doubleFormat),
      "date" -> localDate
    )(PaymentFormData.apply)(PaymentFormData.unapply)
  )
}
