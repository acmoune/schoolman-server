package api.dto

import models._
import play.api.libs.json._

case class TrainingView(value: Training, units: Seq[TrainingUnit], plans: Seq[TrainingPlanView])

object TrainingView {
  implicit val trainingViewFormat: OFormat[TrainingView] = Json.format[TrainingView]
}
