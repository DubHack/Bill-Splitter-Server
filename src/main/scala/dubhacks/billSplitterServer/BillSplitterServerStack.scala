package dubhacks.billSplitterServer

import org.scalatra._
import scalate.ScalateSupport

trait BillSplitterServerStack extends ScalatraServlet with ScalateSupport with AuthenticationSupport
