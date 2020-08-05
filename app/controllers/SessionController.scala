package controllers

import controllers.formfactories.{FormFactory, TrainingSessionFormData}
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

import scala.util.Success

class SessionController @Inject()(
                                   cc: ControllerComponents,
                                   trainingSessionRepository: TrainingSessionRepository,
                                   SecuredAction: SecuredAction,
                                   config: Configuration,
                                   mailer: Mailer
                                 )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val trainingSessionForm = FormFactory.form[TrainingSessionFormData, TrainingSession]

  def newTrainingSession(departmentId: Long, programId: Long, planId: Long, trainingId: Long, trainingPlanId: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      trainingSessionRepository.context(departmentId, programId, planId, trainingId, trainingPlanId)
        .map { ctx =>
          Ok(views.html.pages.trainingSessions.newTrainingSession(
            trainingSessionForm.fill(FormFactory.emptyFormData[TrainingSessionFormData, TrainingSession]),
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
      trainingSessionRepository.context(departmentId, programId, planId, trainingId, trainingPlanId)
        .flatMap { ctx =>
          trainingSessionForm.bindFromRequest.fold(
            formWithErrors => Future {
              BadRequest(views.html.pages.trainingSessions.newTrainingSession(
                formWithErrors,
                ctx.department,
                ctx.program,
                ctx.plan,
                ctx.training,
                ctx.trainingPlan
              ))
            },

            data => {
              trainingSessionRepository.addToTrainingPlan(trainingPlanId, data)
                .map { tSessId =>
                  Redirect(controllers.routes.SessionController.show(departmentId, programId, planId, trainingId, trainingPlanId, tSessId.value))
                    .flashing("info" -> "The training Session was successfully created")
                }
                .recover { case ex =>
                  Redirect(controllers.routes.PlanController.showTrainingPlan(departmentId, programId, planId, trainingId, trainingPlanId))
                    .flashing("danger" -> s"Could not create training session: ${ex.getMessage}")
                }
            }
          )
        }
    }

  def update(departmentId: Long, programId: Long, planId: Long, trainingId: Long, trainingPlanId: Long, id: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      trainingSessionRepository.findById(departmentId, programId, planId, trainingId, trainingPlanId, id)
        .flatMap { tsess =>
          trainingSessionForm.bindFromRequest.fold(
            formWithErrors => Future {
              BadRequest(views.html.pages.trainingSessions.edit(
                formWithErrors,
                tsess.department,
                tsess.program,
                tsess.plan,
                tsess.training,
                tsess.trainingPlan
              ))
            },

            data => {
              trainingSessionRepository.save(data)
                .map { _ => {
                  // send email if session is Finished
                  if (data.status == "Finished") {
                    val fut = trainingSessionRepository.getTrainingSessionEnv(id)
                    fut.onComplete {
                      case Success(tsEnv) => {
                        val from = config.get[String]("schoolman.mailing.from")
                        val to = tsEnv.emails
                        val message = s"""
                          <html>
                            <body>
                              <p>Dear,</p>

                              <p>
                                Your training <strong>${tsEnv.trainingTitle}</strong>,
                                for the session <strong>${tsEnv.sessionTitle}</strong> is completed.
                              </p>

                              <p style="visibility:hidden">
                                If your financial status is OK, you can come and collect your <strong>Certificate of Completion</strong>
                                at the Institute Office, or you can download it in PDF format from the <a href="${config.get[String]("schoolman.student-portal")}" target="_blank">Student Portal</a>.
                                <br />
                              </p>

                              <p>Sincerely, IMIT</p>
                            </body>
                          </html>
                        """
                        mailer.sendEmail("Training Session Completed", from, to, message)
                      }
                    }
                  }

                  Redirect(controllers.routes.SessionController.show(departmentId, programId, planId, trainingId, trainingPlanId, id))
                    .flashing("info" -> "The training session was successfully updated")
                }}

                .recover { case ex =>
                  Redirect(controllers.routes.SessionController.show(departmentId, programId, planId, trainingId, trainingPlanId, id))
                    .flashing("danger" -> s"Could not update training session: ${ex.getMessage}")
                }
            }
          )
        }
    }

  def show(departmentId: Long, programId: Long, planId: Long, trainingId: Long, trainingPlanId: Long, id: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      trainingSessionRepository.findById(departmentId, programId, planId, trainingId, trainingPlanId, id)
        .map { t => Ok(views.html.pages.trainingSessions.show(t.value, t.department, t.program, t.plan, t.training, t.trainingPlan, t.fees, t.enrollments)) }
    }

  def edit(departmentId: Long, programId: Long, planId: Long, trainingId: Long, trainingPlanId: Long, id: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      trainingSessionRepository.findById(departmentId, programId, planId, trainingId, trainingPlanId, id)
        .map { tsess => Ok(views.html.pages.trainingSessions.edit(
          trainingSessionForm.fill(FormFactory.formDataFrom[TrainingSessionFormData, TrainingSession](tsess.value)),
          tsess.department,
          tsess.program,
          tsess.plan,
          tsess.training,
          tsess.trainingPlan
        ))}
    }

  def delete(departmentId: Long, programId: Long, planId: Long, trainingId: Long, trainingPlanId: Long, id: Long) =
    SecuredAction.andThen(IsAdmin).async { implicit request =>
      trainingSessionRepository.delete(id)
        .map { _ =>
          Redirect(controllers.routes.PlanController.showTrainingPlan(departmentId, programId, planId, trainingId, trainingPlanId))
            .flashing("info" -> "The training session was successfully deleted")
        }
        .recover { case ex =>
          Redirect(controllers.routes.SessionController.show(departmentId, programId, planId, trainingId, trainingPlanId, id))
            .flashing("danger" -> s"Could not delete training session: ${ex.getMessage}")
        }
    }
}
