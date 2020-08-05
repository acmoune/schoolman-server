package modules

import com.google.inject.{AbstractModule, Provides}
import com.hhandoko.play.pdf.PdfGenerator
import play.api.Environment

class MainModule extends AbstractModule {

  @Provides
  def providePdfGenerator(env: Environment): PdfGenerator = {
    val pdfGen = new PdfGenerator(env)
    pdfGen.loadLocalFonts(Seq("fonts/Arial.ttf"))
    pdfGen
  }
}
