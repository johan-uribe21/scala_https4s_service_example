package com.example.recipeservice.database

import cats.effect.Sync
import com.example.recipeservice.application.Users
import com.example.recipeservice.application.Users.{User, UserMessage}
import io.getquill.EntityQuery

import java.util.UUID

class UsersRepository[F[_] : Sync] extends Users[F] {

  import ctx._

  val users: ctx.Quoted[EntityQuery[User]] = quote {
    querySchema[User]("users")
  }

  def create(user: User): F[Either[UserMessage, User]] = {
    Sync[F].delay {
      ctx.run(users.insert(_.name -> lift(user.name)).returning(_.id)).map { uiid =>
        Right(user.copy(id = Some(uiid)))
      }.getOrElse(Left(UserMessage("User failed to create")))
    }
  }

  def fromUUID(uuid: String): Option[UUID] = {
    try {
      Option(UUID.fromString(uuid))
    } catch {
      case _: IllegalArgumentException => None
    }
  }

  def findById(id: String): F[Either[UserMessage, User]] = {
    fromUUID(id).map { id =>
      Sync[F].delay {
        ctx.run(users.filter(_.id.contains(lift(id)))) match {
          case Seq(recipe) => Right(recipe)
          case Seq() => Left(UserMessage(s"user did not exist with following identifier: $id"))
          case _ => Left(UserMessage(""))
        }
      }
    }.getOrElse(Sync[F].pure(Left(UserMessage(s"provided identifier was invalid: $id"))))
  }
}
