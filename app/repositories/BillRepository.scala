package repositories

import controllers.formfactories.BillFormData
import javax.inject.{Inject, Singleton}
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import utils.PK

import scala.concurrent.{ExecutionContext, Future}

case class BillGraph
(
  value: Bill,
  department: Department,
  program: Program,
  plan: Plan,
  training: Training,
  trainingPlan: TrainingPlan,
  trainingSession: TrainingSession,
  account: Account,
  enrollment: Enrollment,
  payments: Seq[Payment],
  fee: TrainingSessionFee
)

case class BillContext
(
  department: Department,
  program: Program,
  plan: Plan,
  training: Training,
  trainingPlan: TrainingPlan,
  trainingSession: TrainingSession,
  account: Account,
  enrollment: Enrollment,
  fees: Seq[(TrainingSessionFee, Option[Bill])]
)

@Singleton
class BillRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with Tables  {

  import profile.api._

  def findById(
                departmentId: Long,
                programId: Long,
                planId: Long,
                trainingId: Long,
                trainingPlanId: Long,
                trainingSessionId: Long,
                accountId: Long,
                enrollmentId: Long,
                id: Long
              ): Future[BillGraph] = {

    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val planAction = plans.filter(_.id === PK[Plan](planId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head
    val tpAction = trainingPlans.filter(_.id === PK[TrainingPlan](trainingPlanId)).result.head
    val tsessAction = trainingSessions.filter(_.id === PK[TrainingSession](trainingSessionId)).result.head
    val accAction = accounts.filter(_.id === PK[Account](accountId)).result.head
    val enrol = enrollments.filter(_.id === PK[Enrollment](enrollmentId)).result.head
    val billAction = bills.filter(_.id === PK[Bill](id)).result.head
    val payAct = payments.filter(_.billId === PK[Bill](id)).result
    val feeAct = bills.filter(_.id === PK[Bill](id)).join(trainingSessionFees).on(_.trainingSessionFeeId === _.id).map(_._2).result.head

    db.run(departmentAction zip programAction zip planAction zip trainingAction zip tpAction zip tsessAction zip accAction zip enrol zip billAction zip payAct zip feeAct)
      .map { case ((((((((((department, program), plan), training), tp), tsess), acc), enr), bil), pays), fee) =>
        BillGraph(bil, department, program, plan, training, tp, tsess, acc, enr, pays, fee)
      }
  }

  def context(
               departmentId: Long,
               programId: Long,
               planId: Long,
               trainingId: Long,
               trainingPlanId: Long,
               trainingSessionId: Long,
               accountId: Long,
               enrollmentId: Long
             ): Future[BillContext] = {

    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val planAction = plans.filter(_.id === PK[Plan](planId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head
    val tpAction = trainingPlans.filter(_.id === PK[TrainingPlan](trainingPlanId)).result.head
    val tsessAction = trainingSessions.filter(_.id === PK[TrainingSession](trainingSessionId)).result.head
    val accAct = accounts.filter(_.id === PK[Account](accountId)).result.head
    val enrAct = enrollments.filter(_.id === PK[Enrollment](enrollmentId)).result.head
    val feesAct = trainingSessionFees.filter(_.trainingSessionId === PK[TrainingSession](trainingSessionId))
        .sortBy(_.feeType)
        .joinLeft(bills)
        .on((tsf, b) => tsf.id === b.trainingSessionFeeId && b.enrollmentId === PK[Enrollment](enrollmentId))
        .result

    db.run(departmentAction zip programAction zip planAction zip trainingAction zip tpAction zip tsessAction zip accAct zip enrAct zip feesAct)
      .map { case ((((((((department, program), plan), training), tp), tsess), acc), enr), fees) =>
        BillContext(department, program, plan, training, tp, tsess, acc, enr, fees)
      }
  }

  def addToEnrollment(data: BillFormData): Future[PK[Bill]] =
    db.run(
      bills.returning(bills.map(_.id)) += Bill(
        trainingSessionFeeId = PK[TrainingSessionFee](data.trainingSessionFeeId),
        enrollmentId = PK[Enrollment](data.enrollmentId),
        amount = data.amount
      )
    )

  def save(data: BillFormData): Future[PK[Bill]] =
    db.run(
      bills
        .filter(_.id === PK[Bill](data.id.get))
        .map { d => (d.amount) }
        .update(data.amount)
    ).map(_ => PK[Bill](data.id.get))

  def delete(id: Long): Future[Unit] = {
    db.run(bills.filter(_.id === PK[Bill](id)).delete).map(_ => ())
  }

  def getBill(billId: Long): Future[BillGraph] =
    for {
      bill <- db.run(bills.filter(_.id === PK[Bill](billId)).result.head)
      enr <- db.run(enrollments.filter(_.id === bill.enrollmentId).result.head)
      tsess <- db.run(trainingSessions.filter(_.id === enr.trainingSessionId).result.head)
      tp <- db.run(trainingPlans.filter(_.id === tsess.trainingPlanId).result.head)
      t <- db.run(trainings.filter(_.id === tp.trainingId).result.head)
      plan <- db.run(plans.filter(_.id === tp.planId).result.head)
      pro <- db.run(programs.filter(_.id === plan.programId).result.head)
      dep <- db.run(departments.filter(_.id === pro.departmentId).result.head)
      account <- db.run(accounts.filter(_.id === enr.accountId).result.head)
      pays <- db.run(payments.filter(_.billId === bill.id).result)
      fee <- db.run(trainingSessionFees.filter(_.id === bill.trainingSessionFeeId).result.head)
    } yield BillGraph(bill, dep, pro, plan, t, tp, tsess, account, enr, pays, fee)
}
