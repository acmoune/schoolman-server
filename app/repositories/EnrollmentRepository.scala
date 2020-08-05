package repositories

import java.util.Locale

import controllers.formfactories.EnrollmentFormData
import javax.inject._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import models._
import utils.PK

case class EnrollmentGraph
(
  value: Enrollment,
  department: Department,
  program: Program,
  plan: Plan,
  training: Training,
  trainingPlan: TrainingPlan,
  trainingSession: TrainingSession,
  account: Account,
  bills: Seq[(Bill, String, Option[Double])],
  dueAmount: Double
)

@Singleton
class EnrollmentRepository @Inject()(
                                      protected val dbConfigProvider: DatabaseConfigProvider
                                    )(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with Tables  {

  import profile.api._

  case class EnrollmentContext
  (
    department: Department,
    program: Program,
    plan: Plan,
    training: Training,
    trainingPlan: TrainingPlan,
    trainingSession: TrainingSession,
    accounts: Seq[(Account, Option[Enrollment])]
  )

  def findById(
                departmentId: Long,
                programId: Long,
                planId: Long,
                trainingId: Long,
                trainingPlanId: Long,
                trainingSessionId: Long,
                accountId: Long,
                id: Long
              ): Future[EnrollmentGraph] = {

    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val planAction = plans.filter(_.id === PK[Plan](planId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head
    val tpAction = trainingPlans.filter(_.id === PK[TrainingPlan](trainingPlanId)).result.head
    val tsessAction = trainingSessions.filter(_.id === PK[TrainingSession](trainingSessionId)).result.head
    val accAction = accounts.filter(_.id === PK[Account](accountId)).result.head
    val enrol = enrollments.filter(_.id === PK[Enrollment](id)).result.head
    val billsAction = bills
      .join(trainingSessionFees)
      .on((b, tsf) => b.trainingSessionFeeId === tsf.id && b.enrollmentId === PK[Enrollment](id))
      .sortBy(_._2.position)
      .map(res => (res._1, res._2.description, payments.filter(_.billId === res._1.id).map(_.amount).sum))
      .result

    val totalBills = bills.filter(_.enrollmentId === PK[Enrollment](id)).map(_.amount).sum.result
    val totalPayments = payments.join(bills).on((p, b) => p.billId === b.id && b.enrollmentId === PK[Enrollment](id)).map(_._1.amount).sum.result

    db.run(departmentAction zip programAction zip planAction zip trainingAction zip tpAction zip tsessAction zip accAction zip enrol zip billsAction zip (totalBills zip totalPayments))
      .map { case (((((((((department, program), plan), training), tp), tsess), acc), enr), bis), totals) =>
        EnrollmentGraph(enr, department, program, plan, training, tp, tsess, acc, bis, totals._1.getOrElse(0.0) - totals._2.getOrElse(0.0))
      }
  }

  def context(
               departmentId: Long,
               programId: Long,
               planId: Long,
               trainingId: Long,
               trainingPlanId: Long,
               trainingSessionId: Long
             ): Future[EnrollmentContext] = {

    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val planAction = plans.filter(_.id === PK[Plan](planId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head
    val tpAction = trainingPlans.filter(_.id === PK[TrainingPlan](trainingPlanId)).result.head
    val tsessAction = trainingSessions.filter(_.id === PK[TrainingSession](trainingSessionId)).result.head
    val accsAct = accounts
      .sortBy(_.fullName)
      .joinLeft(enrollments)
      .on((a, e) => a.id === e.accountId && e.trainingSessionId === PK[TrainingSession](trainingSessionId))
      .result

    db.run(departmentAction zip programAction zip planAction zip trainingAction zip tpAction zip tsessAction zip accsAct)
      .map { case ((((((department, program), plan), training), tp), tsess), accs) =>
        EnrollmentContext(department, program, plan, training, tp, tsess, accs)
      }
  }

  def addToSession(data: EnrollmentFormData): Future[PK[Enrollment]] = {
    val addEnrollmentAction =
      enrollments.returning(enrollments.map(_.id)) += Enrollment(
        accountId = PK[Account](data.accountId),
        trainingSessionId = PK[TrainingSession](data.trainingSessionId),
        notes = data.notes
      )

    val createBillAction = (enrollmentId: PK[Enrollment], sessionFee: TrainingSessionFee) =>
      bills.returning(bills.map(_.id)) += Bill(
        trainingSessionFeeId = sessionFee.id,
        enrollmentId = enrollmentId,
        amount = sessionFee.promotionalAmount.getOrElse(sessionFee.amount)
      )

    for {
      enrId <- db.run(addEnrollmentAction)
      mandatorySessionFees <- db.run(trainingSessionFees.filter(f => f.trainingSessionId === PK[TrainingSession](data.trainingSessionId) && f.optional === false ).result)
      _ <- db.run(
        mandatorySessionFees
          .map(createBillAction(enrId, _))
          .fold(DBIO.successful(0))((acc, action) => acc.andThen(action))
          .transactionally
      )
    } yield enrId
  }

  def save(data: EnrollmentFormData): Future[PK[Enrollment]] =
    db.run(
      enrollments
        .filter(_.id === PK[Enrollment](data.id.get))
        .map { d => (d.notes) }
        .update(data.notes)
    ).map(_ => PK[Enrollment](data.id.get))

  def delete(id: Long): Future[Unit] = {
    db.run(enrollments.filter(_.id === PK[Enrollment](id)).delete).map(_ => ())
  }
}
