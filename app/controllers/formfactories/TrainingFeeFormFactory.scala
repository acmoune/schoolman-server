package controllers.formfactories

import models.{FeeType, TrainingFee}
import play.api.data._
import play.api.data.format.Formats._
import play.api.data.Forms._

case class TrainingFeeFormData
(
  id: Option[Long] = None,
  trainingPlanId: Long,
  description: String,
  feeType: String,
  amount: Double,
  promotionalAmount: Option[Double] = None,
  optional: Boolean = false,
  position: Int = 0
) extends FormData

object TrainingFeeFormFactory extends FormFactory[TrainingFeeFormData, TrainingFee] {
  def emptyFormData: TrainingFeeFormData = TrainingFeeFormData(
    id = None,
    trainingPlanId = 0L,
    description = "",
    feeType = FeeType.Registration.toString,
    amount = 0,
    promotionalAmount = None
  )

  def formDataFrom(tf: TrainingFee): TrainingFeeFormData =
    TrainingFeeFormData(
      Some(tf.id.value),
      tf.trainingPlanId.value,
      tf.description,
      tf.feeType.toString,
      tf.amount,
      tf.promotionalAmount,
      tf.optional,
      tf.position.getOrElse(0)
    )

  def form: Form[TrainingFeeFormData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "trainingPlanId" -> longNumber,
      "description" -> nonEmptyText,
      "feeType" -> nonEmptyText,
      "amount" -> of(doubleFormat),
      "promotionalAmount" -> optional(of(doubleFormat)),
      "optional" -> boolean,
      "position" -> number
    )(TrainingFeeFormData.apply)(TrainingFeeFormData.unapply)
  )
}
