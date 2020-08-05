package models

import play.api.libs.json._
import utils.{EnumUtils, PK}

object FeeType extends Enumeration {
  type FeeType = Value
  val Registration = Value("Registration")
  val Tuition = Value("Tuition")
  val Exam = Value("Exam")

  implicit val feeTypeFormat = EnumUtils.enumFormat(FeeType)
}

import FeeType.FeeType

case class TrainingFee
(
  id: PK[TrainingFee] = PK[TrainingFee](0L),
  trainingPlanId: PK[TrainingPlan],
  description: String,
  feeType: FeeType,
  amount: Double = 0,
  promotionalAmount: Option[Double] = None,
  optional: Boolean = false,
  position: Option[Int] = None
) extends Model

object TrainingFee {
  implicit val tsfFormat: OFormat[TrainingFee] = Json.format[TrainingFee]
  def tupled = (TrainingFee.apply _).tupled
}
