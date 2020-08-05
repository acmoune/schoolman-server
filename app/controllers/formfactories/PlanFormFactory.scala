package controllers.formfactories

import models.Plan
import play.api.data._
import play.api.data.Forms._

case class PlanFormData
(
  id: Option[Long],
  programId: Long,
  title: String,
  description: String
) extends FormData

case class TrainingPlanListFormData(planId: Long, trainingIds: Seq[Long]) extends FormData
case class TrainingPlanDetailsFormData(id: Long, duration: String) extends FormData

object PlanFormFactory extends FormFactory[PlanFormData, Plan] {
  def emptyFormData: PlanFormData = PlanFormData(None, 0L, "", "")
  def formDataFrom(p: Plan): PlanFormData = PlanFormData(id=Some(p.id.value), programId=p.id.value, title=p.title, description=p.description)

  def form: Form[PlanFormData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "programId" -> longNumber,
      "title" -> nonEmptyText(minLength = 4),
      "description" -> nonEmptyText,
    )(PlanFormData.apply)(PlanFormData.unapply)
  )
}

object TrainingPlanForms {
  val tpForm: Form[TrainingPlanListFormData] = Form(
    mapping(
      "planId" -> longNumber,
      "trainingIds" -> seq(longNumber)
    )(TrainingPlanListFormData.apply)(TrainingPlanListFormData.unapply)
  )

  val tpDetailsForm: Form[TrainingPlanDetailsFormData] = Form(
    mapping(
      "id" -> longNumber,
      "duration" -> nonEmptyText
    )(TrainingPlanDetailsFormData.apply)(TrainingPlanDetailsFormData.unapply)
  )
}

