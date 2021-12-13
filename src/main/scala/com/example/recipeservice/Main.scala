package com.example.recipeservice

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = RecipeserviceServer.stream[IO].compile.drain.as(ExitCode.Success)
}
