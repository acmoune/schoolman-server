package controllers

import controllers.formfactories.TrainingPlanForms._
import controllers.formfactories._
import javax.inject._
import models._
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories._
import utils.ActionFunctions._
import utils.actions._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PlanController @Inject()(
                                cc: ControllerComponents,
                                planRepository: PlanRepository,
                                SecuredAction: SecuredAction
                              )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val planForm = FormFactory.form[PlanFormData, Plan]

  def newPlan(departmentId: Long, programId: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    planRepository.context(departmentId, programId)
      .map { ctx => Ok(views.html.pages.catalog.plans.newPlan(planForm.fill(FormFactory.emptyFormData[PlanFormData, Plan]), ctx.department, ctx.program)) }
  }

  def create(departmentId: Long, programId: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    planRepository.context(departmentId, programId)
      .flatMap { ctx =>
        planForm.bindFromRequest.fold(
          formWithErrors => Future { BadRequest(views.html.pages.catalog.plans.newPlan(formWithErrors, ctx.department, ctx.program)) },

          data => planRepository.addToProgram(programId, data)
            .map { planId =>
              Redirect(controllers.routes.PlanController.show(departmentId, programId, planId.value))
                .flashing("info" -> "The plan was successfully created")
            }
            .recover { case ex =>
              Redirect(controllers.routes.ProgramController.show(departmentId, programId))
                .flashing("danger" -> s"Could not create plan: ${ex.getMessage}")
            }
        )
      }
  }

  def show(departmentId: Long, programId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    planRepository.findById(departmentId, programId, id)
      .map { p => Ok(views.html.pages.catalog.plans.show(p.value, p.department, p.program, p.trainingPlans)) }
  }

  def update(departmentId: Long, programId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    planRepository.findById(departmentId, programId, id)
      .flatMap { p =>
        planForm.bindFromRequest.fold(
          formWithErrors => Future { BadRequest(views.html.pages.catalog.plans.edit(formWithErrors, p.department, p.program)) },

          data => planRepository.save(data)
            .map { planId =>
              Redirect(controllers.routes.PlanController.show(departmentId, programId, planId.value))
                .flashing("info" -> "The plan was successfully updated")
            }
            .recover { case ex =>
              Redirect(controllers.routes.PlanController.show(departmentId, programId, id))
                .flashing("danger" -> s"Could not update plan: ${ex.getMessage}")
            }
        )
      }
  }

  def edit(departmentId: Long, programId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    planRepository.findById(departmentId, programId, id)
      .map { p => Ok(views.html.pages.catalog.plans.edit(planForm.fill(FormFactory.formDataFrom[PlanFormData, Plan](p.value)), p.department, p.program)) }
  }

  def delete(departmentId: Long, programId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    planRepository.delete(id)
      .map { _ =>
        Redirect(controllers.routes.ProgramController.show(departmentId, programId))
          .flashing("info" -> "The plan was successfully deleted")
      }
      .recover { case ex =>
        Redirect(controllers.routes.PlanController.show(departmentId, programId, id))
          .flashing("danger" -> s"Could not delete plan: ${ex.getMessage}")
      }
  }

  def addTrainingPlans(departmentId: Long, programId: Long, planId: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    planRepository.findById(departmentId, programId, planId)
      .flatMap { p => {
        planRepository.matchTrainings(planId, programId)
          .map { trains =>
            Ok(views.html.pages.catalog.plans.addTrainingPlans(tpForm.fill(TrainingPlanListFormData(planId, Seq.empty[Long])), p.department, p.program, p.value, trains))
          }
      }}
  }

  def createTrainingPlans(departmentId: Long, programId: Long, planId: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    tpForm.bindFromRequest.fold(
      errors => Future {
        Redirect(controllers.routes.PlanController.show(departmentId, programId, planId))
          .flashing("danger" -> s"Could not add trainings into plan: ${errors.errors.head.message}")
      },

      data => {
        planRepository.addTrainings(data.planId, data.trainingIds)
          .map { _ =>
            Redirect(controllers.routes.PlanController.show(departmentId, programId, planId))
              .flashing("info" -> "Plan updated successfully")
          }
          .recover { case ex =>
            Redirect(controllers.routes.PlanController.show(departmentId, programId, planId))
              .flashing("danger" -> s"Could not add trainings into plan: ${ex.getMessage}")
          }
      }
    )
  }

  def showTrainingPlan(departmentId: Long, programId: Long, planId: Long, trainingId: Long, id: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      planRepository.findTrainingPlanById(departmentId, programId, planId, trainingId, id)
        .map { tp => Ok(views.html.pages.catalog.plans.showTrainingPlan(tp.value, tp.department, tp.program, tp.plan, tp.training, tp.trainingFees, tp.trainingSessions)) }
    }

  def editTrainingPlan(departmentId: Long, programId: Long, planId: Long, trainingId: Long, id: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      planRepository.findTrainingPlanById(departmentId, programId, planId, trainingId, id)
        .map { tp =>
          Ok(
            views.html.pages.catalog.plans.editTrainingPlan(
              tpDetailsForm.fill(TrainingPlanDetailsFormData(id, tp.value.duration)),
              tp.department,
              tp.program,
              tp.plan,
              tp.training
            )
          )
        }
    }

  def updateTrainingPlan(departmentId: Long, programId: Long, planId: Long, trainingId: Long, id: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      planRepository.findTrainingPlanById(departmentId, programId, planId, trainingId, id)
        .flatMap { tp =>
          tpDetailsForm.bindFromRequest.fold(
            formWithErrors => Future { BadRequest(views.html.pages.catalog.plans.editTrainingPlan(formWithErrors, tp.department, tp.program, tp.plan, tp.training)) },

            data => {
              planRepository.updateTrainingDuration(id, data.duration)
                .map { _ =>
                  Redirect(controllers.routes.PlanController.showTrainingPlan(departmentId, programId, planId, trainingId, id))
                    .flashing("info" -> "Training Plan updated successfully")
                }
            }
          )
        }
    }

  def deleteTrainingPlan(departmentId: Long, programId: Long, planId: Long, id: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      planRepository.deleteTrainingPlan(id)
        .map { _ =>
          Redirect(controllers.routes.PlanController.show(departmentId, programId, planId))
            .flashing("info" -> "The training plan was successfully deleted")
        }
        .recover { case ex =>
          Redirect(controllers.routes.PlanController.show(departmentId, programId, planId))
            .flashing("danger" -> s"Could not delete training plan: ${ex.getMessage}")
        }
    }
}
