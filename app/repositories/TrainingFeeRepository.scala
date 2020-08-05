package repositories

import controllers.formfactories.TrainingFeeFormData
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import models._
import utils.PK

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class TrainingFeeRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with Tables  {

  import profile.api._

  case class TrainingFeeGraph
  (
    value: TrainingFee,
    department: Department,
    program: Program,
    plan: Plan,
    training: Training,
    trainingPlan: TrainingPlan
  )

  case class TrainingFeeContext
  (
    department: Department,
    program: Program,
    plan: Plan,
    training: Training,
    trainingPlan: TrainingPlan
  )

  def findById(
                departmentId: Long,
                programId: Long,
                planId: Long,
                trainingId: Long,
                trainingPlanId: Long,
                id: Long
              ): Future[TrainingFeeGraph] = {

    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val planAction = plans.filter(_.id === PK[Plan](planId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head
    val tpAction = trainingPlans.filter(_.id === PK[TrainingPlan](trainingPlanId)).result.head
    val tfAction = trainingFees.filter(_.id === PK[TrainingFee](id)).result.head

    db.run(departmentAction zip programAction zip planAction zip trainingAction zip tpAction zip tfAction)
      .map { case (((((department, program), plan), training), tp), tf) => TrainingFeeGraph(tf, department, program, plan, training, tp) }
  }

  def context(
               departmentId: Long,
               programId: Long,
               planId: Long,
               trainingId: Long,
               trainingPlanId: Long
             ): Future[TrainingFeeContext] = {

    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val planAction = plans.filter(_.id === PK[Plan](planId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head
    val tpAction = trainingPlans.filter(_.id === PK[TrainingPlan](trainingPlanId)).result.head

    db.run(departmentAction zip programAction zip planAction zip trainingAction zip tpAction)
      .map { case ((((department, program), plan), training), tp) => TrainingFeeContext(department, program, plan, training, tp) }
  }

  def addToTrainingPlan(trainingPlanId: Long, data: TrainingFeeFormData): Future[PK[TrainingFee]] =
    db.run(trainingFees.returning(trainingFees.map(_.id)) += TrainingFee(
      trainingPlanId = PK[TrainingPlan](trainingPlanId),
      description = data.description,
      feeType = FeeType.withName(data.feeType),
      amount = data.amount,
      promotionalAmount = data.promotionalAmount,
      optional = data.optional,
      position = Some(data.position)
    ))

  def save(data: TrainingFeeFormData): Future[PK[TrainingFee]] =
    db.run(
      trainingFees
        .filter(_.id === PK[TrainingFee](data.id.get))
        .map { d => (d.description, d.feeType, d.amount, d.promotionalAmount, d.optional, d.position) }
        .update((data.description, FeeType.withName(data.feeType), data.amount, data.promotionalAmount, data.optional, Some(data.position)))
    ).map(_ => PK[TrainingFee](data.id.get))

  def delete(id: Long): Future[Unit] =
    db.run(trainingFees.filter(_.id === PK[TrainingFee](id)).delete).map(_ => ())
}
