package com.example.recipeservice.database

import cats.effect.Sync
import com.example.recipeservice.application.Recipes.{Recipe, RecipeMessage}
import com.example.recipeservice.application.Recipes
import io.getquill.EntityQuery

import java.util.UUID

class RecipesRepository[F[_] : Sync] extends Recipes[F] {

  import ctx._

  val recipes: ctx.Quoted[EntityQuery[Recipe]] = quote {
    querySchema[Recipe]("recipes")
  }

  def create(recipe: Recipe): F[Either[RecipeMessage, Recipe]] = {
    Sync[F].delay {
      ctx.run(recipes.insert(_.name -> lift(recipe.name)).returning(_.id)).map { uiid =>
        Right(recipe.copy(id = Some(uiid)))
      }.getOrElse(Left(RecipeMessage("")))
    }
  }

  def fromUUID(uuid: String): Option[UUID] = {
    try {
      Option(UUID.fromString(uuid))
    } catch {
      case _: IllegalArgumentException => None
    }
  }

  def findById(id: String): F[Either[RecipeMessage, Recipe]] = {
    fromUUID(id).map { id =>
      Sync[F].delay {
        ctx.run(recipes.filter(_.id.contains(lift(id)))) match {
          case Seq(recipe) => Right(recipe)
          case Seq() => Left(RecipeMessage(s"recipe did not exist with following identifier: $id"))
          case _ => Left(RecipeMessage(""))
        }
      }
    }.getOrElse(Sync[F].pure(Left(RecipeMessage(s"provided identifier was invalid: $id"))))
  }
}
