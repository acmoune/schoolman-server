package repositories

import controllers.formfactories.TrainingUnitFormData
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import models._
import utils.PK

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class TrainingUnitRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with Tables  {

  import profile.api._

  case class TrainingUnitGraph(value: TrainingUnit, department: Department, program: Program, training: Training)
  case class TrainingUnitContext(department: Department, program: Program, training: Training)

  def findById(departmentId: Long, programId: Long, trainingId: Long, id: Long): Future[TrainingUnitGraph] = {
    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head
    val trainingUnitAction = trainingUnits.filter(_.id === PK[TrainingUnit](id)).result.head

    db.run(departmentAction zip programAction zip trainingAction zip trainingUnitAction)
      .map { case (((department, program), training), trainingUnit) => TrainingUnitGraph(trainingUnit, department, program, training) }
  }

  def context(departmentId: Long, programId: Long, trainingId: Long): Future[TrainingUnitContext] = {
    val departmentAction = departments.filter(_.id === PK[Department](departmentId)).result.head
    val programAction = programs.filter(_.id === PK[Program](programId)).result.head
    val trainingAction = trainings.filter(_.id === PK[Training](trainingId)).result.head

    db.run(departmentAction zip programAction zip trainingAction)
      .map { case ((department, program), training) => TrainingUnitContext(department, program, training) }
  }

  def addToTraining(trainingId: Long, data: TrainingUnitFormData): Future[PK[TrainingUnit]] =
    db.run(trainingUnits.returning(trainingUnits.map(_.id)) += TrainingUnit(
      trainingId = PK[Training](trainingId),
      title = data.title,
      syllabus = data.syllabus,
      optional = data.optional,
      position = Some(data.position)
    ))

  def save(data: TrainingUnitFormData): Future[PK[TrainingUnit]] =
    db.run(
      trainingUnits
        .filter(_.id === PK[TrainingUnit](data.id.get))
        .map { d => (d.title, d.syllabus, d.optional, d.position) }
        .update((data.title, data.syllabus, data.optional, Some(data.position)))
    ).map(_ => PK[TrainingUnit](data.id.get))

  def delete(id: Long): Future[Unit] =
    db.run(trainingUnits.filter(_.id === PK[TrainingUnit](id)).delete).map(_ => ())
}
