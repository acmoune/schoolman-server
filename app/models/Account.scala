package models

import utils.PK

object AccountRole extends Enumeration {
  type AccountRole = Value
  val Student = Value("Student")
  val Administrator = Value("Administrator")
  val Manager = Value("Manager")
}

import AccountRole.AccountRole

case class Account
(
  id: PK[Account] = PK[Account](0L),
  email: String,
  password: String,
  role: AccountRole = AccountRole.Student,
  locked: Boolean = false,
  fullName: String
) extends Model

case class AccountDetails (email: String, role: String, fullName: String)
