package com.example.recipeservice

import cats.effect.Sync
import cats.implicits._
import com.example.recipeservice.application.Users
import com.example.recipeservice.application.Users.{User, UserMessage}
import com.typesafe.scalalogging.Logger
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object UserRoutes {

  def routes[F[_] : Sync](users: Users[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    val logger = Logger("Root")
    import dsl._

    HttpRoutes.of[F] {
      case request @ POST -> Root / "register" =>
        for {
          user <- request.as[User]
          createdRecipeE <- users.create(user)
          resp <- createdRecipeE match {
            case Left(message) => BadRequest(message)
            case Right(createdRecipe) => Created(createdRecipe)
          }
        } yield resp
      case GET -> Root / id =>
        logger.debug("debug log in user get request")

        for {
          resolvedUserO <- users.findById(id)
          resp <- resolvedUserO match {
            case Right(resolvedUserO) => Ok(resolvedUserO)
            case Left(userMessage@UserMessage(message))
              if message.startsWith("user did not exist with following identifier") => NotFound(userMessage)
            case Left(userMessage) => BadRequest(userMessage)
          }
        } yield {
          resp
        }
    }
  }
}
