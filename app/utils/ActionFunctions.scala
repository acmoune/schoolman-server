package utils

import models.AccountRole
import play.api.mvc.{ActionFilter, Result, Results}
import utils.actions.SecuredRequest

import scala.concurrent.{ExecutionContext, Future}

object ActionFunctions {
  def IsAdmin(implicit ec: ExecutionContext) = new ActionFilter[SecuredRequest] {
    def executionContext: ExecutionContext = ec

    def filter[A](request: SecuredRequest[A]): Future[Option[Result]] = Future {
      if (request.currentUser.role == AccountRole.Student.toString)
        Some(Results.Forbidden)
      else
        None
    }
  }
}
