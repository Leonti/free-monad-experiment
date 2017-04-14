package example

import cats.free._
import cats.instances.either._
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
  case class GetReceipt(id: String) extends ReceiptOp[Either[Error, ReceiptEntity]]

  // Users
  sealed trait UserOp[A]
  case class GetUser(id: String) extends UserOp[Either[Error, User]]

  type PRG = ReceiptOp :|: UserOp :|: NilDSL
  val PRG = DSL.Make[PRG]

  type O = Either[Error, ?] :&: Bulb

  val program =
    for {
      user <- GetUser("user_id").freek[PRG].onionT[O]
      receipt <- GetReceipt("test " + user).freek[PRG].onionT[O]
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

  val interpreters: Interpreter[PRG.Cop, Id] = TestReceiptInterpreter :&: TestUserInterpreter

  val result: Either[Error, String] = program.value.interpret(interpreters)
}
