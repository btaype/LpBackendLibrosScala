package Model

import cats.effect.IO
import DB.ConexionBD
import slick.jdbc.PostgresProfile.api._

case class Libro(nombre: String, precio: BigDecimal, idCategoria: Int, nombrePdf: String, nombreImagen: String)
case class Categoria(id_categoria: Int, nombre: String)
object LibroQueries {

  def insertarLibro(nombre: String, precio: BigDecimal, idCategoria: Int, nombrePdf: String, nombreImagen: String): IO[String] = {
    val query = sql"""
      SELECT insertar_libro($nombre, $precio, $idCategoria, $nombrePdf, $nombreImagen)
    """.as[String]

    IO.fromFuture(IO(ConexionBD.db.run(query)))
      .map { result =>
        val mensaje = result.headOption.getOrElse("Libro insertado (sin respuesta explícita)")
        println(s"Resultado de insertar_libro: '$mensaje'")
        println(s"Datos enviados: nombre='$nombre', precio=$precio, id_categoria=$idCategoria, pdf='$nombrePdf', imagen='$nombreImagen'")
        mensaje
      }
      .handleErrorWith { e =>
        val errorMsg = s"Error al ejecutar insertar_libro: ${e.getMessage}"
        IO {
          println(errorMsg)
          println(s"Parámetros que causaron error: nombre='$nombre', precio=$precio, id_categoria=$idCategoria, pdf='$nombrePdf', imagen='$nombreImagen'")
          e.printStackTrace()
        } *> IO.pure(s"ERROR: ${e.getMessage}")
      }
  }
 def obtenerCategorias(): IO[Seq[Categoria]] = {
  val query = sql"""
    SELECT * FROM obtener_categorias()
  """.as[(Int, String)] 
  IO.fromFuture(IO(ConexionBD.db.run(query)))
    .map(_.map { case (id_categoria, nombre) => Categoria(id_categoria, nombre) })
    .handleErrorWith { e =>
      IO {
        println(s"Error al ejecutar obtener_categorias(): ${e.getMessage}")
        e.printStackTrace()
      } *> IO.pure(Seq.empty)
    }
}

}