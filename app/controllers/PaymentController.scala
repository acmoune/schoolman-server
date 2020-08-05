package controllers

import controllers.formfactories.{FormFactory, PaymentFormData}
import javax.inject.Inject
import models._
import play.api.mvc._
import repositories._
import slick.util.Logging

import scala.concurrent.{ExecutionContext, Future}

import utils.actions._
import utils.ActionFunctions._
import play.api.i18n.I18nSupport

class PaymentController @Inject()(
                                cc: ControllerComponents,
                                paymentRepository: PaymentRepository,
                                SecuredAction: SecuredAction
                              )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with Logging with I18nSupport {

  val paymentForm = FormFactory.form[PaymentFormData, Payment]

  def newPayment(
               departmentId: Long,
               programId: Long,
               planId: Long,
               trainingId: Long,
               trainingPlanId: Long,
               trainingSessionId: Long,
               accountId: Long,
               enrollmentId: Long,
               billId: Long
             ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      paymentRepository.context(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, billId)
        .map { ctx =>
          Ok(views.html.pages.payments.newPayment(
            paymentForm.fill(FormFactory.emptyFormData[PaymentFormData, Payment]),
            ctx.department,
            ctx.program,
            ctx.plan,
            ctx.training,
            ctx.trainingPlan,
            ctx.trainingSession,
            ctx.account,
            ctx.enrollment,
            ctx.bill,
            ctx.fee
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
              enrollmentId: Long,
              billId: Long
            ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      paymentRepository.context(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, billId)
        .flatMap { ctx =>
          paymentForm.bindFromRequest.fold(
            formWithErrors => Future {
              BadRequest(views.html.pages.payments.newPayment(
                formWithErrors,
                ctx.department,
                ctx.program,
                ctx.plan,
                ctx.training,
                ctx.trainingPlan,
                ctx.trainingSession,
                ctx.account,
                ctx.enrollment,
                ctx.bill,
                ctx.fee
              ))
            },

            data => {
              paymentRepository.addToBill(data)
                .map { _ =>
                  Redirect(controllers.routes.BillController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, billId))
                    .flashing("info" -> "Payment successfully created")
                }
                .recover { case ex =>
                  Redirect(controllers.routes.BillController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, billId))
                    .flashing("danger" -> s"Could not create payment: ${ex.getMessage}")
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
              billId: Long,
              id: Long
            ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      paymentRepository.delete(id)
        .map { _ =>
          Redirect(controllers.routes.BillController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, billId))
            .flashing("info" -> "Payment successfully deleted")
        }
        .recover { case ex =>
          Redirect(controllers.routes.BillController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, enrollmentId, billId))
            .flashing("danger" -> s"Could not delete payment: ${ex.getMessage}")
        }
    }
}
