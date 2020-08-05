package models

import play.api.libs.json._
import utils.PK

case class TrainingSessionFee
(
  id: PK[TrainingSessionFee] = PK[TrainingSessionFee](0L),
  trainingSessionId: PK[TrainingSession],
  description: String,
  feeType: FeeType.Value,
  amount: Double = 0,
  promotionalAmount: Option[Double] = None,
  optional: Boolean = false,
  position: Option[Int] = None
)

object TrainingSessionFee {
  implicit val tsfFormat: OFormat[TrainingSessionFee] = Json.format[TrainingSessionFee]
  def tupled = (TrainingSessionFee.apply _).tupled
}
