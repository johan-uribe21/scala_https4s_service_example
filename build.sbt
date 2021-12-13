val Http4sVersion = "0.21.14"
val CirceVersion = "0.13.0"
val MunitVersion = "0.7.20"
val LogbackVersion = "1.2.3"
val MunitCatsEffectVersion = "0.12.0"

//def init(): Unit = {
//  sys.props.put("quill.macro.log", true.toString)
//  sys.props.put("quill.binds.log", false.toString)
//  sys.props.put("quill.macro.log.pretty", true.toString)
//}

//val fake = init()

lazy val root = (project in file("."))
  .settings(
    organization := "com.example",
    name := "recipe-service",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.4",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.flywaydb" % "flyway-core" % "7.8.2",
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-2" % MunitCatsEffectVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.postgresql" % "postgresql" % "42.2.8",
      "io.getquill" %% "quill-jdbc" % "3.7.1",
      "org.scalameta" %% "svm-subs" % "20.2.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework"),
    scalacOptions ++= Seq(
      "-Dquill.binds.log=true",
      "-Dquill.macro.log=true",
      "-Dquill.macro.log.pretty=true",
//      "-Ypartial-unification"
    )
  )
