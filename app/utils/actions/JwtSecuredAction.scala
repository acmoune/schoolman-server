package utils.actions

import javax.inject.Inject
import models.AccountDetails
import play.api.mvc._
import repositories.AccountRepository
import utils.JWTUtility

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class JwtSecuredAction @Inject()(
                                  val parser: BodyParsers.Default,
                                  accountRepository: AccountRepository
                                )(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[SecuredRequest, AnyContent]
    with ActionRefiner[Request, SecuredRequest] {

  override def refine[A](request: Request[A]): Future[Either[Result, SecuredRequest[A]]] = {
    val token = request.headers.get("schoolman_token").getOrElse("")

    JWTUtility.decode(token) match {
      case Success(payload) =>
        accountRepository.findByEmail(payload.email).map {
          case Some(account) =>
            if ((account.role.toString != payload.role) || (account.fullName != payload.fullName)) {
              Left(Results.Unauthorized("Access denied: obsolete token, please sign in again"))
            } else {
              Right(new SecuredRequest(AccountDetails(account.email, account.role.toString, account.fullName), request))
            }

          case _ =>
            Left(Results.Unauthorized("Access denied: Missing or invalid token"))
        }

      case Failure(ex) =>
        Future.successful(Left(Results.Unauthorized("Access denied: Missing or invalid token")))
    }
  }
}
