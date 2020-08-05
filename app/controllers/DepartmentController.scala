package controllers

import controllers.formfactories._
import javax.inject._
import models.Department
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.DepartmentRepository
import utils.ActionFunctions._
import utils.actions._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DepartmentController @Inject()(
                                      departmentRepository: DepartmentRepository,
                                      cc: ControllerComponents,
                                      SecuredAction: SecuredAction
                                    )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  val departmentForm = FormFactory.form[DepartmentFormData, Department]

  def newDepartment = SecuredAction.andThen(IsAdmin) { implicit request =>
    Ok(views.html.pages.catalog.departments.newDepartment(
      departmentForm.fill(FormFactory.emptyFormData[DepartmentFormData, Department])
    ))
  }

  def create = SecuredAction.andThen(IsAdmin).async { implicit request =>
    departmentForm.bindFromRequest.fold(
      formWithErrors => {
        Future { BadRequest(views.html.pages.catalog.departments.newDepartment(formWithErrors)) }
      },
      data => {
        departmentRepository.add(data)
          .map { departmentId =>
            Redirect(controllers.routes.DepartmentController.show(departmentId.value))
              .flashing("info" -> "The new department was successfully created.")
          }
          .recover { case ex =>
            Redirect(controllers.routes.DepartmentController.list())
              .flashing("danger" -> s"Could not create department: ${ex.getMessage}")
          }
      }
    )
  }

  def list = SecuredAction.andThen(IsAdmin).async { implicit request =>
    departmentRepository.all
      .map { departments => Ok(views.html.pages.catalog.departments.list(departments)) }
  }

  def show(id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    departmentRepository.findById(id)
      .map { dep => Ok(views.html.pages.catalog.departments.show(dep.value, dep.programs)) }
  }

  def edit(id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    departmentRepository.findById(id)
      .map { dep => Ok(views.html.pages.catalog.departments.edit(departmentForm.fill(FormFactory.formDataFrom[DepartmentFormData, Department](dep.value)))) }
  }

  def update(id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    departmentForm.bindFromRequest.fold(
      formWithErrors => Future { BadRequest(views.html.pages.catalog.departments.edit(formWithErrors)) },

      data => {
        departmentRepository.save(data)
          .map { departmentId =>
            Redirect(controllers.routes.DepartmentController.show(departmentId.value))
              .flashing("info" -> "The department was successfully updated")
          }
          .recover { case ex =>
            Redirect(controllers.routes.DepartmentController.show(data.id.get))
              .flashing("danger" -> s"Could not update department: ${ex.getMessage}")
          }
      }
    )
  }

  def delete(id: Long) = SecuredAction.andThen(IsAdmin).async { implicit request =>
    departmentRepository.delete(id)
      .map { _ =>
        Redirect(controllers.routes.DepartmentController.list())
          .flashing("info" -> "The department was successfully deleted")
      }
      .recover { case ex =>
        Redirect(controllers.routes.DepartmentController.show(id))
          .flashing("danger" -> s"Could not delete department: ${ex.getMessage}")
      }
  }
}
