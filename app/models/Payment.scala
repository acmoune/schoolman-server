package models

import java.time.LocalDate

import utils.PK

case class Payment
(
  id: PK[Payment] = PK[Payment](0L),
  billId: PK[Bill],
  amount: Double,
  date: LocalDate
) extends Model
