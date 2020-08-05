package utils.actions

import javax.inject.Inject
import models.AccountDetails
import play.api.mvc._
import repositories.AccountRepository
import scala.concurrent.{ExecutionContext, Future}


class SecuredAction @Inject()(
                               val parser: BodyParsers.Default,
                               accountRepository: AccountRepository
                             )(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[SecuredRequest, AnyContent]
  with ActionRefiner[Request, SecuredRequest] {

  override def refine[A](request: Request[A]): Future[Either[Result, SecuredRequest[A]]] = {
    val email = request.session.get("email")

    if (email.isDefined) {
      accountRepository.findByEmail(email.get).map {
        case Some(account) =>
          if ((account.role.toString != request.session.get("role").getOrElse("")) || (account.fullName != request.session.get("fullName").getOrElse(""))) {
            Left(
              Results.Redirect(controllers.routes.AuthController.signInUser())
                .withNewSession
                .flashing("danger" -> "Your account has been updated since your last login. Please sign in again.")
            )
          } else {
            Right(new SecuredRequest(AccountDetails(account.email, account.role.toString, account.fullName), request))
          }

        case _ =>
          Left(
            Results.Redirect(controllers.routes.AuthController.signInUser())
              .withNewSession
              .flashing("danger" -> "Please sign in again.")
          )
      }
    } else
      Future.successful(Left(Results.Redirect(controllers.routes.AuthController.signInUser())))
  }
}
