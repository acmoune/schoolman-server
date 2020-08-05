package api.dto

import models._
import play.api.libs.json.{Json, OFormat}

case class TrainingPlanView
(
  value: TrainingPlan,
  title: String,
  description: String,
  fees: Seq[TrainingFee],
  currentSessions: Seq[TrainingSessionView]
)

object TrainingPlanView {
  implicit val tpViewFormat: OFormat[TrainingPlanView] = Json.format[TrainingPlanView]
}
