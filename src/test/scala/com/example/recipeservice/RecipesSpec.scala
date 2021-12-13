package com.example.recipeservice

import java.util.UUID
import cats.effect.IO
import com.example.recipeservice.application.Recipes.{Recipe, RecipeMessage}
import com.example.recipeservice.database.{Migrations, RecipesRepository}
import org.http4s._
import org.http4s.implicits._
import munit.CatsEffectSuite
import org.http4s
import io.circe.syntax._
import org.http4s.circe._

class RecipesSpec extends CatsEffectSuite {

  test("creating recipes") {
    val recipeName = s"Recipe - ${UUID.randomUUID()}"
    val createdRecipeResponse: Response[IO] = createRecipe(Recipe(name = recipeName))
    val createdRecipe: IO[Recipe] = createdRecipeResponse.as[Recipe]
    assert(createdRecipeResponse.status == Status.Created, s"Expected: ${Status.Created}, Actual: ${createdRecipeResponse.status}")
    assertIO(createdRecipe.map(_.name), recipeName)
    assertIOBoolean(createdRecipe.map(_.id.isDefined), "id was not defined")
  }

  test("retrieving recipes") {
    val recipeName = s"Recipe - ${UUID.randomUUID()}"

    val createdRecipe: Recipe = createRecipe(Recipe(name = recipeName)).as[Recipe].unsafeRunSync()
    val recipeId: UUID = createdRecipe.id.getOrElse(fail("identifier was not provided"))
    val resolvedRecipeResponse = getRecipe(recipeId.toString)
    val resolvedRecipe = resolvedRecipeResponse.as[Recipe]
    assert(resolvedRecipeResponse.status == Status.Ok, s"Expected: ${Status.Ok}, Actual: ${resolvedRecipeResponse.status}")
    assertIO(resolvedRecipe.map(_.name), recipeName)
    assertIOBoolean(resolvedRecipe.map(_.id.contains(recipeId)), "id did not match")
  }

  test("retrieving non-existent recipes") {
    val recipeId = UUID.randomUUID().toString
    val resolvedRecipeResponse = getRecipe(recipeId)
    val resolvedRecipe = resolvedRecipeResponse.as[RecipeMessage]
    assert(resolvedRecipeResponse.status == Status.NotFound, s"Expected: ${Status.NotFound}, Actual: ${resolvedRecipeResponse.status}")
    assertIO(resolvedRecipe.map(_.message), s"recipe did not exist with following identifier: $recipeId")
  }

  test("retrieving recipes with invalid identifiers") {
    val invalidRecipeId = "1234"
    val resolvedRecipeResponse = getRecipe(invalidRecipeId)
    val resolvedRecipe = resolvedRecipeResponse.as[RecipeMessage]
    assert(resolvedRecipeResponse.status == Status.BadRequest, s"Expected: ${Status.BadRequest}, Actual: ${resolvedRecipeResponse.status}")
    assertIO(resolvedRecipe.map(_.message), s"provided identifier was invalid: $invalidRecipeId")
  }


  val server: http4s.HttpApp[IO] = {
    Migrations.migrate[IO]().compile.drain.unsafeRunSync()
    RecipeRoutes.routes[IO](new RecipesRepository[IO]()).orNotFound
  }

  private[this] def getRecipe(id: String): Response[IO] = {
    val getRecipe: Request[IO] = Request[IO](Method.GET, uri"/v1/recipes" / id)
    this.server.run(getRecipe).unsafeRunSync()
  }


  private[this] def createRecipe(recipe: Recipe): Response[IO] = {
    val postRecipe: Request[IO] = Request[IO](Method.POST, uri"/v1/recipes").withEntity(recipe.asJson)
    this.server.run(postRecipe).unsafeRunSync()
  }
}
