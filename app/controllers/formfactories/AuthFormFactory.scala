package controllers.formfactories

import play.api.data._
import play.api.data.Forms._

case class SignInFormData(email: String, password: String)

object AuthForms {
  def signInForm: Form[SignInFormData] = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(SignInFormData.apply)(SignInFormData.unapply)
  )
}
