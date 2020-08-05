package controllers

import controllers.formfactories.{FormFactory, TrainingUnitFormData}
import javax.inject.{Inject, Singleton}
import models._
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories._
import utils.ActionFunctions._
import utils.actions._

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class TrainingUnitController @Inject()(
                                        cc: ControllerComponents,
                                        trainingUnitRepository: TrainingUnitRepository,
                                        SecuredAction: SecuredAction
                                      )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val trainingUnitForm = FormFactory.form[TrainingUnitFormData, TrainingUnit]

  def newTrainingUnit(departmentId: Long, programId: Long, trainingId: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    trainingUnitRepository.context(departmentId, programId, trainingId)
      .map { ctx =>
        Ok(views.html.pages.catalog.trainingUnits.newTrainingUnit(
          trainingUnitForm.fill(FormFactory.emptyFormData[TrainingUnitFormData, TrainingUnit]),
          ctx.department,
          ctx.program,
          ctx.training
        ))
      }
  }

  def create(departmentId: Long, programId: Long, trainingId: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    trainingUnitRepository.context(departmentId, programId, trainingId)
      .flatMap { ctx =>
        trainingUnitForm.bindFromRequest.fold(
          formWithErrors => Future { BadRequest(views.html.pages.catalog.trainingUnits.newTrainingUnit(formWithErrors, ctx.department, ctx.program, ctx.training)) },

          data => {
            trainingUnitRepository.addToTraining(trainingId, data)
              .map { trainingUnitId =>
                Redirect(controllers.routes.TrainingUnitController.show(departmentId, programId, trainingId, trainingUnitId.value))
                  .flashing("info" -> "The training unit was successfully created")
              }
              .recover { case ex =>
                Redirect(controllers.routes.TrainingController.show(departmentId, programId, trainingId))
                  .flashing("danger" -> s"Could not create training unit: ${ex.getMessage}")
              }
          }
        )
      }
  }

  def show(departmentId: Long, programId: Long, trainingId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    trainingUnitRepository.findById(departmentId, programId, trainingId, id)
      .map { t => Ok(views.html.pages.catalog.trainingUnits.show(t.value, t.department, t.program, t.training)) }
  }

  def update(departmentId: Long, programId: Long, trainingId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    trainingUnitRepository.findById(departmentId, programId, trainingId, id)
      .flatMap { t =>
        trainingUnitForm.bindFromRequest.fold(
          formWithErrors => Future { BadRequest(views.html.pages.catalog.trainingUnits.edit(formWithErrors, t.department, t.program, t.training)) },

          data => {
            trainingUnitRepository.save(data)
              .map { trainingUnitId =>
                Redirect(controllers.routes.TrainingUnitController.show(departmentId, programId, trainingId, trainingUnitId.value))
                  .flashing("info" -> "The training unit was successfully updated")
              }
              .recover { case ex =>
                Redirect(controllers.routes.TrainingUnitController.show(departmentId, programId, trainingId, id))
                  .flashing("danger" -> s"Could not update training unit: ${ex.getMessage}")
              }
          }
        )
      }
  }

  def edit(departmentId: Long, programId: Long, trainingId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    trainingUnitRepository.findById(departmentId, programId, trainingId, id)
      .map { t => Ok(views.html.pages.catalog.trainingUnits.edit(trainingUnitForm.fill(FormFactory.formDataFrom[TrainingUnitFormData, TrainingUnit](t.value)), t.department, t.program, t.training)) }
  }

  def delete(departmentId: Long, programId: Long, trainingId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    trainingUnitRepository.delete(id)
      .map { _ =>
        Redirect(controllers.routes.TrainingController.show(departmentId, programId, trainingId))
          .flashing("info" -> "The training unit was successfully deleted")
      }
      .recover { case ex =>
        Redirect(controllers.routes.TrainingUnitController.show(departmentId, programId, trainingId, id))
          .flashing("danger" -> s"Could not delete training unit: ${ex.getMessage}")
      }
  }
}
