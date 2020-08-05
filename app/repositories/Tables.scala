package repositories

import java.time.LocalDate
import models._
import slick.jdbc.JdbcProfile
import utils.PK

trait Tables {
  protected val profile: JdbcProfile

  import profile.api._

  /*  DEPARTMENT table  */
  protected class DepartmentTable(tag: Tag) extends Table[Department](tag, "DEPARTMENT") {
    def id = column[PK[Department]]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title", O.Unique)
    def position = column[Option[Int]]("position")

    def * = (id, title, position).mapTo[Department]
    def ind1 = index("dep_tit_idx", title, unique = true)
  }
  protected lazy val departments = TableQuery[DepartmentTable]


  /*  PROGRAM table  */
  protected class ProgramTable(tag: Tag) extends Table[Program](tag, "PROGRAM") {
    def id = column[PK[Program]]("id", O.PrimaryKey, O.AutoInc)
    def departmentId = column[PK[Department]]("department")
    def title = column[String]("title", O.Unique)
    def description = column[String]("description")
    def logo = column[Option[String]]("logo")
    def link = column[Option[String]]("link")
    def position = column[Option[Int]]("position")

    def * = (id, departmentId, title, description, logo, link, position).mapTo[Program]
    def plan = foreignKey("pro_dep_fk", departmentId, departments)(_.id)
    def ind1 = index("pro_tit_idx", title, unique = true)
    def ind2 = index("pro_dep_idx", departmentId, unique = false)
  }
  protected lazy val programs = TableQuery[ProgramTable]


  /*  PLAN table  */
  protected class PlanTable(tag: Tag) extends Table[Plan](tag, "PLAN") {
    def id = column[PK[Plan]]("id", O.PrimaryKey, O.AutoInc)
    def programId = column[PK[Program]]("program")
    def title = column[String]("title", O.Unique)
    def description = column[String]("description")

    def * = (id, programId, title, description).mapTo[Plan]
    def program = foreignKey("pla_pro_fk", programId, programs)(_.id)
    def ind1 = index("pla_tit_idx", title, unique = true)
    def ind2 = index("pla_pro_idx", programId, unique = false)
  }
  protected lazy val plans = TableQuery[PlanTable]


  /*  TRAINING table  */
  protected class TrainingTable(tag: Tag) extends Table[Training](tag, "TRAINING") {
    def id = column[PK[Training]]("id", O.PrimaryKey, O.AutoInc)
    def programId = column[PK[Program]]("program")
    def title = column[String]("title", O.Unique)
    def description = column[String]("description")
    def requiredOptionalUnits = column[Int]("required_optional_units")
    def prerequisites = column[Option[String]]("prerequisites")
    def qualifications = column[Option[String]]("qualifications")
    def link = column[Option[String]]("link")
    def banner = column[Option[String]]("banner")
    def position = column[Option[Int]]("position")

    def * = (id, programId, title, description, requiredOptionalUnits, prerequisites, qualifications, link, banner, position).mapTo[Training]
    def program = foreignKey("tra_pro_fk", programId, programs)(_.id)
    def ind1 = index("tra_tit_idx", title, unique = true)
    def ind2 = index("tra_pro_idx", programId, unique = false)
  }
  protected lazy val trainings = TableQuery[TrainingTable]


  /*  TRAINING_PLAN table  */
  protected class TrainingPlanTable(tag: Tag) extends Table[TrainingPlan](tag, "TRAINING_PLAN") {
    def id = column[PK[TrainingPlan]]("id", O.PrimaryKey, O.AutoInc)
    def trainingId = column[PK[Training]]("training")
    def planId = column[PK[Plan]]("plan")
    def duration = column[String]("duration")

    def * = (id, trainingId, planId, duration).mapTo[TrainingPlan]
    def training = foreignKey("tp_tra_fk", trainingId, trainings)(_.id)
    def plan = foreignKey("tp_pla_fk", planId, plans)(_.id)
    def ind1 = index("tp_tra_pla_idx", (trainingId, planId), unique = true)
    def ind2 = index("tp_pla_idx", planId, unique = false)
  }
  protected lazy val trainingPlans = TableQuery[TrainingPlanTable]


  /*  TRAINING_UNIT table  */
  protected class TrainingUnitTable(tag: Tag) extends Table[TrainingUnit](tag, "TRAINING_UNIT") {
    def id = column[PK[TrainingUnit]]("id", O.PrimaryKey, O.AutoInc)
    def trainingId = column[PK[Training]]("training")
    def title = column[String]("title", O.Unique)
    def syllabus = column[String]("syllabus")
    def optional = column[Boolean]("optional")
    def position = column[Option[Int]]("position")

    def * = (id, trainingId, title, syllabus, optional, position).mapTo[TrainingUnit]
    def fk1 = foreignKey("uni_tra_fk", trainingId, trainings)(_.id)
    def ind1 = index("uni_tra_idx", trainingId, unique = false)
    def ind2 = index("uni_tit_idx", title, unique = true)
  }
  protected lazy val trainingUnits = TableQuery[TrainingUnitTable]


  /*  TRAINING_SESSION table  */
  import TrainingSessionState.TrainingSessionState

  implicit val sessionStateMapper = MappedColumnType.base[TrainingSessionState, String](_.toString, TrainingSessionState.withName)

  protected class TrainingSessionTable(tag: Tag) extends Table[TrainingSession](tag, "TRAINING_SESSION") {
    def id = column[PK[TrainingSession]]("id", O.PrimaryKey, O.AutoInc)
    def trainingPlanId = column[PK[TrainingPlan]]("training_plan")
    def title = column[String]("title", O.Unique)
    def startDate = column[Option[LocalDate]]("startDate")
    def status = column[TrainingSessionState]("status")

    def * = (id, trainingPlanId, title, startDate, status).mapTo[TrainingSession]
    def fk1 = foreignKey("ses_tp_fk", trainingPlanId, trainingPlans)(_.id)
    def ind1 = index("ses_tit_idx", title, unique = true)
    def ind2 = index("ses_tp_idx", trainingPlanId, unique = false)
  }
  protected lazy val trainingSessions = TableQuery[TrainingSessionTable]


  /*  ACCOUNT table  */
  import AccountRole.AccountRole

  implicit val accountRoleMapper = MappedColumnType.base[AccountRole, String](_.toString, AccountRole.withName)

  protected class AccountTable(tag: Tag) extends Table[Account](tag, "ACCOUNT") {
    def id = column[PK[Account]]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email", O.Unique)
    def password = column[String]("password")
    def role = column[AccountRole]("role")
    def locked = column[Boolean]("locked")
    def fullName = column[String]("full_name")

    def * = (id, email, password, role, locked, fullName).mapTo[Account]
    def ind1 = index("acc_ema_idx", email, unique = true)
  }
  protected lazy val accounts = TableQuery[AccountTable]


  /*  TRAINING_FEE and TRAINING_SESSION_FEE table  */
  import FeeType.FeeType

  implicit val feeTypeMapper = MappedColumnType.base[FeeType, String](_.toString, FeeType.withName)

  protected class TrainingFeeTable(tag: Tag) extends Table[TrainingFee](tag, "TRAINING_FEE") {
    def id = column[PK[TrainingFee]]("id", O.PrimaryKey, O.AutoInc)
    def trainingPlanId = column[PK[TrainingPlan]]("training_plan")
    def description = column[String]("description")
    def feeType = column[FeeType]("fee_type")
    def amount = column[Double]("amount")
    def promotionalAmount = column[Option[Double]]("promotional_amount")
    def optional = column[Boolean]("optional")
    def position = column[Option[Int]]("position")

    def * = (id, trainingPlanId, description, feeType, amount, promotionalAmount, optional, position).mapTo[TrainingFee]
    def trainingPlan = foreignKey("tf_tp_fk", trainingPlanId, trainingPlans)(_.id)
    def ind1 = index("tf_tp_idx", trainingPlanId, unique = false)
  }
  protected lazy val trainingFees = TableQuery[TrainingFeeTable]

  protected class TrainingSessionFeeTable(tag: Tag) extends Table[TrainingSessionFee](tag, "TRAINING_SESSION_FEE") {
    def id = column[PK[TrainingSessionFee]]("id", O.PrimaryKey, O.AutoInc)
    def trainingSessionId = column[PK[TrainingSession]]("training_session")
    def description = column[String]("description")
    def feeType = column[FeeType]("fee_type")
    def amount = column[Double]("amount")
    def promotionalAmount = column[Option[Double]]("promotional_amount")
    def optional = column[Boolean]("optional")
    def position = column[Option[Int]]("position")

    def * = (id, trainingSessionId, description, feeType, amount, promotionalAmount, optional, position).mapTo[TrainingSessionFee]
    def fk1 = foreignKey("sf_ses_fk", trainingSessionId, trainingSessions)(_.id)
    def ind1 = index("sf_ses_idx", trainingSessionId, unique = false)
  }
  protected lazy val trainingSessionFees = TableQuery[TrainingSessionFeeTable]


  /*  ENROLLMENT table  */
  protected class EnrollmentTable(tag: Tag) extends Table[Enrollment](tag, "ENROLLMENT") {
    def id = column[PK[Enrollment]]("id", O.PrimaryKey, O.AutoInc)
    def accountId = column[PK[Account]]("account")
    def trainingSessionId = column[PK[TrainingSession]]("training_session")
    def notes = column[Option[String]]("notes")

    def * = (id, accountId, trainingSessionId, notes).mapTo[Enrollment]
    def fk1 = foreignKey("enr_acc_fk", accountId, accounts)(_.id)
    def fk2 = foreignKey("enr_ses_fk", trainingSessionId, trainingSessions)(_.id)
    def ind1 = index("enr_acc_idx", accountId, unique = false)
    def ind2 = index("enr_ses_idx", trainingSessionId, unique = false)
    def ind3 = index("enr_acc_ses_idx", (trainingSessionId, accountId), unique = true)
  }
  protected lazy val enrollments = TableQuery[EnrollmentTable]


  /*  BILL table  */
  protected class BillTable(tag: Tag) extends Table[Bill](tag, "BILL") {
    def id = column[PK[Bill]]("id", O.PrimaryKey, O.AutoInc)
    def trainingSessionFeeId = column[PK[TrainingSessionFee]]("training_session_fee")
    def enrollmentId = column[PK[Enrollment]]("enrollment_id")
    def amount = column[Double]("amount")

    def * = (id, trainingSessionFeeId, enrollmentId, amount).mapTo[Bill]
    def fk1 = foreignKey("bil_sf_fk", trainingSessionFeeId, trainingSessionFees)(_.id)
    def fk2 = foreignKey("bil_enr_fk", enrollmentId, enrollments)(_.id)
    def ind1 = index("bil_enr_idx", enrollmentId, unique = false)
    def ind2 = index("bil_enr_ses_idx", (enrollmentId, trainingSessionFeeId), unique = true)
  }
  protected lazy val bills = TableQuery[BillTable]


  /*  PAYMENT table  */
  protected class PaymentTable(tag: Tag) extends Table[Payment](tag, "PAYMENT") {
    def id = column[PK[Payment]]("id", O.PrimaryKey, O.AutoInc)
    def billId = column[PK[Bill]]("bill")
    def amount = column[Double]("amount")
    def date = column[LocalDate]("date")

    def * = (id, billId, amount, date).mapTo[Payment]
    def fk1 = foreignKey("pay_bil_fk", billId, bills)(_.id)
    def ind1 = index("pay_bil_idx", billId, unique = false)
  }
  protected lazy val payments = TableQuery[PaymentTable]


  /*  PROFILE table  */
  protected class AccountProfileTable(tag: Tag) extends Table[AccountProfile](tag, "ACCOUNT_PROFILE") {
    def id = column[PK[AccountProfile]]("id", O.PrimaryKey, O.AutoInc)
    def accountId = column[PK[Account]]("account", O.Unique)

    def birthDate = column[LocalDate]("birth_date")
    def birthPlace = column[String]("birth_place")
    def residence = column[String]("residence")
    def phoneNumber = column[String]("phone_number")
    def nationality = column[String]("nationality")

    def otherDetails = column[Option[String]]("other_details")

    def * = (
      id,
      accountId,
      birthDate,
      birthPlace,
      residence,
      phoneNumber,
      nationality,
      otherDetails
      ).mapTo[AccountProfile]

    def account = foreignKey("prof_acc_fk", accountId, accounts)(_.id)
    def ind1 = index("prof_acc_idx", accountId, unique = true)
  }
  protected lazy val accountProfiles = TableQuery[AccountProfileTable]


  /*  EVOLUTIONS  */
  protected case class Evolution(upScript: Seq[String], downScript: Seq[String])

  protected def evolution1(): Evolution = {
    val dbSchema =
      departments.schema ++
      programs.schema ++
      plans.schema ++
      trainings.schema ++
      trainingPlans.schema ++
      trainingUnits.schema ++
      trainingSessions.schema ++
      accounts.schema ++
      trainingFees.schema ++
      trainingSessionFees.schema ++
      enrollments.schema ++
      bills.schema ++
      payments.schema ++
      accountProfiles.schema

    Evolution(
      upScript = dbSchema.createStatements.toSeq,
      downScript = dbSchema.dropStatements.toSeq
    )
  }
}

private object EvolutionDDL extends Tables {
  lazy val profile = slick.jdbc.PostgresProfile

  def run(): Unit = {
    val e = evolution1()

    println("-- !Ups\n"); for (statement <- e.upScript) println(s"$statement;")
    println("\n-- !Downs\n"); for (statement <- e.downScript) println(s"$statement;")
  }
}
