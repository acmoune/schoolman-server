package api.controllers

import api.controllers.inputs.{AccountProfileFields, PasswordResetFields, TokenFields}
import api.dto.AccountProfileView
import api.repositories.AccountsRepository
import controllers.formfactories.AccountProfileFormData
import javax.inject._
import play.api.Logging
import play.api.libs.json._
import play.api.mvc._
import repositories.{AccountProfileRepository, AccountRepository}
import utils.JWTUtility
import utils.actions.JwtSecuredAction

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountsController @Inject()(
                                  cc: ControllerComponents,
                                  JwtSecuredAction: JwtSecuredAction,
                                  accountsRepository: AccountsRepository,
                                  accountRepository: AccountRepository,
                                  accountProfileRepository: AccountProfileRepository
                                  )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with Logging {

  def getAccount(email: String) = JwtSecuredAction.async { implicit request =>
    accountsRepository.getAccountByEmail(email).map { acc => Ok(Json.toJson(acc)) }
  }

  def getAccountProfile(email: String) = JwtSecuredAction.async { implicit  request =>
    accountsRepository.getAccountProfile(email).map { pro => Ok(Json.toJson(pro)) }
  }

  def getAccountForProfile(profileId: Long) = JwtSecuredAction.async { implicit request =>
    accountsRepository.getAccountForProfile(profileId).map { acc => Ok(Json.toJson(acc)) }
  }

  def updatePassword = JwtSecuredAction.async(parse.json) { implicit request =>
    request.body.validate[PasswordResetFields].fold(
      errors => { Future.successful(BadRequest(Json.obj( "status" -> "failed", "message" -> JsError.toJson(errors)))) },
      fields => {
        accountRepository.findByEmail(fields.email).flatMap {
          case Some(account) if accountRepository.comparePassword(account.password, fields.oldPassword) =>
            accountRepository.updatePassword(account, fields.newPassword)
              .map {_ => Ok(Json.obj("status" -> "succeeded"))}

          case _ => Future.successful(BadRequest(Json.obj("status" -> "failed", "message" -> "Wrong email Or incorrect password")))
        }
      }
    )
  }

  def validateToken = Action.async(parse.json) { implicit request =>
    request.body.validate[TokenFields].fold(
      errors => { Future.successful(BadRequest(Json.obj( "status" -> "failed", "message" -> JsError.toJson(errors)))) },
      fields => {
        if (JWTUtility.isValid(fields.token)){
          val acc = JWTUtility.decode(fields.token).get

          accountRepository.findByEmail(acc.email).map {
            case Some(_) => Ok(Json.obj("status" -> "succeeded"))
            case _ => Unauthorized(Json.obj( "status" -> "failed", "message" -> "Invalid token"))
          }
        } else {
          Future.successful(Unauthorized(Json.obj( "status" -> "failed", "message" -> "Invalid token")))
        }
      }
    )
  }

  def createProfile = JwtSecuredAction.async(parse.json) { implicit request =>
    request.body.validate[AccountProfileFields].fold(
      errors => { Future.successful(BadRequest(Json.obj( "status" -> "failed", "message" -> JsError.toJson(errors)))) },
      data => {
        val profile = AccountProfileFormData(
          accountId = data.accountId,
          birthDate = data.birthDate,
          birthPlace = data.birthPlace,
          residence = data.residence,
          phoneNumber = data.phoneNumber,
          nationality = data.nationality,
          otherDetails = data.otherDetails
        )

        accountProfileRepository.createFor(data.accountId, profile).map { p => Ok(Json.toJson(p.asInstanceOf[AccountProfileView])) }
      }
    )
  }

  def updateProfile = JwtSecuredAction.async(parse.json) { implicit request =>
    request.body.validate[AccountProfileFields].fold(
      errors => { Future.successful(BadRequest(Json.obj( "status" -> "failed", "message" -> JsError.toJson(errors)))) },
      data => {
        val profile = AccountProfileFormData(
          id = data.id,
          accountId = data.accountId,
          birthDate = data.birthDate,
          birthPlace = data.birthPlace,
          residence = data.residence,
          phoneNumber = data.phoneNumber,
          nationality = data.nationality,
          otherDetails = data.otherDetails
        )

        accountProfileRepository.save(profile).map { p => Ok(Json.toJson(p.asInstanceOf[AccountProfileView])) }
      }
    )
  }

  def getEnrollments(accountId: Long) = JwtSecuredAction.async { implicit request =>
    accountsRepository.getEnrollments(accountId).map { enrs => Ok(Json.toJson(enrs)) }
  }
}
