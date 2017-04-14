package example

import cats.data.Coproduct
import cats.free.{Free, Inject}
import cats.{Id, ~>}

import scala.language.higherKinds

object FreeMonadTesting {

  case class ReceiptEntity()
  case class User()

  sealed trait Error
  case object TestError extends Error

  // Receipts
  sealed trait ReceiptOp[A]
  case class GetReceipt(id: String)                          extends ReceiptOp[Either[Error, ReceiptEntity]]

  class ReceiptOps[F[_]](implicit I: Inject[ReceiptOp, F]) {
    def getReceipt(id: String): Free[F, Either[Error, ReceiptEntity]] = Free.inject[ReceiptOp, F](GetReceipt(id))
  }

  object ReceiptOps {
    implicit def receiptOps[F[_]](implicit I: Inject[ReceiptOp, F]): ReceiptOps[F] = new ReceiptOps[F]
  }

  // Users
  sealed trait UserOp[A]
  case class GetUser(id: String) extends UserOp[Either[Error, User]]

  class UserOps[F[_]](implicit I: Inject[UserOp, F]) {
    def getUser(id: String): Free[F, Either[Error, User]] = Free.inject[UserOp, F](GetUser(id))
  }

  object UserOps {
    implicit def userOps[F[_]](implicit I: Inject[UserOp, F]): UserOps[F] = new UserOps[F]
  }

  type ReceiptsApp[A] = Coproduct[ReceiptOp, UserOp, A]
  type Program[A] = Free[ReceiptsApp, A]

  def program(implicit RO: ReceiptOps[ReceiptsApp], UO: UserOps[ReceiptsApp]): Program[String] = {

    import RO._, UO._

    for {
      user <- getUser("user_id")
      receipt <- getReceipt("test " + user.isLeft)
    } yield "some result"
  }

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

  val interpreter: ReceiptsApp ~> Id = TestReceiptInterpreter or TestUserInterpreter

  val result: String = program.foldMap(interpreter)
}

