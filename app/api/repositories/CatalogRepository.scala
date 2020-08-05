package api.repositories

import api.dto.{TrainingPlanView, TrainingSessionView, TrainingView}
import javax.inject._
import models._
import api.dto._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repositories.Tables
import slick.jdbc.JdbcProfile
import utils.PK

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CatalogRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with Tables {

  import profile.api._

  def getDepartments: Future[Seq[DepartmentView]] =
    db.run(
      departments.sortBy(_.position)
        .joinLeft(programs.sortBy(_.position))
        .on(_.id === _.departmentId)
        .result
        .map(_.groupBy(_._1).toSeq)
        .map(items => items.map { case (dep, rows) =>
          DepartmentView(dep, rows.map(_._2).filter(_.isDefined).map(_.get))
        })
    )

  def getTrainingSummariesForProgram(programId: Long): Future[Seq[Training]] =
    db.run(trainings.filter(_.programId === PK[Program](programId)).sortBy(_.position).result)

  def getTrainingById(id: Long): Future[TrainingView] =
    for {
      training <- db.run(trainings.filter(_.id === PK[Training](id)).result.head)
      tView <- trainingToTrainingView(training)
    } yield tView

  def getDepartmentForProgram(programId: Long): Future[DepartmentView] =
    for {
      dep <- db.run(programs.filter(_.id === PK[Program](programId))
        .join(departments)
        .on(_.departmentId === _.id)
        .map(_._2).result.head)

      depView <- departmentToDepartmentView(dep)
    } yield depView

  def getProgramForTraining(trainingId: Long): Future[Program] =
    db.run(
      trainings.filter(_.id === PK[Training](trainingId))
        .join(programs)
        .on(_.programId === _.id)
        .map(_._2)
        .result
        .head
    )

  def getTrainingForTrainingPlan(trainingPlanId: Long): Future[TrainingView] =
    for {
      training <- db.run(
          trainingPlans.filter(_.id === PK[TrainingPlan] (trainingPlanId))
          .join(trainings)
          .on(_.trainingId === _.id)
          .map(_._2)
          .result
          .head
        )

      tView <- trainingToTrainingView(training)
    } yield tView

  def getTrainingPlanForTrainingSession(sessionId: Long): Future[TrainingPlanView] =
    for {
      tp <- db.run(
        trainingSessions.filter(_.id === PK[TrainingSession](sessionId))
          .join(trainingPlans)
          .on(_.trainingPlanId === _.id)
          .map(_._2)
          .result
          .head
      )
      tpView <- tpToTpView(tp)
    } yield tpView

  def getTrainingSession(sessionId: Long): Future[TrainingSessionView] =
    for {
      ts <- db.run(trainingSessions.filter(_.id === PK[TrainingSession](sessionId)).result.head)
      fees <- db.run(trainingSessionFees.filter(_.trainingSessionId === ts.id).sortBy(_.position).result)
    } yield TrainingSessionView(ts, fees)

  private def tpToTpView(tp: TrainingPlan): Future[TrainingPlanView] =
    for {
      (title, description) <- db.run(plans.filter(_.id === tp.planId).map(p => (p.title, p.description)).result.head)
      fees <- db.run(trainingFees.filter(_.trainingPlanId === tp.id).sortBy(_.position).result)
      sess <-
        db.run(trainingSessions.filter(ts => ts.trainingPlanId === tp.id && ts.status.inSet(Seq(TrainingSessionState.Opened))).result)
          .flatMap(items2 =>
            Future.sequence(
              items2.map { ts =>
                db.run(trainingSessionFees.filter(_.trainingSessionId === ts.id).sortBy(_.position).result)
                  .map(fees => TrainingSessionView(ts, fees))
              }
            )
          )
    } yield TrainingPlanView(tp, title, description, fees, sess)

  private def departmentToDepartmentView(dep: Department): Future[DepartmentView] =
    for {
      progs <- db.run(programs.filter(_.departmentId === dep.id).sortBy(_.position).result)
    } yield DepartmentView(value = dep, programs = progs)

  private def trainingToTrainingView(training: Training): Future[TrainingView] =
    for {
      unts <- db.run(trainingUnits.filter(_.trainingId === training.id).sortBy(_.position).result)
      tps <- db.run(trainingPlans.filter(_.trainingId === training.id).result)
        .flatMap(items =>
          Future.sequence(
            items.map { tp => tpToTpView(tp) }
          )
        )
    } yield TrainingView(training, unts, tps)
}
