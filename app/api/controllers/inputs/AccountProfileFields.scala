package api.controllers.inputs

import java.time.LocalDate

import play.api.libs.json.{Json, OFormat}

case class AccountProfileFields
(
  id: Option[Long] = None,
  accountId: Long,

  birthDate: LocalDate,
  birthPlace: String,
  residence: String,
  phoneNumber: String,
  nationality: String,

  otherDetails: Option[String]
)

object AccountProfileFields {
  implicit val format: OFormat[AccountProfileFields] = Json.format[AccountProfileFields]
  def tupled = (AccountProfileFields.apply _).tupled
}
