package Model

import cats.effect.IO
import DB.ConexionBD
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Future

case class Libro(nombre: String, precio: BigDecimal, id_categoria: Int, nombrepdf: String, nombreimagen: String)
case class Categoria(id_categoria: Int, nombre: String)
case class Reportess(  id_libro: Int,nombre_libro: String,categoria: String,nombre_persona:String, dni: String,correo: String, precio: BigDecimal,total_ventas: BigDecimal)
case class Respuesta_reporte(reporte: Seq[Reportess], total: BigDecimal)
case class Pagina_principal(  id_libro: Int,nombre: String,precio: BigDecimal,id_categoria: Int,nombre_categoria: String,nombrepdf: String, nombreimagen: String)

case class Comprarlibross(
  id_libro: Int,
  id_persona: Int,
  numero: String,
  cvv_numero: String
)



case class Ventaa(correo: String, nombrePDF: String)

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
        val error_mensjae = s"Error : ${e.getMessage}"
        IO {
          println(error_mensjae)
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
  

  //--pagina principal
  def paginap():IO[Seq[Pagina_principal]]=
  {
    val conusltadb = sql"""
    SELECT * FROM pagina_principal()
  """.as[(Int,String,BigDecimal, Int,String,String,String)] 


  val consultaF: Future[ Seq[(Int,String,BigDecimal, Int,String,String,String)]] = ConexionBD.db.run(conusltadb)

  
  val transofr_cat: IO[Seq[(Int,String,BigDecimal, Int,String,String,String)]] = IO.fromFuture(IO(consultaF))

  
  val categoria_io: IO[Seq[Pagina_principal]] = transofr_cat.map { listaTuplas =>
  val lisyas = listaTuplas.map { case ( id_libro,nombre,precio,id_categoria,nombre_categoria,nombrepdf, nombreimagen) =>
        Pagina_principal( id_libro,nombre,precio,id_categoria,nombre_categoria,nombrepdf, nombreimagen)
    }
  lisyas
  }

  
  val return_reportes: IO[Seq[Pagina_principal]] = categoria_io.handleErrorWith { error =>
  val logError: IO[Unit] = IO {
      println(s"Error: ${error.getMessage}")
      error.printStackTrace()
    }

    val vacio: IO[Seq[Pagina_principal]] = IO.pure(Seq.empty)

   
   logError  *> vacio
  }

 
  return_reportes

  }


  
  def comprar_libro(id_libro: Int, id_persona: Int, numero: String, ccv_numero: String): IO[String] = {
    
    
    val query = sql"""
      SELECT comprar_libro($id_libro, $id_persona, $numero, $ccv_numero)
    """.as[String]

    IO.fromFuture(IO(ConexionBD.db.run(query)))
      .map { result =>
        val mensaje = result.headOption.getOrElse("error: Sin respuesta de la función")
       
        mensaje
      }
      .handleErrorWith { e =>
        println(s"error en comprar_libro: ${e.getMessage}")
        e.printStackTrace()
        IO.raiseError(new RuntimeException(s"Error en base de datos: ${e.getMessage}"))
      }
  }

  def obtener_correos_pdf(id_persona: Int, id_libro: Int): IO[Option[Ventaa]] = {
    

    val query = sql"""
      SELECT obtener_correo($id_persona), obtener_nombrepdf($id_libro)
    """.as[(String, String)]

    IO.fromFuture(IO(ConexionBD.db.run(query)))
      .map(_.headOption.map { case (correo, pdf) =>
       
        
        
        if (correo.isEmpty || pdf.isEmpty) {
          None
        } else {
          Some(Ventaa(correo, pdf))
        }
      }.flatten)
      .handleErrorWith { e =>
        println(s" Error al obtener correo y PDF: ${e.getMessage}")
        e.printStackTrace()
        IO.raiseError(new RuntimeException(s"Error al obtener datos de correo: ${e.getMessage}"))
      }
  }
//fintro
  def paginafiltro(id_categoria: Int,precio_min: BigDecimal,precio_max: BigDecimal,orden: Boolean
): IO[Seq[Pagina_principal]] = {
  
  val consultaDB = sql"""
    SELECT * FROM obtener_librosfiltro($id_categoria, $precio_min, $precio_max, $orden)
  """.as[(Int, String, BigDecimal, Int, String, String, String)]

  val consultaF: Future[Seq[(Int, String, BigDecimal, Int, String, String, String)]] = ConexionBD.db.run(consultaDB)

  val transformada: IO[Seq[(Int, String, BigDecimal, Int, String, String, String)]] =IO.fromFuture(IO(consultaF))

  val librosIO: IO[Seq[Pagina_principal]] = transformada.map { listaTuplas =>
    listaTuplas.map {
      case (id_libro, nombre, precio, id_categoria, nombre_categoria, nombrepdf, nombreimagen) =>
        Pagina_principal(id_libro, nombre, precio, id_categoria, nombre_categoria, nombrepdf, nombreimagen)
    }
  }

  val resultadoFinal: IO[Seq[Pagina_principal]] = librosIO.handleErrorWith { error =>
    val logError = IO {
      println(s"errod e filtro: ${error.getMessage}")
      error.printStackTrace()
    }
    logError *> IO.pure(Seq.empty)
  }

  resultadoFinal
}

}

