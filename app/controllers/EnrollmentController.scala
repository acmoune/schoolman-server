package controllers

import controllers.formfactories.{EnrollmentFormData, FormFactory}
import javax.inject._
import play.api.mvc._
import repositories.EnrollmentRepository
import models._
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}
import utils.actions._
import utils.ActionFunctions._
import utils.Mailer
import play.api.i18n.I18nSupport

import scala.util.Success

class EnrollmentController @Inject()(
                                    cc: ControllerComponents,
                                    enrollmentRepository: EnrollmentRepository,
                                    SecuredAction: SecuredAction,
                                    config: Configuration,
                                    mailer: Mailer
                                    )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val enrollmentForm = FormFactory.form[EnrollmentFormData, Enrollment]

  def newEnrollment(
                     departmentId: Long,
                     programId: Long,
                     planId: Long,
                     trainingId: Long,
                     trainingPlanId: Long,
                     trainingSessionId: Long
                   ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      enrollmentRepository.context(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId)
        .map { ctx =>
          Ok(views.html.pages.enrollments.newEnrollment(
            enrollmentForm.fill(FormFactory.emptyFormData[EnrollmentFormData, Enrollment]),
            ctx.department,
            ctx.program,
            ctx.plan,
            ctx.training,
            ctx.trainingPlan,
            ctx.trainingSession,
            ctx.accounts
          ))
        }
    }

  def create(
              departmentId: Long,
              programId: Long,
              planId: Long,
              trainingId: Long,
              trainingPlanId: Long,
              trainingSessionId: Long
            ) = {

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      enrollmentRepository.context(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId)
        .flatMap { ctx =>
          enrollmentForm.bindFromRequest.fold(
            formWithErrors => Future {
              BadRequest(views.html.pages.enrollments.newEnrollment(
                formWithErrors,
                ctx.department,
                ctx.program,
                ctx.plan,
                ctx.training,
                ctx.trainingPlan,
                ctx.trainingSession,
                ctx.accounts
              ))
            },

            data => {
              enrollmentRepository.addToSession(data)
                .map { enrollmentId => {
                  // send email
                  val fut = enrollmentRepository.findById(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, data.accountId, enrollmentId.value)
                  fut.onComplete {
                    case Success(result) =>
                      val from = config.get[String]("schoolman.mailing.from")
                      val to = Seq(s"${result.account.fullName} <${result.account.email}>")
                      val message = s"""
                        <html>
                          <body>
                            <p>Dear ${result.account.fullName}</p>

                            <p>Your application for:</p>

                            <ul>
                              <li>Department: ${result.department.title}</li>
                              <li>Program: ${result.program.title}</li>
                              <li>Plan: ${result.plan.title}</li>
                              <li>Training: ${result.training.title}</li>
                              <li>Session: ${result.trainingSession.title}</li>
                              <li>Start date: ${result.trainingSession.startDate}</li>
                              <li>Duration: ${result.trainingPlan.duration}</li>
                            </ul>

                            <p>has been <strong>Approved</strong></p>

                            <p>
                              Please login into your account on our
                              <a href="${config.get[String]("schoolman.student-portal")}/signIn" target="_blank">Student Portal</a>
                              so you can manage your profile and finances.
                              <br />
                            </p>

                            <p>Sincerely, SchoolMan</p>
                          </body>
                        </html>
                      """
                      mailer.sendEmail("Application Approved", from, to, message)
                  }

                  Redirect(controllers.routes.EnrollmentController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, data.accountId, enrollmentId.value))
                    .flashing("info" -> "Enrollment was successfully created")
                }}

                .recover { case ex =>
                  Redirect(controllers.routes.SessionController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId))
                    .flashing("danger" -> s"Could not create enrollment: ${ex.getMessage}")
                }
            }
          )
        }
    }
  }

  def update(
              departmentId: Long,
              programId: Long,
              planId: Long,
              trainingId: Long,
              trainingPlanId: Long,
              trainingSessionId: Long,
              accountId: Long,
              id: Long
            ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      enrollmentRepository.findById(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, id)
        .flatMap { e =>
          enrollmentForm.bindFromRequest.fold(
            formWithErrors => Future {
              BadRequest(views.html.pages.enrollments.edit(
                formWithErrors,
                e.department,
                e.program,
                e.plan,
                e.training,
                e.trainingPlan,
                e.trainingSession,
                e.account
              ))
            },

            data => {
              enrollmentRepository.save(data)
                .map { _ =>
                  Redirect(controllers.routes.EnrollmentController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, id))
                    .flashing("info" -> "Enrollment was successfully updated")
                }
                .recover { case ex =>
                  Redirect(controllers.routes.EnrollmentController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, id))
                    .flashing("danger" -> s"Could not update enrollment: ${ex.getMessage}")
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
            id: Long
          ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      enrollmentRepository.findById(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, id)
        .map { e => Ok(views.html.pages.enrollments.show(
          e.value,
          e.department,
          e.program,
          e.plan,
          e.training,
          e.trainingPlan,
          e.trainingSession,
          e.account,
          e.bills,
          e.dueAmount
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
            id: Long
          ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      enrollmentRepository.findById(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, id)
        .map { e => Ok(views.html.pages.enrollments.edit(
          enrollmentForm.fill(FormFactory.formDataFrom[EnrollmentFormData, Enrollment](e.value)),
          e.department,
          e.program,
          e.plan,
          e.training,
          e.trainingPlan,
          e.trainingSession,
          e.account
        ))}
    }

  def delete(
              departmentId: Long,
              programId: Long,
              planId: Long,
              trainingId: Long,
              trainingPlanId: Long,
              trainingSessionId: Long,
              accountId: Long,
              id: Long
            ) =

    SecuredAction.andThen(IsAdmin).async { implicit request =>
      enrollmentRepository.delete(id)
        .map { _ =>
          Redirect(controllers.routes.SessionController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId))
            .flashing("info" -> "Enrollment was successfully deleted")
        }
        .recover { case ex =>
          Redirect(controllers.routes.EnrollmentController.show(departmentId, programId, planId, trainingId, trainingPlanId, trainingSessionId, accountId, id))
            .flashing("danger" -> s"Could not delete enrollment: ${ex.getMessage}")
        }
    }
}
