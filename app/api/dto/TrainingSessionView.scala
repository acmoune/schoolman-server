package api.dto

import models._
import play.api.libs.json._

case class TrainingSessionView(value: TrainingSession, fees: Seq[TrainingSessionFee])

object TrainingSessionView {
  implicit val tsViewFormat: OFormat[TrainingSessionView] = Json.format[TrainingSessionView]
}
