package api.repositories

import javax.inject._
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repositories.Tables
import slick.jdbc.JdbcProfile
import utils.PK
import api.dto._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountsRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with Tables {

  import profile.api._

  def getAccountByEmail(email: String): Future[AccountView] = {
    db.run(accounts.filter(_.email === email).result.head)
      .map { a => AccountView(a.id, a.email, a.fullName) }
  }

  def getAccountProfile(email: String): Future[Option[AccountProfileView]] = {
    db.run(
      accounts.filter(_.email === email)
        .joinLeft(accountProfiles)
        .on(_.id === _.accountId)
        .map(_._2)
        .result
        .head
    ).map { _.map(p =>
      AccountProfileView(
        id = p.id,
        accountId = p.accountId,
        birthDate = p.birthDate,
        birthPlace = p.birthPlace,
        residence = p.residence,
        phoneNumber = p.phoneNumber,
        nationality = p.nationality,
        otherDetails = p.otherDetails
      ))
    }
  }

  def getAccountForProfile(profileId: Long): Future[AccountView] = {
    db.run(
      accountProfiles.filter(_.id === PK[AccountProfile](profileId))
        .join(accounts)
        .on(_.accountId === _.id)
        .map(_._2)
        .result
        .head
     ).map { a => AccountView(a.id, a.email, a.fullName) }
  }

  private def getTrainingSessionContext(trainingSessionId: PK[TrainingSession]) = {
    db.run(
      trainingSessions.filter(_.id === trainingSessionId)
        .join(trainingPlans).on(_.trainingPlanId === _.id)
        .join(trainings).on(_._2.trainingId === _.id)
        .join(plans).on(_._1._2.planId === _.id)
        .join(programs).on(_._2.programId ===_.id )
        .join(departments).on(_._2.departmentId === _.id)
        .map { case (((((ts, tp), training), plan), prog), dep) =>
          (ts.title, training.title, plan.title, prog.title, dep.title, tp.duration, ts.startDate, ts.status)
        }
        .result
        .head
    )
  }

  private def getBills(enrollmentId: PK[Enrollment]) = {
    db.run(
      bills.filter(_.enrollmentId === enrollmentId).sortBy(_.id.desc)
        .join(trainingSessionFees).on(_.trainingSessionFeeId === _.id)
        .sortBy(_._2.position)
        .joinLeft(payments).on(_._1.id === _.billId)
        .map { case ((bill, tsFee), optionalPayment) =>
          ((bill.id, tsFee.feeType, tsFee.description, bill.amount), optionalPayment)
        }
        .result
        .map(_.groupBy(_._1).toSeq)
        .map{ items => items.map { case (bil, rows) => BillView(
          billId = bil._1.value,
          feeType = bil._2,
          description = bil._3,
          amount = bil._4,
          payments = rows.map(_._2).filter(_.isDefined).map(_.get).map(p => PaymentView(p.id.value, p.amount, p.date))
        ) } }
    )
  }

  def getEnrollments(accountId: Long): Future[Seq[EnrollmentView]] = {
    db.run(enrollments.filter(_.accountId === PK[Account](accountId)).sortBy(_.id.desc).result)
      .flatMap { items => Future.sequence(items.map { enr =>
        for {
          (sessionTitle, trainingTitle, planTitle, programTitle, departmentTitle, duration, startDate, sessionStatus) <- getTrainingSessionContext(enr.trainingSessionId)
          bills <- getBills(enr.id)
        } yield EnrollmentView(
          enrollmentId = enr.id.value,
          accountId = accountId,
          sessionTitle = sessionTitle,
          trainingTitle = trainingTitle,
          planTitle = planTitle,
          programTitle = programTitle,
          departmentTitle = departmentTitle,
          startDate = startDate,
          duration = duration,
          sessionStatus = sessionStatus,
          bills = bills
        )
      })}
  }
}
