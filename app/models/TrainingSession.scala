package models

import java.time.LocalDate

import play.api.libs.json._
import utils.{EnumUtils, PK}

object TrainingSessionState extends Enumeration {
  type TrainingSessionState = Value
  val Opened = Value("Opened")
  val Closed = Value("Closed")
  val Finished = Value("Finished")

  implicit val trainingSessionStateFormat = EnumUtils.enumFormat(TrainingSessionState)
}

case class TrainingSession
(
  id: PK[TrainingSession] = PK[TrainingSession](0L),
  trainingPlanId: PK[TrainingPlan],
  title: String,
  startDate: Option[LocalDate] = None,
  status: TrainingSessionState.TrainingSessionState = TrainingSessionState.Opened
) extends Model

object TrainingSession {
  implicit val tsFormat: OFormat[TrainingSession] = Json.format[TrainingSession]
  def tupled = (TrainingSession.apply _).tupled
}
