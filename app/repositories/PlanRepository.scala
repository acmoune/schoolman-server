package repositories

import com.google.inject.Inject
import controllers.formfactories.PlanFormData
import javax.inject.Singleton
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import utils.PK
import models._
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class PlanRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
  with Tables
  with Logging {

  import profile.api._

  type Title = String
  type Duration = String

  case class PlanGraph(value: Plan, department: Department, program: Program, trainingPlans: Seq[(PK[TrainingPlan], Title, Duration, PK[Training])])
  case class PlanContext(department: Department, program: Program)

  case class TrainingPlanGraph
  (
    value: TrainingPlan,
    department: Department,
    program: Program,
    plan: Plan,
    training: Training,
    trainingFees: Seq[TrainingFee],
    trainingSessions: Seq[TrainingSession]
  )

  def findById(departmentId: Long, programId: Long, id: Long): Future[PlanGraph] = {
    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val planAction = plans.filter(_.id === PK[Plan](id)).result.head
    val tpAction = trainingPlans.filter(_.planId === PK[Plan](id)).join(trainings).on(_.trainingId === _.id).map(r => (r._1.id, r._2.title, r._1.duration, r._2.id)).result

    db.run(departmentAction zip programAction zip planAction zip tpAction)
      .map { case (((department, program), plan), tps) => PlanGraph(plan, department, program, tps) }
  }

  def context(departmentId: Long, programId: Long): Future[PlanContext] = {
    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head

    db.run(departmentAction zip programAction)
      .map { case (department, program) => PlanContext(department, program) }
  }

  def addToProgram(programId: Long, data: PlanFormData): Future[PK[Plan]] =
    db.run(plans.returning(plans.map(_.id)) += Plan(programId=PK[Program](programId), title=data.title, description=data.description))

  def save(data: PlanFormData): Future[PK[Plan]] =
    db.run(
      plans
        .filter(_.id === PK[Plan](data.id.get))
        .map { d => (d.title, d.description) }
        .update((data.title, data.description))
    ).map { _ => PK[Plan](data.id.get) }

  def delete(id: Long): Future[Unit] =
    db.run(plans.filter(_.id === PK[Plan](id)).delete).map(_ => ())

  def matchTrainings(planId: Long, programId: Long): Future[Seq[(Training, Option[TrainingPlan])]] = {
    db.run(
      trainings
        .filter(_.programId === PK[Program](programId))
        .joinLeft(trainingPlans)
        .on((t, tp) => (t.id === tp.trainingId) && (tp.planId === PK[Plan](planId)))
        .result
    )
  }

  def addTrainings(planId: Long, trainingIds: Seq[Long]): Future[Unit] = {
    // logger.info("PLAN" + planId.toString + "############## TRAINING IDS #########" + trainingIds.toString)

    val actions = trainingIds
      .map(id => trainingPlans += TrainingPlan(trainingId = PK[Training](id), planId = PK[Plan](planId), duration = ""))
      .fold(DBIO.successful(0))((acc, action) => acc.andThen(action))

    db.run(actions.transactionally).map(_ => ())
  }

  def updateTrainingDuration(trainingPlanId: Long, duration: String): Future[Unit] =
    db.run(trainingPlans.filter(_.id === PK[TrainingPlan](trainingPlanId)).map(d => d.duration).update(duration)).map(_ => ())

  def deleteTrainingPlan(trainingPlanId: Long): Future[Unit] =
    db.run(trainingPlans.filter(_.id === PK[TrainingPlan](trainingPlanId)).delete).map(_ => ())

  def findTrainingPlanById(departmentId: Long, programId: Long, planId: Long, trainingId: Long, id: Long): Future[TrainingPlanGraph] = {
    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val planAction = plans.filter(_.id === PK[Plan](planId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head
    val tpAction = trainingPlans.filter(_.id === PK[TrainingPlan](id)).result.head
    val tfsAction = trainingFees.filter(_.trainingPlanId === PK[TrainingPlan](id)).sortBy(_.position).result
    val tsessAction = trainingSessions.filter(_.trainingPlanId === PK[TrainingPlan](id)).sortBy(_.id.desc).result

    db.run(departmentAction zip programAction zip planAction zip trainingAction zip tpAction zip tfsAction zip tsessAction)
      .map { case ((((((department, program), plan), training), tp), tfs), tsess) =>
        TrainingPlanGraph(tp, department, program, plan, training, tfs, tsess)
      }
  }
}
