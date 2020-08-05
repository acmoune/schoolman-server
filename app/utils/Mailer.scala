package utils

import com.google.inject.{Inject, Singleton}
import play.api.libs.mailer._

@Singleton
class Mailer @Inject()(mailerClient: MailerClient) {

  def sendEmail(subject: String, from: String, to: Seq[String], message: String): String = {
    val email = Email(subject, from, to, bodyHtml = Some(message.stripMargin))
    mailerClient.send(email)
  }
}
