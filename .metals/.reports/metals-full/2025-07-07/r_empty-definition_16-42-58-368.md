error id: file:///F:/LPFinal/Backend/src/main/scala/Model/ModelLibro.scala:`<none>`.
file:///F:/LPFinal/Backend/src/main/scala/Model/ModelLibro.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -slick/jdbc/PostgresProfile.api.String#
	 -String#
	 -scala/Predef.String#
offset: 607
uri: file:///F:/LPFinal/Backend/src/main/scala/Model/ModelLibro.scala
text:
```scala
package Model

import cats.effect.IO
import DB.ConexionBD
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Future

case class Libro(nombre: String, precio: BigDecimal, id_categoria: Int, nombrepdf: String, nombreimagen: String)
case class Categoria(id_categoria: Int, nombre: String)
case class Reportess(  id_libro: Int,nombre_libro: String,categoria: String,nombre_persona:String, dni: String,correo: String, precio: BigDecimal,total_ventas: BigDecimal)
case class Respuesta_reporte(reporte: Seq[Reportess], total: BigDecimal)
case class Pagina_principal(  id_libro: Int,nombre: @@String,categoria: String,nombre_persona:String, dni: String,correo: String,total_ventas: BigDecimal)
object LibroQueries {

  def insert_libro(nombre: String, precio: BigDecimal, id_categoria: Int, nombrepdf: String, nombreimagen: String): IO[Boolean] = {
    val query = sql"""
      SELECT insertar_libro($nombre, $precio, $id_categoria, $nombrepdf, $nombreimagen)
    """.as[String]

    val resultadF: Future[Seq[String]]= ConexionBD.db.run(query)
    val conv_result: IO[Seq[String]]= IO.fromFuture(IO(resultadF))
    val msj:IO[Boolean]=conv_result.map { result =>
      
        println(s"Datos enviados: nombre='$nombre', precio=$precio, id_categoria=$id_categoria, pdf='$nombrepdf', imagen='$nombreimagen'")
        true
      }
      .handleErrorWith { e =>
        val errorMsg = s"Error : ${e.getMessage}"
        IO {
          println(errorMsg)
          println(s"Parametros='$nombre', precio=$precio, id_categoria=$id_categoria, pdf='$nombrepdf', imagen='$nombreimagen'")
          e.printStackTrace()
        } *> IO.pure(false)
      }
    msj
  }
def obtener_categorias(): IO[Seq[Categoria]] = {
 
  val conusltadb = sql"""
    SELECT * FROM obtener_categorias()
  """.as[(Int, String)] 


  val consultaF: Future[ Seq[(Int,String)]] = ConexionBD.db.run(conusltadb)

  
  val transofr_cat: IO[Seq[(Int,String)]] = IO.fromFuture(IO(consultaF))

  
  val categoria_io: IO[Seq[Categoria]] = transofr_cat.map { listaTuplas =>
  val lisyas = listaTuplas.map { case (id, nombre) =>
        Categoria(id, nombre)
    }
  lisyas
  }

  
  val return_categorias: IO[Seq[Categoria]] = categoria_io.handleErrorWith { error =>
  val logError: IO[Unit] = IO {
      println(s"Error: ${error.getMessage}")
      error.printStackTrace()
    }

    val vacio: IO[Seq[Categoria]] = IO.pure(Seq.empty)

   
   logError  *> vacio
  }

 
  return_categorias
}

def delete_libro(id_libro: Int): IO[Boolean] = {
    val query = sql"""
      SELECT eliminar_libro($id_libro)
    """.as[Boolean]

    val resultadF: Future[Seq[Boolean]]= ConexionBD.db.run(query)
    val conv_result: IO[Seq[Boolean]]= IO.fromFuture(IO(resultadF))

    val returnbool : IO[Boolean]=conv_result.map{result=>
      val eliminado=result.headOption.getOrElse(false)
      eliminado
    }handleErrorWith { e =>
    
    println(s"Error: $id_libro: ${e.getMessage}")

    e.printStackTrace()
    IO.pure(false)
  }
  returnbool
  }

//-----reprtes
  def reportes(mes:Int,anio:Int):IO[Seq[Reportess]]=
  {
    val conusltadb = sql"""
    SELECT * FROM reportedevntas($mes,$anio)
  """.as[( Int, String, String, String,String,String, BigDecimal,BigDecimal)] 


  val consultaF: Future[ Seq[( Int, String, String, String,String,String, BigDecimal,BigDecimal)]] = ConexionBD.db.run(conusltadb)

  
  val transofr_cat: IO[Seq[( Int, String, String, String,String,String, BigDecimal,BigDecimal)]] = IO.fromFuture(IO(consultaF))

  
  val categoria_io: IO[Seq[Reportess]] = transofr_cat.map { listaTuplas =>
  val lisyas = listaTuplas.map { case ( id_libro,nombre_libro,categoria,nombre_persona, dni,correo, precio,total_ventas) =>
        Reportess( id_libro,nombre_libro,categoria,nombre_persona, dni,correo, precio,total_ventas)
    }
  lisyas
  }

  
  val return_reportes: IO[Seq[Reportess]] = categoria_io.handleErrorWith { error =>
  val logError: IO[Unit] = IO {
      println(s"Error: ${error.getMessage}")
      error.printStackTrace()
    }

    val vacio: IO[Seq[Reportess]] = IO.pure(Seq.empty)

   
   logError  *> vacio
  }

 
  return_reportes

  }

}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.