package controllers

import javax.inject._
import play.api.mvc._
import repositories._
import controllers.formfactories.{AuthForms, SignInFormData}
import scala.concurrent.{ExecutionContext, Future}
import utils.actions._
import utils.ActionFunctions._
import play.api.i18n.I18nSupport

class AuthController @Inject()(
                              cc: ControllerComponents,
                              accountRepository: AccountRepository,
                              SecuredAction: SecuredAction
                              )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  def signInUser = Action { implicit request =>
    Ok(views.html.pages.auth.signin(AuthForms.signInForm.fill(SignInFormData(email = "", password = ""))))
  }

  def signOutUser = SecuredAction.andThen(IsAdmin) { implicit request =>
    Redirect(routes.DepartmentController.list()).withNewSession
  }

  def authenticateUser = Action.async { implicit request =>
    AuthForms.signInForm.bindFromRequest.fold(
      formWithErrors => Future { BadRequest(views.html.pages.auth.signin(formWithErrors)) },
      data => {
        accountRepository.findByEmail(data.email)
          .map {
            case Some(account) =>
              if (accountRepository.comparePassword(account.password, data.password)) {
                Redirect(routes.DepartmentController.list())
                  .withSession("email" -> account.email, "role" -> account.role.toString, "fullName" -> account.fullName)
              } else {
                Ok(views.html.pages.auth.signin(
                  AuthForms.signInForm.fill(SignInFormData(data.email, data.password)).withGlobalError("Incorrect password"))
                )
              }

            case _ =>
              Ok(views.html.pages.auth.signin(
                AuthForms.signInForm.fill(SignInFormData(data.email, data.password)).withGlobalError("Unable to find that email address"))
              )
          }
      }
    )
  }
}
