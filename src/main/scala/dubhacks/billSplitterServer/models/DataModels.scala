package dubhacks.billSplitterServer.models

import org.joda.time.DateTime

/**
 * Created by aerust on 10/18/14.
 */
case class User(emailAddress: String, fullName: String, phoneNumber: String , streetAddress: String,
                 password:String, cards: List[Card], createdAt: DateTime, updatedAt: DateTime)

case class Card(id: Int, user: String, cardNum: String, streetAddress: String, createdAt: DateTime, updatedAt: DateTime)

case class SubTransaction(id: Int, user: String, cardId: Int, amount: Int, accepted: Boolean,
                          paid: Boolean, createdAt : DateTime, updatedAt: DateTime)
