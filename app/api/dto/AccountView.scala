package api.dto

import models._
import play.api.libs.json._
import utils.PK

case class AccountView(id: PK[Account], email: String, fullName: String)

object AccountView {
  implicit val accountViewFormat: OFormat[AccountView] = Json.format[AccountView]
}
