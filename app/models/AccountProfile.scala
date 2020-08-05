package models

import java.time.LocalDate

import play.api.libs.json.{Json, OFormat}
import utils.PK
import utils.EnumUtils

case class AccountProfile
(
  id: PK[AccountProfile] = PK[AccountProfile](0L),
  accountId: PK[Account],

  birthDate: LocalDate,
  birthPlace: String,
  residence: String,
  phoneNumber: String,
  nationality: String,

  otherDetails: Option[String]
) extends Model

object AccountProfile {
  implicit val accountProfileFormat: OFormat[AccountProfile] = Json.format[AccountProfile]
  def tupled = (AccountProfile.apply _).tupled
}
