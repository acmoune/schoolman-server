package api.dto

import models.FeeType

case class BillView
(
  billId: Long,
  feeType: FeeType.FeeType,
  description: String,
  amount: Double,
  payments: Seq[PaymentView]
)
