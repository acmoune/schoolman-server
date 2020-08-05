package api.controllers.inputs

import play.api.libs.json.{Json, OFormat}

case class Credentials(email: String, password: String)

object Credentials {
  implicit val credentialsFormat: OFormat[Credentials] = Json.format[Credentials]
  def tupled = (Credentials.apply _).tupled
}
