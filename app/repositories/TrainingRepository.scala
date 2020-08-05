package repositories

import controllers.formfactories.TrainingFormData
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import models._
import utils.PK

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TrainingRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
  with Tables  {

  import profile.api._

  case class TrainingGraph(value: Training, department: Department, program: Program, trainingUnits: Seq[TrainingUnit])
  case class TrainingContext(department: Department, program: Program)

  def findById(departmentId: Long, programId: Long, id: Long): Future[TrainingGraph] = {
    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](id)).result.head
    val trainingUnitsAction = trainingUnits.filter(_.trainingId === PK[Training](id)).sortBy(_.position).result

    db.run(departmentAction zip programAction zip trainingAction zip trainingUnitsAction)
      .map { case (((department, program), training), tus) => TrainingGraph(training, department, program, tus) }
  }

  def context(departmentId: Long, programId: Long): Future[TrainingContext] = {
    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head

    db.run(departmentAction zip programAction)
      .map { case (department, program) => TrainingContext(department, program) }
  }

  def addToProgram(programId: Long, data: TrainingFormData): Future[PK[Training]] = {
    val training: Training = Training(
      programId = PK[Program](programId),
      title = data.title,
      description = data.description,
      requiredOptionalUnits = data.requiredOptionalUnits,
      prerequisites = data.prerequisites,
      qualifications = data.qualifications,
      link = data.link,
      banner = data.banner,
      position = Some(data.position)
    )

    db.run(trainings.returning(trainings.map(_.id)) += training)
  }

  def save(data: TrainingFormData): Future[PK[Training]] =
    db.run(
      trainings
        .filter(_.id === PK[Training](data.id.get))
        .map { d => (d.title, d.description, d.requiredOptionalUnits, d.prerequisites, d.qualifications, d.link, d.banner, d.position) }
        .update((data.title, data.description, data.requiredOptionalUnits, data.prerequisites, data.qualifications, data.link, data.banner, Some(data.position)))
    ).map { _ => PK[Training](data.id.get)}

  def delete(id: Long): Future[Unit] =
    db.run(trainings.filter(_.id === PK[Training](id)).delete).map(_ => ())
}
