package repositories

import controllers.formfactories.PaymentFormData
import javax.inject.{Inject, Singleton}
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import utils.PK

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with Tables  {

  import profile.api._

  case class PaymentGraph
  (
    value: Payment,
    department: Department,
    program: Program,
    plan: Plan,
    training: Training,
    trainingPlan: TrainingPlan,
    trainingSession: TrainingSession,
    account: Account,
    enrollment: Enrollment,
    bill: Bill
  )

  case class PaymentContext
  (
    department: Department,
    program: Program,
    plan: Plan,
    training: Training,
    trainingPlan: TrainingPlan,
    trainingSession: TrainingSession,
    account: Account,
    enrollment: Enrollment,
    bill: Bill,
    fee: TrainingSessionFee
  )

  def findById(
                departmentId: Long,
                programId: Long,
                planId: Long,
                trainingId: Long,
                trainingPlanId: Long,
                trainingSessionId: Long,
                accountId: Long,
                enrollmentId: Long,
                billId: Long,
                id: Long
              ): Future[PaymentGraph] = {

    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val planAction = plans.filter(_.id === PK[Plan](planId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head
    val tpAction = trainingPlans.filter(_.id === PK[TrainingPlan](trainingPlanId)).result.head
    val tsessAction = trainingSessions.filter(_.id === PK[TrainingSession](trainingSessionId)).result.head
    val accAction = accounts.filter(_.id === PK[Account](accountId)).result.head
    val enrol = enrollments.filter(_.id === PK[Enrollment](enrollmentId)).result.head
    val billAction = bills.filter(_.id === PK[Bill](billId)).result.head
    val payAct = payments.filter(_.id === PK[Payment](id)).result.head

    db.run(departmentAction zip programAction zip planAction zip trainingAction zip tpAction zip tsessAction zip accAction zip enrol zip billAction zip payAct)
      .map { case (((((((((department, program), plan), training), tp), tsess), acc), enr), bil), pay) =>
        PaymentGraph(pay, department, program, plan, training, tp, tsess, acc, enr, bil)
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
               enrollmentId: Long,
               billId: Long
             ): Future[PaymentContext] = {

    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val planAction = plans.filter(_.id === PK[Plan](planId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head
    val tpAction = trainingPlans.filter(_.id === PK[TrainingPlan](trainingPlanId)).result.head
    val tsessAction = trainingSessions.filter(_.id === PK[TrainingSession](trainingSessionId)).result.head
    val accAct = accounts.filter(_.id === PK[Account](accountId)).result.head
    val enrAct = enrollments.filter(_.id === PK[Enrollment](enrollmentId)).result.head
    val billAct = bills.filter(_.id === PK[Bill](billId)).result.head
    val feeAct = bills.filter(_.id === PK[Bill](billId)).join(trainingSessionFees).on(_.trainingSessionFeeId === _.id).map(_._2).result.head


    db.run(departmentAction zip programAction zip planAction zip trainingAction zip tpAction zip tsessAction zip accAct zip enrAct zip billAct zip feeAct)
      .map { case (((((((((department, program), plan), training), tp), tsess), acc), enr), bil), fee) =>
        PaymentContext(department, program, plan, training, tp, tsess, acc, enr, bil, fee)
      }
  }

  def addToBill(data: PaymentFormData): Future[PK[Payment]] =
    db.run(
      payments.returning(payments.map(_.id)) += Payment(
        billId = PK[Bill](data.billId),
        amount = data.amount,
        date = data.date
      )
    )

  def delete(id: Long): Future[Unit] = {
    db.run(payments.filter(_.id === PK[Payment](id)).delete).map(_ => ())
  }
}
