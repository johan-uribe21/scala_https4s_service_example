package com.example.recipeservice.application

import cats.effect.Sync
import com.example.recipeservice.application.Users.{User, UserMessage}
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder}

import java.util.UUID

trait Users[F[_]] {
  def create(user: Users.User): F[Either[UserMessage, User]]

  def findById(id: String): F[Either[UserMessage, User]]
}

object Users {

  case class User(id: Option[UUID] = None, name: String)

  case class UserMessage(message: String)

  object UserMessage {
    implicit val recipeMessageDecoder: Decoder[UserMessage] = deriveDecoder[UserMessage]

    implicit def recipeMessageEntityDecoder[F[_] : Sync]: EntityDecoder[F, UserMessage] = jsonOf

    implicit val recipeMessageEncoder: Encoder[UserMessage] = deriveEncoder[UserMessage]

    implicit def recipeMessageEntityEncoder[F[_] : Sync]: EntityEncoder[F, UserMessage] = jsonEncoderOf

  }

  object User {
    implicit val userDecoder: Decoder[User] = deriveDecoder[User]

    implicit def userEntityDecoder[F[_] : Sync]: EntityDecoder[F, User] = jsonOf

    implicit val userEncoder: Encoder[User] = deriveEncoder[User]

    implicit def userEntityEncoder[F[_] : Sync]: EntityEncoder[F, User] = jsonEncoderOf
  }

}
