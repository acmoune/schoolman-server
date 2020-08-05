package controllers.formfactories

import models.Model
import play.api.data.Form


trait FormData

trait FormFactory[D <: FormData, M <: Model] {
  def emptyFormData: D
  def formDataFrom(model: M): D
  def form: Form[D]
}

object FormFactory {
  implicit val departmentFDF = DepartmentFormFactory
  implicit val programFDF = ProgramFormFactory
  implicit val planFDF = PlanFormFactory
  implicit val trainingFDF = TrainingFormFactory
  implicit val trainingUnitFDF = TrainingUnitFormFactory
  implicit val trainingFeeFDF = TrainingFeeFormFactory
  implicit val trainingSessionFDF = TrainingSessionFormFactory
  implicit val accountFDF = AccountFormFactory
  implicit val accountProfileFDF = AccountProfileFormFactory
  implicit val enrollmentFDF = EnrollmentFormFactory
  implicit val biilFDF = BillFormFactory
  implicit val paymentFDF = PaymentFormFactory


  def emptyFormData[D <: FormData, M <: Model](implicit fdFactory: FormFactory[D, M]): D =
    fdFactory.emptyFormData

  def formDataFrom[D <: FormData, M <: Model](model: M)(implicit fdFactory: FormFactory[D, M]): D =
    fdFactory.formDataFrom(model)

  def form[D <: FormData, M <: Model](implicit fdFactory: FormFactory[D, M]): Form[D] =
    fdFactory.form
}
