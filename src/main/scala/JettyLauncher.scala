package dubhacks.billSplitterServer

import java.net.InetSocketAddress

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet

import org.scalatra.servlet.ScalatraListener
import org.eclipse.jetty.webapp.WebAppContext
import dubhacks.billSplitterServer.database.DatabaseInitializer

object JettyLauncher {
  def main(args: Array[String]) = {
    if (args.length == 1 && args(0) == "init") {
      // create tables
      DatabaseInitializer.createTables()
    } else if (args.length == 1 && args(0) == "drop") {
      // drop tables
      DatabaseInitializer.dropTables()
    } else {
      val socketAddress = new InetSocketAddress(2048)
      val server = new Server(socketAddress)
      val context = new WebAppContext()
      context.setContextPath("/")
      context.setResourceBase("src/main/webapp")
      context.addEventListener(new ScalatraListener)
      context.addServlet(classOf[DefaultServlet], "/")
      server.setHandler(context)
      server.start()
      server.join()
    }
  }
}
