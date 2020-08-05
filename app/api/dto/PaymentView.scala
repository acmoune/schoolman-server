package api.dto

import java.time.LocalDate

case class PaymentView(paymentId: Long, amount: Double, date: LocalDate)
