
// The simplest possible sbt build file is just one line:

scalaVersion := "2.13.12"
// That is, to create a valid sbt build, all you've got to do is define the
// version of Scala you'd like your project to use.

// ============================================================================

// Lines like the above defining `scalaVersion` are called "settings". Settings
// are key/value pairs. In the case of `scalaVersion`, the key is "scalaVersion"
// and the value is "2.13.12"

// It's possible to define many kinds of settings, such as:
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.4.1",
  "org.postgresql" % "postgresql" % "42.7.3",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1" // importante: conexión con pool
)

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-server" % "0.23.26",
  "org.http4s" %% "http4s-dsl" % "0.23.26",
  "org.typelevel" %% "cats-effect" % "3.5.2",
  "com.typesafe.slick" %% "slick" % "3.4.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
  
  "org.http4s" %% "http4s-circe" % "0.23.26",    // Para integrar Circe con http4s
"io.circe" %% "circe-generic" % "0.14.1",       // Para la serialización/deserialización automática de objetos
"io.circe" %% "circe-parser" % "0.14.1",        // Para parsear JSON
 "org.mindrot" % "jbcrypt" % "0.4" , 
 "org.http4s" %% "http4s-core" % "0.23.26"                // Para bcrypt
)
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.14"
libraryDependencies += "com.zaxxer" % "HikariCP" % "5.0.0"
libraryDependencies += "co.fs2" %% "fs2-io" % "3.9.3"
name := "hello-world"
organization := "ch.epfl.scala"
version := "1.0"
Compile / run / fork := true
javaHome := Some(file("C:/Program Files/Java/jdk-11"))
// Note, it's not required for you to define these three settings. These are
// mostly only necessary if you intend to publish your library's binaries on a
// place like Sonatype.


// Want to use a published library in your project?
// You can define other libraries as dependencies in your build like this:

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"

// Here, `libraryDependencies` is a set of dependencies, and by using `+=`,
// we're adding the scala-parser-combinators dependency to the set of dependencies
// that sbt will go and fetch when it starts up.
// Now, in any Scala file, you can import classes, objects, etc., from
// scala-parser-combinators with a regular import.

// TIP: To find the "dependency" that you need to add to the
// `libraryDependencies` set, which in the above example looks like this:

// "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"

// You can use Scaladex, an index of all known published Scala libraries. There,
// after you find the library you want, you can just copy/paste the dependency
// information that you need into your build file. For example, on the
// scala/scala-parser-combinators Scaladex page,
// https://index.scala-lang.org/scala/scala-parser-combinators, you can copy/paste
// the sbt dependency from the sbt box on the right-hand side of the screen.

// IMPORTANT NOTE: while build files look _kind of_ like regular Scala, it's
// important to note that syntax in *.sbt files doesn't always behave like
// regular Scala. For example, notice in this build file that it's not required
// to put our settings into an enclosing object or class. Always remember that
// sbt is a bit different, semantically, than vanilla Scala.

// ============================================================================

// Most moderately interesting Scala projects don't make use of the very simple
// build file style (called "bare style") used in this build.sbt file. Most
// intermediate Scala projects make use of so-called "multi-project" builds. A
// multi-project build makes it possible to have different folders which sbt can
// be configured differently for. That is, you may wish to have different
// dependencies or different testing frameworks defined for different parts of
// your codebase. Multi-project builds make this possible.

// Here's a quick glimpse of what a multi-project build looks like for this
// build, with only one "subproject" defined, called `root`:

// lazy val root = (project in file(".")).
//   settings(
//     inThisBuild(List(
//       organization := "ch.epfl.scala",
//       scalaVersion := "2.13.12"
//     )),
//     name := "hello-world"
//   )

// To learn more about multi-project builds, head over to the official sbt
// documentation at http://www.scala-sbt.org/documentation.html
