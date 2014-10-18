package dubhacks.billSplitterServer

import dubhacks.billSplitterServer.database.access.DAO
import org.json4s.ext.DateTimeSerializer

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

import scala.slick.jdbc.JdbcBackend.{Database, Session}
import dubhacks.billSplitterServer.models.Card

class BillSplitterServerServlet(db: Database, dao: DAO) extends BillSplitterServerStack with JacksonJsonSupport {

  // for automatic case class to JSON conversion
  protected implicit val jsonFormats: Formats = DefaultFormats + DateTimeSerializer

  before() {
    contentType = formats("json")
  }

  /**
   * Required parameters:
   *
   *  - name: String
   *  - phone: String
   *  - address: String
   *  - password: String
   *
   */
  post("/api/users/create/:email/?") {
    basicAuth
    val email = params("email")
    params.get("name") match {
      case None =>
        haltAndLog(400, "missing parameter name")
      case Some(name) =>
        params.get("phone") match {
          case None =>
            haltAndLog(400, "missing parameter phone")
          case Some(phone) =>
            params.get("address") match {
              case None =>
                haltAndLog(400, "missing parameter address")
              case Some(address) =>
                params.get("password") match {
                  case None =>
                  case Some(password) =>
                    createUserHelper(email, name, phone, address, password) match {
                      case Left(error) =>
                        haltAndLog(error.statusCode, error.message)
                      case Right(user: User) =>
                        status = 201
                        user
                    }
                }
            }
        }
    }
  }

  case class HTTPError(statusCode: Int, message: String)

  private def createUserHelper(email: String, name: String, phone: String, address: String, password: String): Either[HTTPError, User] = {
    try {
      db withTransaction { implicit session: Session =>
        dao.createUser(email, name, phone, address, password) match {
          case Left(error) =>
            Left(HTTPError(400, error.getMessage))
          case Right(user: User) =>
            Right(user)
        }
      }
    } catch {
      case error: Throwable =>
        Left(HTTPError(500, "something went wrong with database"))
    }
  }

  /**
   * Required parameters:
   *  - num: String
   *  - address: String
   */
  post("/api/cards/create/:email/?") {
    basicAuth
    val email = params("email")
    params.get("num") match {
      case None =>
        haltAndLog(400, "missing parameter num")
      case Some(num) =>
        params.get("address") match {
          case None =>
            haltAndLog(400, "missing parameter address")
          case Some(address) =>
            createCardHelper(email, num, address) match {
              case Left(error) =>
                haltAndLog(error.statusCode, error.message)
              case Right(card: Card) =>
                status = 201
                card
            }
        }
    }
  }

  private def createCardHelper(email: String, num: String, address: String): Either[HTTPError, Card] = {
    try {
      db withTransaction { implicit session: Session =>
        dao.createCard(email, num, address) match {
          case Left(error) =>
            Left(HTTPError(400, error.getMessage))
          case Right(card: Card) =>
            Right(card)
        }
      }
    } catch {
      case error: Throwable =>
        Left(HTTPError(500, "something went wrong with database"))
    }
  }

  /**
   * Required parameters:
   *
   *  - email: String
   *  - password: String
   *
   */
  get("api/users/auth/?") {
    basicAuth
    params.get("email") match {
      case None =>
        haltAndLog(400, "missing parameter email")
      case Some(email) =>
        params.get("password") match {
          case None =>
            haltAndLog(400, "missing parameter password")
          case Some(password) =>
            try {
              db withTransaction { implicit session: Session =>
                dao.retrieveUser(email) match {
                  case Left(error) =>
                    Auth(email, password, auth=false)
                  case Right(user) =>
                    if (user.password == password)
                      Auth(email, password, auth=true)
                    else
                      Auth(email, password, auth=false)
                }
              }
            } catch {
              case error: Throwable =>
                haltAndLog(500, "something went wrong with database")
            }
        }
    }
  }

  case class Auth(email: String, password: String, auth: Boolean)

  def haltAndLog(statusCode: Int, message: String) {
    println(s"ERROR $statusCode: $message")
    halt(status=statusCode, reason=message)
  }
}