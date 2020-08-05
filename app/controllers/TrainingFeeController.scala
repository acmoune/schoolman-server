package controllers

import controllers.formfactories.{FormFactory, TrainingFeeFormData}
import javax.inject.{Inject, Singleton}
import models._
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories._
import utils.ActionFunctions._
import utils.actions._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TrainingFeeController @Inject()(
                                        cc: ControllerComponents,
                                        trainingFeeRepository: TrainingFeeRepository,
                                        SecuredAction: SecuredAction
                                      )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val trainingFeeForm = FormFactory.form[TrainingFeeFormData, TrainingFee]

  def newTrainingFee(departmentId: Long, programId: Long, planId: Long, trainingId: Long, trainingPlanId: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      trainingFeeRepository.context(departmentId, programId, planId, trainingId, trainingPlanId)
        .map { ctx =>
          Ok(views.html.pages.catalog.trainingFees.newTrainingFee(
            trainingFeeForm.fill(FormFactory.emptyFormData[TrainingFeeFormData, TrainingFee]),
            ctx.department,
            ctx.program,
            ctx.plan,
            ctx.training,
            ctx.trainingPlan
          ))
        }
  }

  def create(departmentId: Long, programId: Long, planId: Long, trainingId: Long, trainingPlanId: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      trainingFeeRepository.context(departmentId, programId, planId, trainingId, trainingPlanId)
        .flatMap { ctx =>
          trainingFeeForm.bindFromRequest.fold(
            formWithErrors => Future {
              BadRequest(views.html.pages.catalog.trainingFees.newTrainingFee(
                formWithErrors,
                ctx.department,
                ctx.program,
                ctx.plan,
                ctx.training,
                ctx.trainingPlan
              ))
            },

            data => {
              trainingFeeRepository.addToTrainingPlan(trainingPlanId, data)
                .map { _ =>
                  Redirect(controllers.routes.PlanController.showTrainingPlan(departmentId, programId, planId, trainingId, trainingPlanId))
                    .flashing("info" -> "The training fee was successfully created")
                }
                .recover { case ex =>
                  Redirect(controllers.routes.PlanController.showTrainingPlan(departmentId, programId, planId, trainingId, trainingPlanId))
                    .flashing("danger" -> s"Could not create training fee: ${ex.getMessage}")
                }
            }
          )
        }
  }

  def update(departmentId: Long, programId: Long, planId: Long, trainingId: Long, trainingPlanId: Long, id: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      trainingFeeRepository.findById(departmentId, programId, planId, trainingId, trainingPlanId, id)
        .flatMap { tf =>
          trainingFeeForm.bindFromRequest.fold(
            formWithErrors => Future {
              BadRequest(views.html.pages.catalog.trainingFees.edit(
                formWithErrors,
                tf.department,
                tf.program,
                tf.plan,
                tf.training,
                tf.trainingPlan
              ))
            },

            data => {
              trainingFeeRepository.save(data)
                .map { _ =>
                  Redirect(controllers.routes.PlanController.showTrainingPlan(departmentId, programId, planId, trainingId, trainingPlanId))
                    .flashing("info" -> "The training fee was successfully updated")
                }
                .recover { case ex =>
                  Redirect(controllers.routes.PlanController.showTrainingPlan(departmentId, programId, planId, trainingId, trainingPlanId))
                    .flashing("danger" -> s"Could not update training fee: ${ex.getMessage}")
                }
            }
          )
        }
  }

  def edit(departmentId: Long, programId: Long, planId: Long, trainingId: Long, trainingPlanId: Long, id: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      trainingFeeRepository.findById(departmentId, programId, planId, trainingId, trainingPlanId, id)
        .map { tf => Ok(views.html.pages.catalog.trainingFees.edit(
          trainingFeeForm.fill(FormFactory.formDataFrom[TrainingFeeFormData, TrainingFee](tf.value)),
          tf.department,
          tf.program,
          tf.plan,
          tf.training,
          tf.trainingPlan
        ))}
  }

  def delete(departmentId: Long, programId: Long, planId: Long, trainingId: Long, trainingPlanId: Long, id: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      trainingFeeRepository.delete(id)
        .map { _ =>
          Redirect(controllers.routes.PlanController.showTrainingPlan(departmentId, programId, planId, trainingId, trainingPlanId))
            .flashing("info" -> "The training fee was successfully deleted")
        }
        .recover { case ex =>
          Redirect(controllers.routes.PlanController.showTrainingPlan(departmentId, programId, planId, trainingId, trainingPlanId))
            .flashing("danger" -> s"Could not delete training fee: ${ex.getMessage}")
        }
  }
}
