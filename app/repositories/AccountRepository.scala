package repositories

import controllers.formfactories.{AccountDetailsFormData, AccountFormData}
import javax.inject._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import models._
import utils._

@Singleton
class AccountRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with Tables {

  import profile.api._

  case class AccountGraph(value: Account, profile: Option[AccountProfile], enrollments: Seq[EnrollmentGraph])

  type EnrollmentCount = Int

  def all: Future[Seq[(Account, EnrollmentCount)]]  = {
    val query = for {
      account <- accounts
    } yield (account, enrollments.filter(_.accountId === account.id).size)

    db.run(query.result)
  }

  def findByEmail(email: String): Future[Option[Account]] =
    db.run(accounts.filter(_.email.toLowerCase === email.toLowerCase).result.headOption)

  def findById(id: Long): Future[AccountGraph] = {
    val accAction = accounts.filter(_.id === PK[Account](id)).result.head
    val profileAction = accountProfiles.filter(_.accountId === PK[Account](id)).result.headOption

    db.run(accAction zip profileAction)
      .flatMap{ case (acc, prof) => {

        db.run(enrollments.filter(_.accountId === PK[Account] (id)).sortBy(_.id.desc).result)
          .map { items => items.map { enr =>
            for {
              tsess <- db.run(trainingSessions.filter(_.id === enr.trainingSessionId).result.head)
              tp <- db.run(trainingPlans.filter(_.id === tsess.trainingPlanId).result.head)
              t <- db.run(trainings.filter(_.id === tp.trainingId).result.head)
              plan <- db.run(plans.filter(_.id === tp.planId).result.head)
              pro <- db.run(programs.filter(_.id === plan.programId).result.head)
              dep <- db.run(departments.filter(_.id === pro.departmentId).result.head)
            } yield EnrollmentGraph(enr, dep, pro, plan, t, tp, tsess, acc, Seq.empty, 0.0)
          }}
          .flatMap { items => Future.sequence(items) }
          .map { items => AccountGraph(acc, prof, items)}
      }}
  }

  def add(data: AccountFormData): Future[PK[Account]] =
    verifyEmailUniqueness(data.email)
      .flatMap { _ =>
        db.run(accounts.returning(accounts.map(_.id)) += Account(
          email = data.email,
          password = encryptPassword(data.password),
          role = AccountRole.withName(data.role),
          locked = data.locked,
          fullName = data.fullName
        ))
      }

  def updateEmail(id: Long, email: String): Future[PK[Account]] =
    verifyEmailUniqueness(email)
      .flatMap { _ =>
        db.run(accounts.filter(_.id === PK[Account](id)).map(d => d.email).update(email))
          .map(_ => PK[Account](id))
      }

  def updateDetails(id: Long, details: AccountDetailsFormData): Future[PK[Account]] = {
    db.run(accounts.filter(_.id === PK[Account](id)).result.head)
      .flatMap { account =>
        val emailChanged: Boolean = account.email.toLowerCase != details.email.toLowerCase

        if (emailChanged) {
          verifyEmailUniqueness(details.email)
            .flatMap { _ =>
              db.run(
                accounts.filter(_.id === PK[Account](id))
                  .map(d => (d.email, d.fullName, d.role))
                  .update((details.email, details.fullName, AccountRole.withName(details.role)))
                  .map(_ => PK[Account](id))
              )
            }
        } else {
          db.run(
            accounts.filter(_.id === PK[Account](id))
              .map(d => (d.fullName, d.role))
              .update((details.fullName, AccountRole.withName(details.role)))
              .map(_ => PK[Account](id))
          )
        }
      }
  }

  def setLocking(id: Long, locked: Boolean): Future[PK[Account]] =
    db.run(accounts.filter(_.id === PK[Account](id)).map(d => d.locked).update(locked))
      .map(_ => PK[Account](id))

  def adminResetPassword(id: Long, newPassword: String): Future[PK[Account]] =
    db.run(accounts.filter(_.id === PK[Account](id)).map(d => d.password).update(encryptPassword(newPassword)))
      .map(_ => PK[Account](id))

  def updatePassword(account: Account, newPassword: String): Future[PK[Account]] =
    db.run(accounts.filter(_.id === account.id).map(d => d.password).update(encryptPassword(newPassword)))
      .map(_ => account.id)

  def delete(id: Long): Future[Unit] =
    db.run(
      (
      accountProfiles.filter(_.accountId === PK[Account](id)).delete >>
      accounts.filter(_.id === PK[Account](id)).delete
      ).transactionally
    ).map(_ => ())

  def verifyEmailUniqueness(email: String): Future[Boolean] =
    db.run(accounts.filter(_.email.toLowerCase === email.toLowerCase).size.result)
      .map { s => if (s == 0) true else throw new Exception(s"Email [$email] already exists") }

  def encryptPassword(password: String): String =
    Security.hashPassword(password)

  def comparePassword(encryptedPass: String, pass: String): Boolean =
    Security.checkPassword(pass, encryptedPass)
}
