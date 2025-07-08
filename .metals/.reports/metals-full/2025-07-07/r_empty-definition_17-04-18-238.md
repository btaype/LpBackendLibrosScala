error id: file:///F:/LPFinal/Backend/src/main/scala/Main.scala:`<none>`.
file:///F:/LPFinal/Backend/src/main/scala/Main.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -org/http4s/ember/server/Paginapricipal.
	 -org/http4s/ember/server/Paginapricipal#
	 -org/http4s/ember/server/Paginapricipal().
	 -cats/effect/Paginapricipal.
	 -cats/effect/Paginapricipal#
	 -cats/effect/Paginapricipal().
	 -controller/Paginapricipal.
	 -controller/Paginapricipal#
	 -controller/Paginapricipal().
	 -com/comcast/ip4s/Paginapricipal.
	 -com/comcast/ip4s/Paginapricipal#
	 -com/comcast/ip4s/Paginapricipal().
	 -org/http4s/implicits/Paginapricipal.
	 -org/http4s/implicits/Paginapricipal#
	 -org/http4s/implicits/Paginapricipal().
	 -Paginapricipal.
	 -Paginapricipal#
	 -Paginapricipal().
	 -scala/Predef.Paginapricipal.
	 -scala/Predef.Paginapricipal#
	 -scala/Predef.Paginapricipal().
offset: 261
uri: file:///F:/LPFinal/Backend/src/main/scala/Main.scala
text:
```scala
import org.http4s.ember.server._  // Para usar el servidor Ember
import cats.effect._
import controller.{PersonaRoutes, ImagenRoutes,DocumentoRoutes,LibroRoutes,ImagenRoutesfront,CategoriaRoutes, LoginRoutes,eliminar_libros_routes,
Reportesdeadmin,A_admin,Pa@@ginapricipal}
import com.comcast.ip4s._
import org.http4s.server.Router
import org.http4s.implicits._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    val httpApp = Router(
      "/" -> PersonaRoutes.rutas,
      "/" -> ImagenRoutes.rutas, 
      "/" -> DocumentoRoutes.rutas,
       "/" -> LibroRoutes.rutas,
       "/" -> ImagenRoutesfront.rutas,
       "/" -> CategoriaRoutes.rutas,
       "/" -> LoginRoutes.rutas,
       "/"->  eliminar_libros_routes.rutas,
       "/" -> Reportesdeadmin.rutas
    ).orNotFound

    EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString("localhost").get)
      .withPort(Port.fromInt(8081).get)
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.