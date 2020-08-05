package repositories

import controllers.formfactories.AccountProfileFormData
import javax.inject._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import models._
import utils.PK

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountProfileRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with Tables {

  import profile.api._

  def createFor(accountId: Long, data: AccountProfileFormData): Future[AccountProfile] =
    db.run(accountProfiles.returning(accountProfiles.map(_.id)) += AccountProfile(
      accountId = PK[Account](data.accountId),

      birthDate = data.birthDate,
      birthPlace = data.birthPlace,
      residence = data.residence,
      phoneNumber = data.phoneNumber,
      nationality = data.nationality,

      otherDetails = data.otherDetails
    )).flatMap(id => findById(id.value))

  def save(data: AccountProfileFormData): Future[AccountProfile] =
    db.run(
      accountProfiles
        .filter(_.id === PK[AccountProfile](data.id.get))
        .map { d => (
          d.birthDate,
          d.birthPlace,
          d.residence,
          d.phoneNumber,
          d.nationality,
          d.otherDetails
        )}
        .update((
          data.birthDate,
          data.birthPlace,
          data.residence,
          data.phoneNumber,
          data.nationality,
          data.otherDetails
        ))
    ).flatMap(_ => findById(data.id.get))

  def findById(id: Long): Future[AccountProfile] =
    db.run(accountProfiles.filter(_.id === PK[AccountProfile](id)).result.head)
}
