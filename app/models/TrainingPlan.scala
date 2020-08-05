package models

import play.api.libs.json._
import utils.PK

case class TrainingPlan
(
  id: PK[TrainingPlan] = PK[TrainingPlan](0L),
  trainingId: PK[Training],
  planId: PK[Plan],
  duration: String,
) extends Model

object TrainingPlan {
  implicit val tpFormat: OFormat[TrainingPlan] = Json.format[TrainingPlan]
  def tupled = (TrainingPlan.apply _).tupled
}
