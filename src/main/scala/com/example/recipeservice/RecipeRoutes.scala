package com.example.recipeservice

import cats.effect.Sync
import cats.implicits._
import com.example.recipeservice.application.Recipes.{Recipe, RecipeMessage}
import com.example.recipeservice.application.Recipes
import com.typesafe.scalalogging.Logger
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object RecipeRoutes {

  def routes[F[_] : Sync](recipes: Recipes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    val logger = Logger("Root")

    import dsl._

    HttpRoutes.of[F] {
      case request @ POST -> Root =>
        for {
          recipe <- request.as[Recipe]
          createdRecipeE <- recipes.create(recipe)
          resp <- createdRecipeE match {
            case Left(message) => BadRequest(message)
            case Right(createdRecipe) => Created(createdRecipe)
          }
        } yield resp
      case GET -> Root / id =>
        logger.debug("debug log in recipes get request")

        for {
          resolvedRecipeO <- recipes.findById(id)
          resp <- resolvedRecipeO match {
            case Right(resolvedRecipe) => Ok(resolvedRecipe)
            case Left(recipeMessage@RecipeMessage(message)) if message.startsWith("recipe did not exist with following identifier") => NotFound(recipeMessage)
            case Left(recipeMessage) => BadRequest(recipeMessage)
          }
        } yield {
          resp
        }
    }
  }
}
