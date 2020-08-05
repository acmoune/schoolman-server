package repositories

import models._
import controllers.formfactories.DepartmentFormData
import javax.inject._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import utils.PK
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class DepartmentRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
  with Tables {

  import profile.api._

  type ProgramsCount = Int
  type PlansCount = Int
  type TrainingsCount = Int

  case class DepartmentGraph(value: Department, programs: Seq[(Program, PlansCount, TrainingsCount)])

  def all: Future[Seq[(Department, ProgramsCount)]]  = {
    val query = for {
      department <- departments.sortBy(_.position)
    } yield (department, programs.filter(_.departmentId === department.id).sortBy(_.position).size)

    db.run(query.result)
  }

  def findById(id: Long): Future[DepartmentGraph] = {
    val depAction = departments.filter(_.id === PK[Department](id)).result.head
    val progsAction = programs.filter(_.departmentId === PK[Department](id)).sortBy(_.position)
      .map(p => (
        p,
        plans.filter(_.programId === p.id).size,
        trainings.filter(_.programId === p.id).sortBy(_.position).size
      ))
      .result

    db.run(depAction zip progsAction).map(res => DepartmentGraph(res._1, res._2))
  }

  def add(data: DepartmentFormData): Future[PK[Department]] =
    db.run(departments.returning(departments.map(_.id)) += Department(title = data.title, position = Some(data.position)))

  def save(data: DepartmentFormData): Future[PK[Department]] =
    db.run(
      departments
        .filter(_.id === PK[Department](data.id.get))
        .map(d => (d.title, d.position))
        .update(data.title, Some(data.position))
    ).map { _ => PK[Department](data.id.get) }

  def delete(id: Long): Future[Unit] =
    db.run(departments.filter(_.id === PK[Department](id)).delete).map(_ => ())
}
