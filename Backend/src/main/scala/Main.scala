import org.http4s.ember.server._  // Para usar el servidor Ember
import cats.effect._
import controller.{PersonaRoutes, ImagenRoutes,DocumentoRoutes,LibroRoutes,ImagenRoutesfront,CategoriaRoutes, LoginRoutes}
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
       "/" -> LoginRoutes.rutas
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