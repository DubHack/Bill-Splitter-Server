import dubhacks.billSplitterServer.BillSplitterServerServlet
import org.scalatra._
import javax.servlet.ServletContext

import com.mchange.v2.c3p0.ComboPooledDataSource
import dubhacks.billSplitterServer.database.access.DAO
import scala.slick.driver.PostgresDriver
import scala.slick.jdbc.JdbcBackend.Database

class ScalatraBootstrap extends LifeCycle {

  val cpds = new ComboPooledDataSource
  override def init(context: ServletContext) {
    val db = Database.forDataSource(cpds)
    val dao = new DAO(PostgresDriver) { override val production = true }
    //val eao = EAO(new CSVRequester(), UidProvider)

    context.mount(new BillSplitterServerServlet(db, dao), "/*")
  }
  private def closeDbConnection() {
    cpds.close()
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection()
  }
}
