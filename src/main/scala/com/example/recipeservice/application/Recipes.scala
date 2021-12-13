package com.example.recipeservice.application

import cats.effect.Sync
import com.example.recipeservice.application.Recipes.{Recipe, RecipeMessage}
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder}

import java.util.UUID

trait Recipes[F[_]] {
  def create(recipe: Recipes.Recipe): F[Either[RecipeMessage, Recipe]]

  def findById(id: String): F[Either[RecipeMessage, Recipe]]
}

object Recipes {

  case class Recipe(id: Option[UUID] = None, name: String)

  case class RecipeMessage(message: String)

  object RecipeMessage {
    implicit val recipeMessageDecoder: Decoder[RecipeMessage] = deriveDecoder[RecipeMessage]

    implicit def recipeMessageEntityDecoder[F[_] : Sync]: EntityDecoder[F, RecipeMessage] = jsonOf

    implicit val recipeMessageEncoder: Encoder[RecipeMessage] = deriveEncoder[RecipeMessage]

    implicit def recipeMessageEntityEncoder[F[_] : Sync]: EntityEncoder[F, RecipeMessage] = jsonEncoderOf

  }

  object Recipe {
    implicit val recipeDecoder: Decoder[Recipe] = deriveDecoder[Recipe]

    implicit def recipeEntityDecoder[F[_] : Sync]: EntityDecoder[F, Recipe] = jsonOf

    implicit val recipeEncoder: Encoder[Recipe] = deriveEncoder[Recipe]

    implicit def recipeEntityEncoder[F[_] : Sync]: EntityEncoder[F, Recipe] = jsonEncoderOf
  }

}
