package controllers

import controllers.formfactories.{AccountDetailsFormData, AccountFormData, AccountPasswordFormData, FormFactory}
import controllers.formfactories.AccountForms._
import javax.inject._
import play.api.mvc._
import repositories._
import models._
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}
import utils.actions._
import utils.ActionFunctions._
import play.api.i18n.I18nSupport
import utils.Mailer

class AccountController @Inject()(
                                   cc: ControllerComponents,
                                   config: Configuration,
                                   accountRepository: AccountRepository,
                                   SecuredAction: SecuredAction,
                                   mailer: Mailer
                                 )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val accountForm = FormFactory.form[AccountFormData, Account]

  def list() = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.all
      .map { accounts => Ok(views.html.pages.accounts.list(accounts)) }
  }

  def newAccount = SecuredAction.andThen(IsAdmin) { implicit request =>
    Ok(views.html.pages.accounts.newAccount(accountForm.fill(FormFactory.emptyFormData[AccountFormData, Account])))
  }

  def create = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountForm.bindFromRequest.fold(
      formWithErrors => {
        Future { BadRequest(views.html.pages.accounts.newAccount(formWithErrors)) }
      },

      data => {
        accountRepository.add(data)
          .map { accountId => {
            val from = config.get[String]("schoolman.mailing.from")
            val to = Seq(s"${data.fullName} <${data.email}>")
            val message = s"""
              <html>
                <body>
                  <p>Dear ${data.fullName}</p>

                  <p>
                    Your account has been created.
                  </p>

                  <p>
                    Please login into your account on our
                    <a href="${config.get[String]("schoolman.student-portal")}/signIn" target="_blank">Student Portal</a>
                    so you can manage your profile and finances.
                  </p>

                  <p>
                    Credentials: <br />
                    Email: ${data.email} <br />
                    Password: changeme
                  </p>

                  <p>
                    Please change your password as soon as possible.
                    <br />
                    <br />
                  </p>

                  <p>Sincerely, SchoolMan</p>
                </body>
              </html>
            """
            mailer.sendEmail("Account Created", from, to, message)

            Redirect(controllers.routes.AccountController.show(accountId.value))
              .flashing("info" -> "The new account was successfully created")
          }}
          .recover { case ex =>
            Redirect(controllers.routes.AccountController.list())
              .flashing("danger" -> s"An error occurred: ${ex.getMessage}")
          }
      }
    )
  }

  def show(id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.findById(id)
      .map { acc => Ok(views.html.pages.accounts.show(acc.value, acc.profile, acc.enrollments)) }
  }

  def delete(id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.delete(id)
      .map { _ =>
        Redirect(controllers.routes.AccountController.list())
          .flashing("info" -> "The account was successfully deleted")
      }
      .recover { case ex =>
        Redirect(controllers.routes.AccountController.show(id))
          .flashing("danger" -> s"Could not delete account: ${ex.getMessage}")
      }
  }

  def lock(id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.setLocking(id, locked = true)
      .map { _ =>
        Redirect(controllers.routes.AccountController.show(id))
          .flashing("info" -> s"Account locked")
      }
  }

  def unlock(id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.setLocking(id, locked = false)
      .map { _ =>
        Redirect(controllers.routes.AccountController.show(id))
          .flashing("info" -> s"Account unlocked")
      }
  }

  def editDetails(id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.findById(id)
      .map { a => Ok(
        views.html.pages.accounts.editDetails(detailsForm.fill(AccountDetailsFormData(a.value.id.value, a.value.email, a.value.fullName, a.value.role.toString)))
      )}
  }

  def updateDetails(id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.findById(id)
      .flatMap { _ =>
        detailsForm.bindFromRequest.fold(
          formWithErrors => Future {
            BadRequest(views.html.pages.accounts.editDetails(formWithErrors))
          },

          data => {
            accountRepository.updateDetails(id, data)
              .map { _ =>
                Redirect(controllers.routes.AccountController.show(id))
                  .flashing("info" -> "Account details updated successfully")
              }
              .recover { case ex =>
                Redirect(controllers.routes.AccountController.show(id))
                  .flashing("danger" -> s"Could not update account details: ${ex.getMessage}")
              }
          }
        )
      }
  }

  def editPassword(id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.findById(id).map { account =>
      Ok(views.html.pages.accounts.editPassword(passwordAdminForm.fill(AccountPasswordFormData(id, "", "")), account.value))
    }
  }

  def updatePassword(id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.findById(id)
      .flatMap { account =>
        passwordAdminForm.bindFromRequest.fold(
          formWithErrors => Future {
            BadRequest(views.html.pages.accounts.editPassword(formWithErrors, account.value))
          },

          data => {
            accountRepository.adminResetPassword(id, data.newPassword)
              .map { _ =>
                Redirect(controllers.routes.AccountController.show(id))
                  .flashing("info" -> "Account password updated successfully")
              }
              .recover { case ex =>
                Redirect(controllers.routes.AccountController.show(id))
                  .flashing("danger" -> s"Could not update account password: ${ex.getMessage}")
              }
          }
        )
      }
  }
}
