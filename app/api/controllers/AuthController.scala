package api.controllers

import api.controllers.inputs.Credentials
import javax.inject._
import play.api.mvc._
import repositories._
import models.AccountDetails

import scala.concurrent.{ExecutionContext, Future}
import play.api.i18n.I18nSupport
import play.api.libs.json._
import utils.JWTUtility

import scala.util.{Failure, Success}

class AuthController @Inject()(
                                cc: ControllerComponents,
                                accountRepository: AccountRepository
                              )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  def authenticateApiUser = Action.async(parse.json) { implicit request =>
    request.body.validate[Credentials].fold(
      errors => { Future.successful(BadRequest(Json.obj("status" -> "failed", "message" -> JsError.toJson(errors)))) },
      cred => {
        accountRepository.findByEmail(cred.email)
          .map {
            case Some(account) =>
              if (accountRepository.comparePassword(account.password, cred.password)) {
                JWTUtility.create(AccountDetails(account.email, account.role.toString, account.fullName)) match {
                  case Success(token) => Ok(Json.obj("status" -> "succeeded", "token" -> token))
                  case Failure(ex) => InternalServerError(Json.obj("status" -> "failed", "message" -> s"ServerError: ${ex.getMessage}"))
                }
              } else {
                BadRequest(Json.obj("status" -> "failed", "message" -> "Incorrect password"))
              }

            case _ =>
              BadRequest(Json.obj("status" -> "failed", "message" -> "Unable to find that email address"))
          }
      }
    )
  }
}

