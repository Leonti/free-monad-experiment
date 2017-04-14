package example

import cats.free._
import cats.{Id, ~>}
import freek._

import scala.language.higherKinds

object FreeMonadsFreek {

  case class ReceiptEntity()
  case class User()

  sealed trait Error
  case object TestError extends Error

  // Receipts
  sealed trait ReceiptOp[A]
  case class GetReceipt(id: String)                          extends ReceiptOp[Either[Error, ReceiptEntity]]

  // Users
  sealed trait UserOp[A]
  case class GetUser(id: String) extends UserOp[Either[Error, User]]

  type PRG = ReceiptOp :|: UserOp :|: NilDSL
  val PRG = DSL.Make[PRG]

//  type O = Either[Error, ?] :&: Bulb
/*
Compiling 1 Scala source to /home/leonti/development/free-monad-experiment/target/scala-2.12/classes...
[error] /home/leonti/development/free-monad-experiment/src/main/scala/example/FreeMonadsFreek.scala:30: not found: type ?
[error]   type O = Either[Error, ?] :&: Bulb
[error]                          ^
[error] /home/leonti/development/free-monad-experiment/src/main/scala/example/FreeMonadsFreek.scala:30: Either[example.FreeMonadsFreek.Error,<error>] takes no type parameters, expected: one
[error]   type O = Either[Error, ?] :&: Bulb
[error]            ^
[error] two errors found
 */

  def programFreek(): Free[PRG.Cop, String] =
    for {
      // this one is still Either[Error, User]
      user <- GetUser("user_id").freek[PRG]
      receipt <- GetReceipt("test " + user.isLeft).freek[PRG]
    } yield "test"

  object TestReceiptInterpreter extends (ReceiptOp ~> Id) {
    def apply[A](i: ReceiptOp[A]): Id[A] = i match {
      case GetReceipt(id) => Left(TestError)
    }
  }

  object TestUserInterpreter extends (UserOp ~> Id) {
    def apply[A](i: UserOp[A]): Id[A] = i match {
      case GetUser(id) => Left(TestError)
    }
  }

  val interpreterFreek: Interpreter[PRG.Cop, Id] = TestReceiptInterpreter :&: TestUserInterpreter

  val resultFreek: String = programFreek.interpret(interpreterFreek)
}
