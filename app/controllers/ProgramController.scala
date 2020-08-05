package controllers

import java.nio.file.Paths
import java.util.UUID.randomUUID

import controllers.formfactories.{FormFactory, ProgramFormData}
import javax.inject._
import models.Program
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.ProgramRepository
import utils.ActionFunctions._
import utils.S3Service
import utils.actions._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProgramController @Inject()(
                                   cc: ControllerComponents,
                                   s3Service: S3Service,
                                   programRepository: ProgramRepository,
                                   env: Environment,
                                   SecuredAction: SecuredAction,
                                   showProgramTpl: views.html.pages.catalog.programs.show,
                                   editProgramTpl: views.html.pages.catalog.programs.edit,
                                   newProgramTpl: views.html.pages.catalog.programs.newProgram
                                 )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val programForm = FormFactory.form[ProgramFormData, Program]

  def newProgram(departmentId: Long) = SecuredAction.andThen(IsAdmin).async { implicit request => {
    programRepository.context(departmentId)
      .map { ctx => Ok(newProgramTpl(
        programForm.fill(FormFactory.emptyFormData[ProgramFormData, Program].copy(departmentId = departmentId)),
        ctx.department
      ))}
  }}

  def create(departmentId: Long) = SecuredAction.andThen(IsAdmin).async(parse.multipartFormData) { implicit request =>
    programRepository.context(departmentId)
      .flatMap { ctx =>
        programForm.bindFromRequest.fold(
          formWithErrors => Future { BadRequest(newProgramTpl(formWithErrors, ctx.department)) },

          data => {
            val newLogoFut = request.body.file("logoFile") match {
              case Some(file) =>
                val newFilename = s"programs/" + randomUUID + "_" + Paths.get(file.filename).getFileName
                s3Service.uploadFile(file.ref, newFilename).map(_ => Some(newFilename))
              case _ => Future { None }
            }

            newLogoFut.flatMap { newLogo =>
              val prog = if (newLogo.isDefined) data.copy(logo = newLogo) else data

              programRepository.addToDepartment(departmentId, prog)
                .map { programId =>
                  Redirect(controllers.routes.ProgramController.show(departmentId, programId.value))
                    .flashing("info" -> "The program was successfully created")
                }
                .recover { case ex =>
                  Redirect(controllers.routes.DepartmentController.show(departmentId))
                    .flashing("danger" -> s"Could not create program: ${ex.getMessage}")
                }

            }
          }
        )
      }
      .recover { case ex => NotFound(ex.getMessage) }
  }

  def show(departmentId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    programRepository.findById(departmentId, id)
      .map { p => Ok(showProgramTpl(p.value, p.department, p.plans, p.trainings)) }
  }

  def update(departmentId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async(parse.multipartFormData) { implicit request =>
    programRepository.findById(departmentId, id).flatMap { p =>
      programForm.bindFromRequest.fold(
        formWithErrors => Future { BadRequest(editProgramTpl(formWithErrors, p.department)) },

        data => {
          val newLogoFut = request.body.file("logoFile") match {
            case Some(file) =>
              val newFilename = s"programs/" + randomUUID + "_" + Paths.get(file.filename).getFileName
              val delOldFut = if (data.logo.isDefined) s3Service.deleteFile(data.logo.get) else Future.successful(0)
              for {
                _ <- s3Service.uploadFile(file.ref, newFilename)
                _ <- delOldFut
              } yield Some(newFilename)

            case _ => Future { None }
          }

          newLogoFut.flatMap { newLogo =>
            val prog = if (newLogo.isDefined) data.copy(logo = newLogo) else data

            programRepository.save(prog)
              .map { programId =>
                Redirect(controllers.routes.ProgramController.show(departmentId, programId.value))
                  .flashing("info" -> "The program was successfully updated")
              }
              .recover { case ex =>
                Redirect(controllers.routes.ProgramController.show(departmentId, id))
                  .flashing("danger" -> s"Could not create program: ${ex.getMessage}")
              }
          }
        }
      )
    }
  }

  def edit(departmentId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    programRepository.findById(departmentId, id)
      .map { p => Ok(editProgramTpl(programForm.fill(FormFactory.formDataFrom[ProgramFormData, Program](p.value)), p.department)) }
  }

  def delete(departmentId: Long, id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    programRepository.delete(id)
      .map { _ =>
        Redirect(controllers.routes.DepartmentController.show(departmentId))
          .flashing("info" -> "The program was successfully deleted")
      }
      .recover { case ex =>
        Redirect(controllers.routes.ProgramController.show(departmentId, id))
          .flashing("danger" -> s"Could not delete program: ${ex.getMessage}")
      }
  }
}
