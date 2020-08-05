package controllers

import java.nio.file.Paths
import java.util.UUID.randomUUID

import controllers.formfactories.{FormFactory, TrainingFormData}
import javax.inject.{Inject, Singleton}
import models._
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories._
import utils.ActionFunctions._
import utils.S3Service
import utils.actions._

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class TrainingController @Inject()(
                                    cc: ControllerComponents,
                                    trainingRepository: TrainingRepository,
                                    s3Service: S3Service,
                                    env: Environment,
                                    SecuredAction: SecuredAction,
                                    showTrainingTpl: views.html.pages.catalog.trainings.show,
                                    editTrainingTpl: views.html.pages.catalog.trainings.edit,
                                    newTrainingTpl: views.html.pages.catalog.trainings.newTraining,
                                  )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val trainingForm = FormFactory.form[TrainingFormData, Training]

  def newTraining(departmentId: Long, programId: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    trainingRepository.context(departmentId, programId)
      .map { ctx =>
        Ok(newTrainingTpl(
          trainingForm.fill(FormFactory.emptyFormData[TrainingFormData, Training]),
          ctx.department,
          ctx.program
        ))
      }
  }

  def create(departmentId: Long, programId: Long) = SecuredAction.andThen(IsAdmin).async(parse.multipartFormData) { implicit request =>
    trainingRepository.context(departmentId, programId)
      .flatMap { ctx =>
        trainingForm.bindFromRequest.fold(
          formWithErrors => Future { BadRequest(newTrainingTpl(formWithErrors, ctx.department, ctx.program)) },

          data => {
            val newBannerFut = request.body.file("bannerFile") match {
              case Some(file) =>
                val newFilename = s"trainings/" + randomUUID + "_" + Paths.get(file.filename).getFileName
                s3Service.uploadFile(file.ref, newFilename).map(_ => Some(newFilename))
              case _ => Future { None }
            }

            newBannerFut.flatMap { newBanner =>
              val train = if (newBanner.isDefined) data.copy(banner = newBanner) else data

              trainingRepository.addToProgram(programId, train)
                .map { trainingId =>
                  Redirect(controllers.routes.TrainingController.show(departmentId, programId, trainingId.value))
                    .flashing("info" -> "The training was successfully created")
                }
                .recover { case ex =>
                  Redirect(controllers.routes.ProgramController.show(departmentId, programId))
                    .flashing("danger" -> s"Could not create training: ${ex.getMessage}")
                }
            }
          }
        )
      }
  }

  def show(departmentId: Long, programId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    trainingRepository.findById(departmentId, programId, id)
      .map { t => Ok(showTrainingTpl(t.value, t.department, t.program, t.trainingUnits)) }
  }

  def update(departmentId: Long, programId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async(parse.multipartFormData) { implicit request =>
    trainingRepository.findById(departmentId, programId, id)
      .flatMap { t =>
        trainingForm.bindFromRequest.fold(
          formWithErrors => Future { BadRequest(editTrainingTpl(formWithErrors, t.department, t.program)) },

          data => {
            val newBannerFut = request.body.file("bannerFile") match {
              case Some(file) =>
                val newFilename = s"trainings/" + randomUUID + "_" + Paths.get(file.filename).getFileName
                val delOldFut = if (data.banner.isDefined) s3Service.deleteFile(data.banner.get) else Future.successful(null)
                for {
                  _ <- s3Service.uploadFile(file.ref, newFilename)
                  _ <- delOldFut
                } yield Some(newFilename)

              case _ => Future { None }
            }

            newBannerFut.flatMap { newBanner =>
              val train = if (newBanner.isDefined) data.copy(banner = newBanner) else data

              trainingRepository.save(train)
                .map { trainingId =>
                  Redirect(controllers.routes.TrainingController.show(departmentId, programId, trainingId.value))
                    .flashing("info" -> "The training was successfully updated")
                }
                .recover { case ex =>
                  Redirect(controllers.routes.TrainingController.show(departmentId, programId, id))
                    .flashing("danger" -> s"Could not update training: ${ex.getMessage}")
                }
            }
          }
        )
      }
  }

  def edit(departmentId: Long, programId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    trainingRepository.findById(departmentId, programId, id)
      .map { t => Ok(editTrainingTpl(trainingForm.fill(FormFactory.formDataFrom[TrainingFormData, Training](t.value)), t.department, t.program)) }
  }

  def delete(departmentId: Long, programId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    trainingRepository.delete(id)
      .map { _ =>
        Redirect(controllers.routes.ProgramController.show(departmentId, programId))
          .flashing("info" -> "The training was successfully deleted")
      }
      .recover { case ex =>
        Redirect(controllers.routes.TrainingController.show(departmentId, programId, id))
          .flashing("danger" -> s"Could not delete training: ${ex.getMessage}")
      }
  }
}
