import org.http4s.ember.server._  // Para usar el servidor Ember
import cats.effect._
import controller.{PersonaRoutes, ImagenRoutes, DocumentoRoutes, LibroRoutes, ImagenRoutesfront, CategoriaRoutes, 
LoginRoutes, eliminar_libros_routes, Reportesdeadmin, A_admin, Paginapricipal,Comprarlibros,Paginafillter}
import com.comcast.ip4s._
import org.http4s.server.Router
import org.http4s.implicits._
import org.http4s.server.middleware.CORS

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    
    val httpApp = Router(
  "/persona"  -> PersonaRoutes.rutas,
  "/imagen"  -> ImagenRoutes.rutas,
  "/documento" -> DocumentoRoutes.rutas,
  "/libro"   -> LibroRoutes.rutas,
  "/frontimg"  -> ImagenRoutesfront.rutas,
  "/categoria" -> CategoriaRoutes.rutas,
  "/login"  -> LoginRoutes.rutas,
  "/eliminar"  -> eliminar_libros_routes.rutas,
  "/reportes" -> Reportesdeadmin.rutas,
  "/admin"    -> A_admin.rutas,
  "/pagina" -> Paginapricipal.rutas,
  "/comprarlibro" ->  Comprarlibros.rutas,
  "/filtrar" -> Paginafillter.rutas
)

    // Aplicar CORS permitiendo solo localhost:8080
   val httpAppWithCors = CORS.policy
  .withAllowOriginAll
  .withAllowCredentials(false)
  .withAllowMethodsIn(Set(
    org.http4s.Method.GET,
    org.http4s.Method.POST,
    org.http4s.Method.PUT,
    org.http4s.Method.DELETE,
    org.http4s.Method.OPTIONS
  ))
  .apply(httpApp)
  .orNotFound

    // Iniciar servidor
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(Port.fromInt(8081).get)
      .withHttpApp(httpAppWithCors)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}