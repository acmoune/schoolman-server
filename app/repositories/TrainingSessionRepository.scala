package repositories

import controllers.formfactories.TrainingSessionFormData
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import models._
import utils.PK

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TrainingSessionRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with Tables  {

  import profile.api._

  case class TrainingSessionGraph
  (
    value: TrainingSession,
    department: Department,
    program: Program,
    plan: Plan,
    training: Training,
    trainingPlan: TrainingPlan,
    fees: Seq[TrainingSessionFee],
    enrollments: Seq[(Enrollment, Account, Boolean)]
  )

  case class TrainingSessionContext
  (
    department: Department,
    program: Program,
    plan: Plan,
    training: Training,
    trainingPlan: TrainingPlan
  )

  case class TrainingSessionEnvironment(trainingTitle: String, sessionTitle: String, emails: Seq[String])

  def findById(
                departmentId: Long,
                programId: Long,
                planId: Long,
                trainingId: Long,
                trainingPlanId: Long,
                id: Long
              ): Future[TrainingSessionGraph] = {

    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val planAction = plans.filter(_.id === PK[Plan](planId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head
    val tpAction = trainingPlans.filter(_.id === PK[TrainingPlan](trainingPlanId)).result.head
    val tsessAction = trainingSessions.filter(_.id === PK[TrainingSession](id)).result.head
    val fees = trainingSessionFees.filter(_.trainingSessionId === PK[TrainingSession](id)).sortBy(_.position).result
    val enrsAction = enrollments.filter(_.trainingSessionId === PK[TrainingSession](id))
      .join(accounts)
      .on((e, a) => e.accountId === a.id)
      .sortBy(_._2.fullName)
      .result

    db.run(departmentAction zip programAction zip planAction zip trainingAction zip tpAction zip tsessAction zip fees zip enrsAction)
      .flatMap { case (((((((department, program), plan), training), tp), tsess), fees), enrs) => {

        val totals: Seq[Future[(Option[Double], Option[Double])]] = enrs.map { enr =>
          val totalBills = bills.filter(_.enrollmentId === enr._1.id).map(_.amount).sum.result
          val totalPayments = payments.join(bills).on((p, b) => p.billId === b.id && b.enrollmentId === enr._1.id).map(_._1.amount).sum.result
          db.run(totalBills zip totalPayments)
        }

        Future.sequence(totals).map { tots => {
          val status = tots.map(t => t._2.getOrElse(0.0) >= t._1.getOrElse(0.0))
          val newEnrs = enrs.zip(status)
          TrainingSessionGraph(tsess, department, program, plan, training, tp, fees, newEnrs.map(e => (e._1._1, e._1._2, e._2)))
        }}
      }}
  }

  def context(
               departmentId: Long,
               programId: Long,
               planId: Long,
               trainingId: Long,
               trainingPlanId: Long
             ): Future[TrainingSessionContext] = {

    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val planAction = plans.filter(_.id === PK[Plan](planId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head
    val tpAction = trainingPlans.filter(_.id === PK[TrainingPlan](trainingPlanId)).result.head

    db.run(departmentAction zip programAction zip planAction zip trainingAction zip tpAction)
      .map { case ((((department, program), plan), training), tp) => TrainingSessionContext(department, program, plan, training, tp) }
  }

  def addToTrainingPlan(trainingPlanId: Long, data: TrainingSessionFormData) = {
    val addSessionAction =
      trainingSessions.returning(trainingSessions.map(_.id)) += TrainingSession(
        trainingPlanId = PK[TrainingPlan](trainingPlanId),
        title = data.title,
        startDate = data.startDate,
        status = TrainingSessionState.withName(data.status)
      )

    val createSessionFeeAction = (sessionId: PK[TrainingSession], fee: TrainingFee) =>
      trainingSessionFees.returning(trainingSessionFees.map(_.id)) += TrainingSessionFee(
        trainingSessionId = sessionId,
        description = fee.description,
        feeType = fee.feeType,
        amount = fee.amount,
        promotionalAmount = fee.promotionalAmount,
        optional = fee.optional,
        position = fee.position
      )

    for {
      sessionId <- db.run(addSessionAction)
      fees <- db.run(trainingFees.filter(_.trainingPlanId === PK[TrainingPlan](trainingPlanId)).result)
      _ <- db.run(
        fees
          .map(createSessionFeeAction(sessionId, _))
          .fold(DBIO.successful(0))((acc, action) => acc.andThen(action))
          .transactionally
      )
    } yield sessionId
  }

  def save(data: TrainingSessionFormData): Future[PK[TrainingSession]] =
    db.run(
      trainingSessions
        .filter(_.id === PK[TrainingSession](data.id.get))
        .map { d => (d.title, d.startDate, d.status) }
        .update((data.title, data.startDate, TrainingSessionState.withName(data.status)))
    ).map(_ => PK[TrainingSession](data.id.get))

  def delete(id: Long): Future[Unit] = {
    db.run(
      (
        trainingSessionFees.filter(_.trainingSessionId === PK[TrainingSession](id)).delete >>
        trainingSessions.filter(_.id === PK[TrainingSession](id)).delete
      ).transactionally
    ).map(_ => ())
  }

  def getTrainingSessionEnv(tsId: Long): Future[TrainingSessionEnvironment] = {
    for {
      sessionTitle <- db.run(trainingSessions.filter(_.id === PK[TrainingSession](tsId)).map(_.title).result.head)
      trainingTitle <-
        db.run(
          trainingSessions.filter(_.id === PK[TrainingSession](tsId))
            .join(trainingPlans).on(_.trainingPlanId === _.id)
            .join(trainings).on(_._2.trainingId === _.id)
            .map(_._2.title)
            .result
            .head
        )
      emails <- db.run(
        enrollments.filter(_.trainingSessionId === PK[TrainingSession](tsId))
          .join(accounts).on(_.accountId === _.id)
          .map(_._2.email)
          .result
      )
    } yield TrainingSessionEnvironment(trainingTitle, sessionTitle, emails)
  }
}
