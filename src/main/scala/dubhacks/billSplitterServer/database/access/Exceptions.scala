package dubhacks.billSplitterServer.database.access

/**
 * Exceptions
 */

case class UserAlreadyExistsException(id: Int, email: Option[String],
                                      phoneNumber: Option[String])
  extends Exception(s"User $id already exists with email $email or phone number $phoneNumber")

case class NoSuchUserException(email: Option[String], phoneNumber: Option[String])
  extends Exception(s"No such user with email $email or phone number $phoneNumber")
