package dubhacks.billSplitterServer

import org.scalatra.auth.strategy.{BasicAuthStrategy, BasicAuthSupport}
import org.scalatra.auth.{ScentrySupport, ScentryConfig}
import org.scalatra.ScalatraBase
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}


class BillSplitterAuthStrategy(protected override val app: ScalatraBase, realm: String, verifier:UserVerifier)
  extends BasicAuthStrategy[User](app, realm) {

  protected def validate(userName: String, password: String)(implicit request: HttpServletRequest,
                                                             response: HttpServletResponse): Option[User] = {
    verifier.verify(userName, password)
  }

  protected def getUserId(user: User)(implicit request: HttpServletRequest, response: HttpServletResponse): String = {
    user.id
  }
}

trait UserVerifier {
  /**
   * Verifies a user
   *
   * @param userName name of user
   * @param password password of user
   * @return Option[User]
   *         if the name and password are valid, then returns Some(User(userName))
   *         else returns None
   */
  def verify(userName:String, password:String): Option[User]
}

object UserVerifierStub extends UserVerifier {
  def verify(userName:String, password:String): Option[User] = {
    val test = "test"
    if (userName == test && password == test) Some(User(test))
    else None
  }
}

object CSVUserVerifier extends UserVerifier {
  import com.github.tototoshi.csv.CSVReader

  private val reader = CSVReader.open("data/users.csv")

  // user -> password
  private val users: Map[String, String] = reader.allWithHeaders().map(row => (row("user"), row("password"))).toMap

  reader.close()

  def verify(userName:String, password:String): Option[User] = {
    users.get(userName) match {
      case None =>
        None
      case Some(userPassword) =>
        if (password == userPassword) Some(User(userName))
        else None
    }
  }
}

trait AuthenticationSupport extends ScentrySupport[User] with BasicAuthSupport[User] {
  self: ScalatraBase =>

  val realm = "Bill-Splitter-Server"

  protected def fromSession = { case id: String => User(id)  }
  protected def toSession   = { case usr: User => usr.id }

  protected val scentryConfig = new ScentryConfig {}.asInstanceOf[ScentryConfiguration]


  override protected def configureScentry() = {
    scentry.unauthenticated {
      scentry.strategies("Basic").unauthenticated()
    }
  }

  override protected def registerAuthStrategies() = {
    scentry.register("Basic", app => new BillSplitterAuthStrategy(app, realm, CSVUserVerifier))
  }

}

case class User(id: String)
