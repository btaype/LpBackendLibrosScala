error id: file:///F:/LPFinal/Backend/src/main/scala/controller/user.scala:`<none>`.
file:///F:/LPFinal/Backend/src/main/scala/controller/user.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -Model.LibroQueries.
	 -org/http4s/dsl/io/LibroQueries.
	 -org/http4s/circe/CirceEntityDecoder.LibroQueries.
	 -io/circe/generic/auto/LibroQueries.
	 -org/http4s/LibroQueries.
	 -org/http4s/circe/CirceEntityEncoder.LibroQueries.
	 -io/circe/syntax/LibroQueries.
	 -LibroQueries.
	 -scala/Predef.LibroQueries.
offset: 4451
uri: file:///F:/LPFinal/Backend/src/main/scala/controller/user.scala
text:
```scala
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

        
       
         _ <- PersonaQueries.insertarPersona(persona.nombre, "cliente", hashedPassword, persona.correo, persona.dni)
       
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
  import io.circe.syntax._                         
  import org.http4s.circe.CirceEntityEncoder._  
  val rutas = HttpRoutes.of[IO] {
    case req @ POST -> Root / "insertar_libro" =>
    for {
        libro <- req.as[Libro]
        resultado <- LibroQueries.insert_libro(
        libro.nombre,

        libro.precio,

        libro.id_categoria,
        libro.nombrepdf,
        libro.nombreimagen  

        )
         res <- if (resultado)
               Ok(Map( "ok" -> "Libro insertado Correctamente").asJson)
             else
               BadRequest(Map("error" -> "No se pudo insertar el libro").asJson)
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
        categorias <- LibroQueries.obtener_categorias()
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
object eliminar_libros_routes {

  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityEncoder._
   import io.circe.syntax._ 

  val rutas = HttpRoutes.of[IO] {
       case req @ GET -> Root / "eliminar_libro"   =>
      val id_libro = req.uri.query.params.get("id_libro").flatMap(id => scala.util.Try(id.toInt).toOption)
       id_libro match {
        case Some(id) =>
      for {
        resultado <- Lib@@roQueries.delete_libro(id)
        res <- if (resultado)
                 Ok(Map("ok" -> s"Libro eliminado Correctamente").asJson)
               else

                 BadRequest(Map("error" -> s"no se pudo eliminar el Libro ").asJson)
      } yield res
      case None =>
          BadRequest(Map("error" -> "el parametro es invalido").asJson)
      }
  }
}
object Reportesdeadmin {

  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityEncoder._
   import io.circe.syntax._ 
   
  val rutas = HttpRoutes.of[IO] {
       case req @ GET -> Root / "reporte"   =>
      val mes = req.uri.query.params.get("mes").flatMap(id => scala.util.Try(id.toInt).toOption)
      val anio = req.uri.query.params.get("anio").flatMap(id => scala.util.Try(id.toInt).toOption)

          (mes, anio) match {
        case (Some(m), Some(a)) =>
          for {
            resultado <- LibroQueries.reportes(m, a)
            res <- {
              val json = if (resultado.isEmpty) {
                
                Respuesta_reporte(Seq.empty, BigDecimal(0)).asJson

              } else {
                  Respuesta_reporte(resultado, resultado.head.total_ventas).asJson
              }
              Ok(json)
            }
          } yield res

        case _ =>
            BadRequest(Map("error" -> "el parametro es invlido").asJson)
      }
  }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.