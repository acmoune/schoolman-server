package api.dto

import java.time.LocalDate
import models._
import play.api.libs.json._
import utils.PK

case class AccountProfileView
(
  id: PK[AccountProfile],
  accountId: PK[Account],

  birthDate: LocalDate,
  birthPlace: String,
  residence: String,
  phoneNumber: String,
  nationality: String,

  otherDetails: Option[String]
)

object AccountProfileView {
  implicit val accountProfileViewFormat: OFormat[AccountProfileView] = Json.format[AccountProfileView]
}
