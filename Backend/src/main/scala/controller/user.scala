package controller

import Model._ 
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityDecoder._

import io.circe.generic.auto._
import cats.effect.IO
import java.nio.file.{Paths, Files, StandardOpenOption}
import fs2.io.file.{Files => Fs2Files, Path => Fs2Path} 
import org.http4s._


object PersonaRoutes {

  val rutas = HttpRoutes.of[IO] {
    
    case req @ POST -> Root / "insertar_persona" =>
      for {
      
        persona <- req.as[Persona]
         _ <- IO(println(persona))
       
        val hashedPassword = PersonaQueries.hashPassword(persona.contrasena)

        
       
         _ <- PersonaQueries.insertarPersona(persona.nombre, persona.rol, hashedPassword, persona.correo, persona.dni)
       
        response <- Ok(s"Persona ${persona.nombre} insertada exitosamente.")
      } yield response
  }
}
object ImagenRoutes {

  val rutas = HttpRoutes.of[IO] {
    case req @ POST -> Root / "upload" / nombre =>
      for {
        bytes <- req.body.compile.to(Array)
        _ <- IO {
          val ruta = Paths.get(s"uploads/$nombre")
          
          Files.createDirectories(ruta.getParent)
          Files.write(ruta, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        }
        res <- Ok(s"Imagen $nombre guardada con éxito")
      } yield res
  }
}

object DocumentoRoutes {

  val rutas = HttpRoutes.of[IO] {
    case req @ POST -> Root / "uploadpdf" / nombre if nombre.toLowerCase.endsWith(".pdf") =>
      for {
        bytes <- req.body.compile.to(Array)

        _ <- IO {
          val ruta = Paths.get(s"librospdf/$nombre")
          Files.createDirectories(ruta.getParent)
          Files.write(ruta, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        }

        res <- Ok(s"Documento $nombre guardado en librospdf/")
      } yield res

    
    case POST -> Root / "uploadpdf" / nombre =>
      BadRequest("Solo se permiten archivos con extensión .pdf")
  }
}
object LibroRoutes {
  val rutas = HttpRoutes.of[IO] {
    case req @ POST -> Root / "insertar_libro" =>
      for {
        libro <- req.as[Libro]
        resultado <- LibroQueries.insertarLibro(
          libro.nombre,
          libro.precio,
          libro.idCategoria,
          libro.nombrePdf,
          libro.nombreImagen
        )
        res <- Ok(resultado)
      } yield res
  }
}



object ImagenRoutesfront {
  val rutas = HttpRoutes.of[IO] {
    case GET -> Root / "imagen" / nombre =>
      val ruta = Fs2Path(s"uploads/$nombre")
      Fs2Files[IO].exists(ruta).flatMap {
        case true =>
          StaticFile.fromPath(ruta, Some(Request[IO]()))
            .getOrElseF(NotFound(s"No se pudo servir el archivo $nombre"))
        case false =>
          NotFound(s"Imagen '$nombre' no encontrada en uploads/")
      }
  }
}

object CategoriaRoutes {
  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityEncoder._

  val rutas = HttpRoutes.of[IO] {
    case GET -> Root / "categorias" =>
      for {
        categorias <- LibroQueries.obtenerCategorias()
        res <- Ok(categorias)
      } yield res
  }
}
object LoginRoutes {
   import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityEncoder._
  val rutas = HttpRoutes.of[IO] {
    case req @ POST -> Root / "login" =>
      for {
        login <- req.as[LoginRequest]
        resultado <- PersonaQueries.loginPersona(login.correo, login.contrasena)
        res <- resultado match {
          case Right(persona) => Ok(persona)
          case Left(error) => Forbidden(error)
        }
      } yield res
  }
}
