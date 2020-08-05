package controllers

import java.time.format.{DateTimeFormatter, FormatStyle}

import com.google.inject.Inject
import com.hhandoko.play.pdf.PdfGenerator
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.BillRepository
import utils.actions._

import scala.concurrent.ExecutionContext

class PrinterController @Inject()(
                                   cc: ControllerComponents,
                                   config: Configuration,
                                   pdfGen: PdfGenerator,
                                   billRepository: BillRepository,
                                   SecuredAction: SecuredAction,
                                   JwtSecuredAction: JwtSecuredAction
                                 )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  val BASE_URL: String = config.get[String]("schoolman.base-url")

  def printBill(billId: Long) = SecuredAction.async {
    billRepository.getBill(billId)
      .map { bill => pdfGen.ok(views.html.pages.bills.print(bill, java.time.LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))), BASE_URL) }
  }

  def clientPrintBill(billId: Long) = JwtSecuredAction.async {
    billRepository.getBill(billId)
      .map { bill => pdfGen.ok(views.html.pages.bills.print(bill, java.time.LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))), BASE_URL) }
  }
}
