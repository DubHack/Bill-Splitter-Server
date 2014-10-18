package dubhacks.billSplitterServer.database

import scala.slick.driver.PostgresDriver
import com.mchange.v2.c3p0.ComboPooledDataSource
import dubhacks.billSplitterServer.database.access.DAO
import scala.slick.jdbc.JdbcBackend.{Database, Session}

/**
 * Created by aerust on 10/18/14.
 */
object DatabaseInitializer {

  private val cpds = new ComboPooledDataSource
  private val db = Database.forDataSource(cpds)
  private val dao = new DAO(PostgresDriver)

  def createTables() {
    db withSession { implicit session: Session =>
      println("Creating tables...")
      dao.createTables
    }
  }

  /**
   * Drops tables from Hyperion database
   */
  def dropTables() {
    db withSession { implicit session: Session =>
      println("Dropping tables...")
      dao.dropTables
    }
  }
}
