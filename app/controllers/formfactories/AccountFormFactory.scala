package controllers.formfactories

import models.{Account, AccountRole}
import play.api.data._
import play.api.data.Forms._

case class AccountFormData
(
  id: Option[Long],
  email: String,
  password: String,
  passwordConfirmation: String,
  role: String,
  locked: Boolean,
  fullName: String
) extends FormData

case class AccountDetailsFormData(id: Long, email: String, fullName: String, role: String)
case class AccountPasswordFormData(id: Long, newPassword: String, passwordConfirmation: String)

object AccountFormFactory extends FormFactory[AccountFormData, Account] {
  def emptyFormData: AccountFormData =
    AccountFormData(None, "", "", "", AccountRole.Student.toString, locked = false, "")

  def formDataFrom(a: Account): AccountFormData =
    AccountFormData(Some(a.id.value), a.email, a.password, "", a.role.toString, a.locked, a.fullName)

  def form: Form[AccountFormData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "email" -> email,
      "password" -> nonEmptyText(minLength = 6),
      "passwordConfirmation" -> nonEmptyText(minLength = 6),
      "role" -> nonEmptyText,
      "locked" -> boolean,
      "fullName" -> nonEmptyText
    )(AccountFormData.apply)(AccountFormData.unapply)
      .verifying("Password and confirmation don't match.", acc => acc.password == acc.passwordConfirmation)
  )
}

object AccountForms {
  val detailsForm: Form[AccountDetailsFormData] = Form(
    mapping(
      "id" -> longNumber,
      "email" -> email,
      "fullName" -> nonEmptyText,
      "role" -> nonEmptyText
    )(AccountDetailsFormData.apply)(AccountDetailsFormData.unapply)
  )

  val passwordAdminForm: Form[AccountPasswordFormData] = Form(
    mapping(
      "id" -> longNumber,
      "newPassword" -> nonEmptyText,
      "passwordConfirmation" -> nonEmptyText
    )(AccountPasswordFormData.apply)(AccountPasswordFormData.unapply)
      .verifying("Password and confirmation don't match.", acc => acc.newPassword == acc.passwordConfirmation)
  )
}
