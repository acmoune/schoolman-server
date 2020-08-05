package api.controllers.inputs

import play.api.libs.json.{Json, OFormat}

case class TokenFields(token: String)

object TokenFields {
  implicit val tokenFieldsFormat: OFormat[TokenFields] = Json.format[TokenFields]
}
