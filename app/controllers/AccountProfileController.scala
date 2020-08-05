package controllers

import controllers.formfactories.{AccountProfileFormData, FormFactory}
import javax.inject._
import play.api.mvc._
import repositories._
import models._

import scala.concurrent.{ExecutionContext, Future}

import utils.actions._
import utils.ActionFunctions._
import play.api.i18n.I18nSupport

class AccountProfileController @Inject()(
                                        cc: ControllerComponents,
                                        accountRepository: AccountRepository,
                                        accountProfileRepository: AccountProfileRepository,
                                        SecuredAction: SecuredAction
                                        )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val accountProfileForm = FormFactory.form[AccountProfileFormData, AccountProfile]

  def newAccountProfile(accountId: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.findById(accountId).map { account =>
      Ok(views.html.pages.accountProfiles.newProfile(accountProfileForm.fill(FormFactory.emptyFormData[AccountProfileFormData, AccountProfile].copy(accountId = accountId)), account.value))
    }
  }

  def create(accountId: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.findById(accountId).flatMap { account =>
      accountProfileForm.bindFromRequest.fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.pages.accountProfiles.newProfile(formWithErrors, account.value))
          }
        },

        data => {
          accountProfileRepository.createFor(accountId, data)
            .map { _ =>
              Redirect(controllers.routes.AccountController.show(accountId))
                .flashing("info" -> "The profile was successfully created")
            }
            .recover { case ex =>
              Redirect(controllers.routes.AccountController.show(accountId))
                .flashing("danger" -> s"Could not create profile: ${ex.getMessage}")
            }
        }
      )
    }
  }

  def edit(accountId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.findById(accountId).flatMap { account =>
      accountProfileRepository.findById(id)
        .map { p => Ok(views.html.pages.accountProfiles.edit(accountProfileForm.fill(FormFactory.formDataFrom[AccountProfileFormData, AccountProfile](p)), account.value)) }

    }
  }

  def update(accountId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    accountRepository.findById(accountId).flatMap { account =>
      accountProfileForm.bindFromRequest.fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.pages.accountProfiles.edit(formWithErrors, account.value))
          }
        },

        data => {
          accountProfileRepository.save(data)
            .map { _ =>
              Redirect(controllers.routes.AccountController.show(accountId))
                .flashing("info" -> "The profile was successfully updated")
            }
            .recover { case ex =>
              Redirect(controllers.routes.AccountController.show(accountId))
                .flashing("danger" -> s"Could not update profile: ${ex.getMessage}")
            }
        }
      )
    }
  }
}
