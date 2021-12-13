package com.example.recipeservice

import cats.effect.{ConcurrentEffect, Timer}
import cats.implicits.toSemigroupKOps
import org.http4s.implicits._
import com.example.recipeservice.database.{Migrations, RecipesRepository, UsersRepository}
import fs2.Stream
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object RecipeserviceServer {

  def stream[F[_] : ConcurrentEffect](implicit T: Timer[F]): Stream[F, Nothing] = {
    // Combine Service Routes into an HttpApp.
    // Can also be done via a Router if you
    // want to extract a segments not checked
    // in the underlying routes.
    val recipeApp = RecipeRoutes.routes[F](new RecipesRepository[F]())
    val recipeRoutes = Router("/recipes" -> recipeApp)

    val userApp = UserRoutes.routes[F](new UsersRepository[F]())
    val userRoutes = Router("/users" -> userApp)

    val v1App = recipeRoutes <+> userRoutes

    // With Middlewares in place
    val httpApp = Router("/v1" -> v1App).orNotFound

    val finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

    for {
      _ <- Migrations.migrate[F]()
      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
