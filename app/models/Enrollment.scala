package models

import utils.PK

case class Enrollment
(
  id: PK[Enrollment] = PK[Enrollment](0L),
  accountId: PK[Account],
  trainingSessionId: PK[TrainingSession],
  notes: Option[String]
) extends Model
