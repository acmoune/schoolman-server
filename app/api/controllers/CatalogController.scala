package api.controllers

import api.repositories.CatalogRepository
import javax.inject._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class CatalogController @Inject()(cc: ControllerComponents,
                                  catalogRepository: CatalogRepository
                                 )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def getDepartments = Action.async { implicit request =>
    catalogRepository.getDepartments.map { deps => Ok(Json.toJson(deps)) }
  }

  def getTrainings(programId: Long) = Action.async { implicit request =>
    catalogRepository.getTrainingSummariesForProgram(programId).map { trainings => Ok(Json.toJson(trainings)) }
  }

  def getTraining(trainingId: Long) = Action.async { implicit request =>
    catalogRepository.getTrainingById(trainingId).map { training => Ok(Json.toJson(training)) }
  }

  def getDepartmentForProgram(programId: Long) = Action.async { implicit request =>
    catalogRepository.getDepartmentForProgram(programId)
      .map { department => Ok(Json.toJson(department)) }
  }

  def getProgramForTraining(trainingId: Long) = Action.async { implicit request =>
    catalogRepository.getProgramForTraining(trainingId)
      .map { program => Ok(Json.toJson(program)) }
  }

  def getTrainingForTrainingPlan(tpId: Long) = Action.async { implicit request =>
    catalogRepository.getTrainingForTrainingPlan(tpId)
      .map { t => Ok(Json.toJson(t)) }
  }

  def getTrainingPlanForTrainingSession(tsId: Long) = Action.async { implicit request =>
    catalogRepository.getTrainingPlanForTrainingSession(tsId)
      .map { tp => Ok(Json.toJson(tp)) }
  }

  def getTrainingSession(tsId: Long) = Action.async { implicit request =>
    catalogRepository.getTrainingSession(tsId).map { ts => Ok(Json.toJson(ts))}
  }
}
