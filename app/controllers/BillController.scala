package controllers

import controllers.formfactories.{BillFormData, FormFactory}
import javax.inject.Inject
import models._
import play.api.mvc._
import repositories._
import slick.util.Logging

import scala.concurrent.{ExecutionContext, Future}

import utils.actions._
import utils.ActionFunctions._
import play.api.i18n.I18nSupport

class BillController @Inject()(
                                cc: ControllerComponents,
                                billRepository: BillRepository,
                                SecuredAction: SecuredAction
                              )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with Logging with I18nSupport {

  val billForm = FormFactory.form[BillFormData, Bill]

  def newBill(
               departmentId: Long,
               programId: Long,
               planId: Long,
               trainingId: Long,
               trainingPlanId: Long,
               trainingSessionId: Long,
               accountId: Long,
               enrollmentId: Long
             ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      billRepository.context(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId)
        .map { ctx =>
          Ok(views.html.pages.bills.newBill(
            billForm.fill(FormFactory.emptyFormData[BillFormData, Bill]),
            ctx.department,
            ctx.program,
            ctx.plan,
            ctx.training,
            ctx.trainingPlan,
            ctx.trainingSession,
            ctx.account,
            ctx.enrollment,
            ctx.fees
          ))
        }
    }

  def create(
              departmentId: Long,
              programId: Long,
              planId: Long,
              trainingId: Long,
              trainingPlanId: Long,
              trainingSessionId: Long,
              accountId: Long,
              enrollmentId: Long
            ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      billRepository.context(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId)
        .flatMap { ctx =>
          billForm.bindFromRequest.fold(
            formWithErrors => Future {
              BadRequest(views.html.pages.bills.newBill(
                formWithErrors,
                ctx.department,
                ctx.program,
                ctx.plan,
                ctx.training,
                ctx.trainingPlan,
                ctx.trainingSession,
                ctx.account,
                ctx.enrollment,
                ctx.fees
              ))
            },

            data => {
              billRepository.addToEnrollment(data)
                .map { billId =>
                  Redirect(controllers.routes.BillController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, billId.value))
                    .flashing("info" -> "Bill successfully created")
                }
                .recover { case ex =>
                  Redirect(controllers.routes.EnrollmentController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId))
                    .flashing("danger" -> s"Could not create bill: ${ex.getMessage}")
                }
            }
          )
        }
    }

  def show(
            departmentId: Long,
            programId: Long,
            planId: Long,
            trainingId: Long,
            trainingPlanId: Long,
            trainingSessionId: Long,
            accountId: Long,
            enrollmentId: Long,
            id: Long
          ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      billRepository.findById(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, id)
        .map { e => Ok(views.html.pages.bills.show(
          e.value,
          e.department,
          e.program,
          e.plan,
          e.training,
          e.trainingPlan,
          e.trainingSession,
          e.account,
          e.enrollment,
          e.payments,
          e.fee
        ))}
    }

  def edit(
            departmentId: Long,
            programId: Long,
            planId: Long,
            trainingId: Long,
            trainingPlanId: Long,
            trainingSessionId: Long,
            accountId: Long,
            enrollmentId: Long,
            id: Long
          ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      billRepository.findById(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, id)
        .map { e => Ok(views.html.pages.bills.edit(
          billForm.fill(FormFactory.formDataFrom[BillFormData, Bill](e.value)),
          e.department,
          e.program,
          e.plan,
          e.training,
          e.trainingPlan,
          e.trainingSession,
          e.account,
          e.enrollment,
          e.fee
        ))}
    }

  def update(
              departmentId: Long,
              programId: Long,
              planId: Long,
              trainingId: Long,
              trainingPlanId: Long,
              trainingSessionId: Long,
              accountId: Long,
              enrollmentId: Long,
              id: Long
            ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      billRepository.findById(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, id)
        .flatMap { e =>
          billForm.bindFromRequest.fold(
            formWithErrors => Future {
              BadRequest(views.html.pages.bills.edit(
                formWithErrors,
                e.department,
                e.program,
                e.plan,
                e.training,
                e.trainingPlan,
                e.trainingSession,
                e.account,
                e.enrollment,
                e.fee
              ))
            },

            data => {
              billRepository.save(data)
                .map { _ =>
                  Redirect(controllers.routes.BillController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, id))
                    .flashing("info" -> "Bill successfully updated")
                }
                .recover { case ex =>
                  Redirect(controllers.routes.BillController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, id))
                    .flashing("danger" -> s"Could not update bill: ${ex.getMessage}")
                }
            }
          )
        }
    }

  def delete(
              departmentId: Long,
              programId: Long,
              planId: Long,
              trainingId: Long,
              trainingPlanId: Long,
              trainingSessionId: Long,
              accountId: Long,
              enrollmentId: Long,
              id: Long
            ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      billRepository.delete(id)
        .map { _ =>
          Redirect(controllers.routes.EnrollmentController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId))
            .flashing("info" -> "Bill successfully deleted")
        }
        .recover { case ex =>
          Redirect(controllers.routes.BillController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, id))
            .flashing("danger" -> s"Could not delete bill: ${ex.getMessage}")
        }
    }
}
