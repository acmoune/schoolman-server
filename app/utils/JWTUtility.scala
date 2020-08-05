package utils

import com.auth0.jwt.{JWT, JWTVerifier}
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.{JWTCreationException, JWTVerificationException, JWTDecodeException}
import models.AccountDetails

import scala.util.{Failure, Success, Try}

object JWTUtility {
  type Token = String
  val secretKey = "secretkey"
  val algorithmHS: Algorithm = Algorithm.HMAC256(secretKey)
  val verifier: JWTVerifier = JWT.require(algorithmHS).withIssuer("SchoolMan Server").build()

  def create(payload: AccountDetails): Try[Token] = {
    try {
      val token = JWT.create
        .withJWTId(java.util.UUID.randomUUID.toString)
        .withIssuer("SchoolMan Server")
        .withIssuedAt(java.util.Date.from(java.time.Instant.now))
        .withExpiresAt(java.util.Date.from(java.time.Instant.now.plus(java.time.Duration.ofDays(100))))
        .withClaim("ema", payload.email)
        .withClaim("rol", payload.role)
        .withClaim("nam", payload.fullName)
        .sign(algorithmHS)

      Success(token)
    } catch {
      case ex: JWTCreationException => Failure(ex)
    }
  }

  def isValid(token: Token): Boolean = {
    try {
      verifier.verify(token)
      true
    } catch { case _: JWTVerificationException => false }
  }

  def decode(token: Token): Try[AccountDetails] = {
    if (!isValid(token)) return Failure(new Exception("Invalid token"))

    try {
      val jwt = JWT.decode(token)
      val email = jwt.getClaim("ema").asString
      val role = jwt.getClaim("rol").asString
      val fullName = jwt.getClaim("nam").asString

      Success(AccountDetails(email, role, fullName))
    } catch { case ex: JWTDecodeException => Failure(ex) }
  }
}
