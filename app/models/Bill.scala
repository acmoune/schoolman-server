package models

import utils.PK

case class Bill
(
  id: PK[Bill] = PK[Bill](0L),
  trainingSessionFeeId: PK[TrainingSessionFee],
  enrollmentId: PK[Enrollment],
  amount: Double = 0,
) extends Model
