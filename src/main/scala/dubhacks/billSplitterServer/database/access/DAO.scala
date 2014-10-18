package dubhacks.billSplitterServer.database.access

import java.sql.Timestamp

import scala.slick.driver.{JdbcDriver, JdbcProfile}
import scala.slick.lifted.{Tag => LiftedTag}
import org.joda.time.{DateTime, DateTimeZone}

import dubhacks.billSplitterServer.models._

/**
 * Created by aerust on 10/17/14.
 */
class DAO(val driver: JdbcDriver) {

  // Import the query language features from the driver
  import driver.simple._

  protected val production = false

  /**
   * Users table
   */
  class Users(tag: LiftedTag) extends Table[(Int, String, String, String, String, String, Timestamp, Timestamp)](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)  // This is the primary key column
    def emailAddress = column[String]("email_address") // This is unique
    def fullName = column[String]("full_name")
    def phoneNumber = column[String]("phone_number") // This is unique
    def streetAddress = column[String]("street_address")
    def password = column[String]("password")
    def createdAt = column[Timestamp]("created_at")
    def updatedAt = column[Timestamp]("updated_at")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, emailAddress, fullName, phoneNumber, streetAddress, password, createdAt, updatedAt)

    // email column should have unique values
    def uniqueEmailAddress = index("idx_source_email_address", emailAddress, unique = true)

    // email column should have unique values
    def uniquePhoneNumber = index("idx_source_phone_number", emailAddress, unique = true)
  }

  /**
   * Cards tables
   */
  class Cards(tag: LiftedTag) extends Table[(Int, Int, String, String, Timestamp,Timestamp)](tag, "cards") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key
    def userId = column[Int]("user_id")
    def cardNum = column[String]("card_num")
    def streetAddress = column[String]("street_address")
    def createdAt = column[Timestamp]("created_at")
    def updatedAt = column[Timestamp]("updated_at")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, userId, cardNum, streetAddress, createdAt, updatedAt)

    // A reified foreign key relation that can be navigated to create a join
    // creates n to 1 mapping from cards to users
    def users = foreignKey("users_fk", userId, TableQuery[Users])(_.id)
  }

  /**
   * From Transactions table
   */
  class FromTransactions(tag: LiftedTag) extends Table[(Int, Int, Int, Int, Boolean, Boolean, Timestamp, Timestamp)](tag, "from_transactions") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key
    def userId = column[Int]("user_id")
    def cardId = column[Int]("card_id")
    def amount = column[Int]("amount")
    def accepted = column[Boolean]("accepted")
    def paid = column[Boolean]("paid")
    def createdAt = column[Timestamp]("created_at")
    def updatedAt = column[Timestamp]("updated_at")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, userId, cardId, amount, accepted, paid, createdAt, updatedAt)

    // A reified foreign key relation that can be navigated to create a join
    // creates n to 1 mapping from fromTrans to users
    def users = foreignKey("users_fk", userId, TableQuery[Users])(_.id)

    // A reified foreign key relation that can be navigated to create a join
    // creates n to 1 mapping from fromTrans to cards
    def cards = foreignKey("cards_fk", userId, TableQuery[Users])(_.id)
  }

  /**
   * To Transactions table
   */
  class ToTransactions(tag: LiftedTag) extends Table[(Int, Int, Int, Int, Int, Boolean, Boolean, Timestamp, Timestamp)](tag, "to_transactions") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key
    def userId = column[Int]("user_id")
    def cardId = column[Int]("card_id")
    def fromTransactionId = column[Int]("from_transaction_id")
    def amount = column[Int]("amount")
    def accepted = column[Boolean]("accepted")
    def paid = column[Boolean]("paid")
    def createdAt = column[Timestamp]("created_at")
    def updatedAt = column[Timestamp]("updated_at")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, userId, cardId, fromTransactionId, amount, accepted, paid, createdAt, updatedAt)

    // A reified foreign key relation that can be navigated to create a join
    // creates n to 1 mapping from toTrans to users
    def users = foreignKey("users_fk", userId, TableQuery[Users])(_.id)

    // A reified foreign key relation that can be navigated to create a join
    // creates n to 1 mapping from toTrans to fromTrans
    def fromTransactions = foreignKey("from_transactions_fk", userId, TableQuery[FromTransactions])(_.id)

    // A reified foreign key relation that can be navigated to create a join
    // creates n to 1 mapping from toTrans to cards
    def cards = foreignKey("cards_fk", userId, TableQuery[Cards])(_.id)
  }

  // Query Tables
  val users = TableQuery[Users]
  val cards = TableQuery[Cards]
  val fromTrans = TableQuery[FromTransactions]
  val toTrans = TableQuery[ToTransactions]

  // some non-production functions
  def createTables(implicit session: Session) {
    if (production) throw new Exception("Cannot create tables in production!")
    (users.ddl ++ cards.ddl ++ fromTrans.ddl ++ toTrans.ddl).create
  }

  def populateTables(implicit session: Session) {
    if (production) throw new Exception("Cannot populate tables in production!")
    val time0 = new Timestamp(0)

    // add data to users
    users +=(0, "bobsmith@gmail.com", "Bob Smith", "0001234567", "address", "password", time0, time0)
  }

  def dropTables(implicit session: Session) {
    if (production) throw new Exception("Cannot drop tables in production!")
    (users.ddl ++ cards.ddl ++ fromTrans.ddl ++ toTrans.ddl).drop
  }


  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CRUD for users
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def createUser(emailAddress: String, fullName: String, phoneNumber: String, streetAddress: String,
                 password: String)(implicit session: Session): Either[UserAlreadyExistsException, User] = {
    users.filter(_.emailAddress === emailAddress).list().headOption match {
      case Some((id, otherEmailAddress, _, _, _, _, _, _)) =>
        Left(new UserAlreadyExistsException(id, Some(otherEmailAddress), None))
      case None =>
        users.filter(_.phoneNumber === phoneNumber).list().headOption match {
          case Some((id, _, _, otherPhoneNumber, _, _, _, _)) =>
            Left(new UserAlreadyExistsException(id, None, Some(otherPhoneNumber)))
          case None =>
            val jodaTime = currentTime
            val timestamp = asTimestamp(jodaTime)
            users +=(0, emailAddress, fullName, phoneNumber, streetAddress, password, timestamp, timestamp)
            Right(new User(emailAddress, fullName, phoneNumber, streetAddress, password, List[Card](), jodaTime, jodaTime))
        }
    }
  }

  def retrieveUser(emailAddress: String)(implicit session: Session): Either[NoSuchUserException, User] = {
    users.filter(_.emailAddress === emailAddress).list().headOption match {
      case None =>
        Left(NoSuchUserException(Option(emailAddress), None))
      case Some((id, _, fullName, phoneNumber, streetAddress, password, createdAt, updatedAt)) =>
        val userCards = cards.filter(_.userId === id).list().map(
          {case (cardId, _, cardNum, cardStreetAddress, cardCreatedAt, cardUpdatedAt) =>
          Card(cardId, emailAddress, cardNum: String, cardStreetAddress: String, asJoda(cardCreatedAt), asJoda(cardUpdatedAt))
          }
        )
        Right(User(emailAddress, fullName, phoneNumber, streetAddress, password, userCards, asJoda(createdAt), asJoda(updatedAt)))
    }
  }

  def createCard(userEmailAddress: String, cardNum: String, streetAddress: String)(implicit session: Session): Either[NoSuchUserException, Card] = {
    users.filter(_.emailAddress === userEmailAddress).list().headOption match {
      case None =>
        Left(NoSuchUserException(Option(userEmailAddress), None))
      case Some((userId, _, _, _, _, password, _, _)) =>
        val jodaTime = currentTime
        val timestamp = asTimestamp(jodaTime)
        cards +=(0, userId, cardNum, streetAddress, timestamp, timestamp)
        val (cardId: Int, _, _, _, _, _) = cards.filter(x => x.userId === userId && x.cardNum === cardNum).list().head
        Right(Card(cardId, userEmailAddress, cardNum, streetAddress, jodaTime, jodaTime))
    }
  }

  // Returns the current time as a DateTime
  // Uses UTC
  private def currentTime: DateTime = {
    new DateTime(DateTimeZone.UTC)
  }

  // Returns a java.sql.Timestamp
  private def asTimestamp(jodaTime: DateTime): Timestamp = {
    new java.sql.Timestamp(jodaTime.getMillis)
  }

  // Returns a org.joda.DateTime
  private def asJoda(timestamp: Timestamp): DateTime = {
    val timeZone = DateTimeZone.UTC
    new DateTime(timestamp.getTime, timeZone)
  }
}
