file:///F:/LPFinal/Backend/src/main/scala/controller/user.scala
### java.lang.AssertionError: NoDenotation.owner

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
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
import org.http4s.multipart._

object PersonaRoutes {
  
  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityEncoder._
  import io.circe.syntax._ 
   
  val rutas = HttpRoutes.of[IO] {
    
    case req @ POST -> Root / "registrarcliente" =>
      for {
      
          persona <- req.as[Persona]
         _ <- IO(println(persona))
       
        val hashedPassword = PersonaQueries.hashPassword(persona.contrasena)

        
       
        
       resultado <- PersonaQueries.inserta_personas(persona.nombre, "cliente", hashedPassword, persona.correo, persona.dni)
         res <- {
          if (resultado.startsWith("OK"))

            Ok(Map("ok" -> resultado).asJson)
          else
            BadRequest(Map("error" -> resultado).asJson)
        }
      } yield res
      
  }
}
object ImagenRoutes {

  val rutas = HttpRoutes.of[IO] {
    case req @ POST -> Root / "upload" / nombre =>
      for {
        multipart <- req.as[Multipart[IO]] 
        _ <- multipart.parts.headOption match {
          case Some(part) =>
            part.body.compile.to(Array).flatMap { bytes =>
              IO {
                val ruta = Paths.get(s"uploads/$nombre")
                Files.createDirectories(ruta.getParent)
                Files.write(ruta, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
              }
            }
          case None =>
            IO.raiseError(new Exception("No se encontró archivo en el formulario"))
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
    case GET -> Root / "obtener_categoria" =>
      for {
        categorias <- LibroQueries.obtener_categorias()
        res <- Ok(categorias)
      } yield res
  }
}
object LoginRoutes {
     import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityEncoder._
  import io.circe.syntax._ 

  val rutas = HttpRoutes.of[IO] {
    case req @ POST -> Root / "login" =>
      for {
        login <- req.as[LoginRequest]
        resultado <- PersonaQueries.login_ersona(login.correo, login.contrasena)
        res <- resultado match {
          case Right(persona) => Ok(persona)
          case Left(error) => BadRequest(Map("error" -> error).asJson)
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
        resultado <- LibroQueries.delete_libro(id)
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
object A_admin {
  
  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityEncoder._
  import io.circe.syntax._ 
   
  val rutas = HttpRoutes.of[IO] {
    
    case req @ POST -> Root / "registraradmin" =>
      for {
      
          persona <- req.as[Persona]
         _ <- IO(println(persona))
       
        val hashedPassword = PersonaQueries.hashPassword(persona.contrasena)

        
       
        
       resultado <- PersonaQueries.inserta_personas(persona.nombre, "admin", hashedPassword, persona.correo, persona.dni)
         res <- {
          if (resultado.startsWith("OK"))

            Ok(Map("ok" -> resultado).asJson)
          else
            BadRequest(Map("error" -> resultado).asJson)
        }
      } yield res
      
  }
}

object Paginapricipal {

  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityEncoder._
   import io.circe.syntax._ 
   
  val rutas = HttpRoutes.of[IO] {
       case req @ GET -> Root / "pagina_principal"   =>
     

          for {
            resultado <- LibroQueries.paginap()
            res <- {
              val json = if (resultado.isEmpty) {
                
                 Seq.empty[Pagina_principal].asJson

              } else {
                  resultado.asJson
              }
              Ok(json)
            }
          } yield res

        
      }
  
}

object Comprarlibros {

  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityEncoder._
  import io.circe.syntax._
  import cats.effect.IO
  import org.http4s.Status

  val rutas = HttpRoutes.of[IO] {
    case req @ POST -> Root / "comprar" =>
      (for {
        _ <- IO(println("[DEBUG] Iniciando proceso de compra"))
        
        // Validar y parsear el JSON
        datos <- req.as[Comprarlibross]
          .handleErrorWith { e =>
            IO(println(s"[ERROR] Error al parsear JSON: ${e.getMessage}")) *>
            IO.raiseError(new RuntimeException(s"JSON inválido: ${e.getMessage}"))
          }
        
        _ <- IO(println(s"[DEBUG] Datos recibidos: $datos"))
        
        // Validar datos básicos
        _ <- if (datos.id_libro <= 0 || datos.id_persona <= 0 || datos.numero.isEmpty || datos.cvv_numero.isEmpty) {
          IO.raiseError(new RuntimeException("Datos inválidos: todos los campos son requeridos"))
        } else IO.unit

        // Ejecutar compra
        resultado <- LibroQueries.comprar_libro(
          datos.id_libro, datos.id_persona, datos.numero, datos.cvv_numero
        ).handleErrorWith { e =>
          IO(println(s"[ERROR] Error en compra: ${e.getMessage}")) *>
          IO.raiseError(e)
        }
        
        _ <- IO(println(s"[DEBUG] Resultado de compra: $resultado"))

        // Procesar envío de correo solo si la compra fue exitosa
        _ <- if (resultado.startsWith("OK")) {
          IO(println("[DEBUG] Compra exitosa, enviando correo")) *>
          LibroQueries.obtener_correos_pdf(datos.id_persona, datos.id_libro)
            .flatMap {
              case Some(info) =>
                IO(println(s"[DEBUG] Datos para correo: ${info.correo}, ${info.nombrePDF}")) *>
                Enviar_correo.enviar_libro_coreo(info.correo, info.nombrePDF)
                  .handleErrorWith { e =>
                    IO(println(s"[ERROR] Error al enviar correo: ${e.getMessage}")) *>
                    // No fallar toda la operación por error de correo
                    IO.unit
                  }
              case None =>
                IO(println("[WARN] No se pudo obtener datos de correo y PDF"))
            }
        } else {
          IO(println(s"[DEBUG] Compra falló: $resultado"))
        }

        // Preparar respuesta
        res <- if (resultado.startsWith("OK")) {
          Ok(Map("ok" -> resultado, "mensaje" -> "Compra exitosa").asJson)
        } else {
          BadRequest(Map("error" -> resultado).asJson)
        }
        
      } yield res).handleErrorWith { e =>
        // Capturar cualquier error no manejado
        IO(println(s"[ERROR] Error general en endpoint: ${e.getMessage}")) *>
        IO(e.printStackTrace()) *>
        InternalServerError(Map(
          "error" -> "Error interno del servidor",
          "detalle" -> e.getMessage
        ).asJson)
      }
  }
}

object Paginafillter {

  val rutas = HttpRoutes.of[IO] {
    case req @ GET -> Root / "obtener_librosfiltro" =>
      val idcat = req.uri.query.params.get("id_categoria").flatMap(s => scala.util.Try(s.toInt).toOption)
      val preciomin = req.uri.query.params.get("precio_minimo").flatMap(s => scala.util.Try(BigDecimal(s)).toOption)
      val preciomax = req.uri.query.params.get("precio_maximo").flatMap(s => scala.util.Try(BigDecimal(s)).toOption)
      val ordenn  = req.uri.query.params.get("orden").flatMap(s => scala.util.Try(s.toBoolean).toOption)

      (idcat, preciomin, preciomax, ordenn) match {
        case (Some(id_cat), Some(precio_min), Some(precioMax), Some(orden)) =>
          for {
            resultado <- LibroQueries.paginafiltro(id_cat, precioMin, precioMax, orden)
            res <- Ok(resultado.asJson)
          } yield res

        case _ =>
          BadRequest(Map("error" -> "Faltan parámetros válidos: id_categoria, precio_min, precio_max, orden").asJson)
      }
  }
}
```



#### Error stacktrace:

```
dotty.tools.dotc.core.SymDenotations$NoDenotation$.owner(SymDenotations.scala:2609)
	dotty.tools.dotc.core.SymDenotations$SymDenotation.isSelfSym(SymDenotations.scala:715)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:330)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1636)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1638)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1669)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:457)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1677)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:457)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1636)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1638)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1675)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:457)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse$$anonfun$13(ExtractSemanticDB.scala:391)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:334)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:386)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1720)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:354)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse$$anonfun$11(ExtractSemanticDB.scala:377)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:334)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:377)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1728)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1642)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:351)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse$$anonfun$1(ExtractSemanticDB.scala:315)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:334)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:315)
	dotty.tools.pc.SemanticdbTextDocumentProvider.textDocument(SemanticdbTextDocumentProvider.scala:36)
	dotty.tools.pc.ScalaPresentationCompiler.semanticdbTextDocument$$anonfun$1(ScalaPresentationCompiler.scala:242)
```
#### Short summary: 

java.lang.AssertionError: NoDenotation.owner