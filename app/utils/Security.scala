package utils

import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

object Security {
  def generateSecret:String =
    java.util.Base64.getEncoder.encodeToString(s"${UUID.randomUUID.toString}.${UUID.randomUUID.toString}.${UUID.randomUUID.toString}.${UUID.randomUUID.toString}".getBytes)

  def hashPassword(pwd: String): String =
    BCrypt.hashpw(pwd, BCrypt.gensalt(12))

  def checkPassword(pwd: String, hashedPwd: String): Boolean =
    BCrypt.checkpw(pwd, hashedPwd)
}
