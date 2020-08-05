package api.controllers.inputs

import play.api.libs.json.{Json, OFormat}

case class PasswordResetFields(email: String, oldPassword: String, newPassword: String)

object PasswordResetFields {
  implicit val passwordResetFieldsFormat: OFormat[PasswordResetFields] = Json.format[PasswordResetFields]
  def tupled = (PasswordResetFields.apply _).tupled
}
