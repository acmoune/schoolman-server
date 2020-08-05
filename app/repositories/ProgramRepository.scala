package repositories

import models._
import controllers.formfactories.ProgramFormData
import javax.inject._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import utils.PK
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProgramRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
  with Tables {

  import profile.api._

  type TrainingPlansCount = Int
  type TrainingUnitsCount = Int

  case class ProgramGraph
  (
    value: Program,
    department: Department,
    plans: Seq[(Plan, TrainingPlansCount)],
    trainings: Seq[(Training, TrainingUnitsCount)]
  )
  case class ProgramContext(department: Department)

  def findById(departmentId: Long, id: Long): Future[ProgramGraph] = {
    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](id)).result.head
    val plansAction = plans.filter(_.programId === PK[Program](id))
      .map(p => (p, trainingPlans.filter(_.planId === p.id).size))
      .result
    val trainingsAction = trainings.filter(_.programId === PK[Program](id)).sortBy(_.position)
      .map(t => (t, trainingUnits.filter(_.trainingId === t.id).size))
      .result

    db.run(departmentAction zip programAction zip plansAction zip trainingsAction)
      .map { case (((department, program), pls), tps) => ProgramGraph(program, department, pls, tps) }
  }

  def context(departmentId: Long): Future[ProgramContext] = {
    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    db.run(departmentAction).map { ProgramContext }
  }

  def addToDepartment(departmentId: Long, data: ProgramFormData): Future[PK[Program]] = {
    val newProgram = Program(
      departmentId = PK[Department](data.departmentId),
      title = data.title,
      description =  data.description,
      logo = data.logo,
      link = data.link,
      position = Some(data.position)
    )

    db.run(programs.returning(programs.map(_.id)) += newProgram)
  }

  def save(data: ProgramFormData): Future[PK[Program]] =
    db.run(
      programs
        .filter(_.id === PK[Program](data.id.get))
        .map { d => (d.title, d.description, d.logo, d.link, d.position) }
        .update((data.title, data.description, data.logo, data.link, Some(data.position)))
    ).map { _ => PK[Program](data.id.get) }

  def delete(id: Long): Future[Unit] = db.run(programs.filter(_.id === PK[Program](id)).delete).map { _ => () }
}
