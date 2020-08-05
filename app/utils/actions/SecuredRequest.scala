package utils.actions

import models.AccountDetails
import play.api.mvc.{Request, WrappedRequest}

class SecuredRequest[A](
                         val currentUser: AccountDetails,
                         val request: Request[A]
                       ) extends WrappedRequest[A](request)
