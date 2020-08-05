package controllers.formfactories

import java.time.LocalDate

import models._
import play.api.data._
import play.api.data.Forms._

case class AccountProfileFormData
(
  id: Option[Long] = None,
  accountId: Long,

  birthDate: LocalDate,
  birthPlace: String,
  residence: String,
  phoneNumber: String,
  nationality: String,

  otherDetails: Option[String]
) extends FormData

object AccountProfileFormFactory extends FormFactory[AccountProfileFormData, AccountProfile] {
  def emptyFormData: AccountProfileFormData = AccountProfileFormData(
    id = None,
    accountId = 0L,

    birthDate = LocalDate.now,
    birthPlace = "",
    residence = "",
    phoneNumber = "",
    nationality = "",

    otherDetails = None
  )

  def formDataFrom(a: AccountProfile): AccountProfileFormData = AccountProfileFormData(
    id = Some(a.id.value),
    accountId = a.accountId.value,

    birthDate = a.birthDate,
    birthPlace = a.birthPlace,
    residence = a.residence,
    phoneNumber = a.phoneNumber,
    nationality = a.nationality,

    otherDetails = a.otherDetails
  )

  def form: Form[AccountProfileFormData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "accountId" -> longNumber,

      "birthDate" -> localDate,
      "birthPlace" -> nonEmptyText,
      "residence" -> nonEmptyText,
      "phoneNumber" -> nonEmptyText,
      "nationality" -> nonEmptyText,

      "otherDetails" -> optional(text)
    )(AccountProfileFormData.apply)(AccountProfileFormData.unapply)
  )
}
