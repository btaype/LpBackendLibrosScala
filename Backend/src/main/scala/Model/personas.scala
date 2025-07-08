package Model

import org.mindrot.jbcrypt.BCrypt
import cats.effect.IO
import DB.ConexionBD
import slick.jdbc.PostgresProfile.api._

case class Persona(nombre: String, contrasena: String, correo: String, dni: String) {
  override def toString: String = s"Persona(nombre=$nombre, correo=$correo, dni=$dni)"
}
case class PersonaRes(id_persona: Int, nombre: String, rol: String, correo: String, dni: String)
case class LoginRequest(correo: String, contrasena: String)


object PersonaQueries {

  
  def hashPassword(password: String): String = {
    BCrypt.hashpw(password, BCrypt.gensalt()) 
  }


 def inserta_personas(nombre: String, rol: String, contrasena: String,  correo: String, dni: String): IO[String] = {
  val query = sql"""
    SELECT insertar_persona($nombre, $rol,  $contrasena, $correo, $dni)
  """.as[String]

  IO.fromFuture(IO(ConexionBD.db.run(query)))

    .map { result =>

      val mensaje = result.headOption.getOrElse("error: Sin respuesta de la funcin")
      println(s"Resultado desde BD: $mensaje")
      mensaje
    }
    .handleErrorWith { e =>

      val errorMsg = s"error:${e.getMessage}"
      println(errorMsg)

      e.printStackTrace()
      IO.pure(errorMsg)
    }
}
  def login_ersona(correo: String, contrasenaIngresada: String): IO[Either[String, PersonaRes]] = {
  val query = sql"""
    SELECT * FROM buscar_persona_por_correo($correo)
  """.as[(Int, String, String, String, String, String)] 

  IO.fromFuture(IO(ConexionBD.db.run(query)))
    .map {
      case Seq((id_persona, nombre, rol, contrasenaHash, correoDb, dni)) =>
        if (BCrypt.checkpw(contrasenaIngresada, contrasenaHash)) {
          Right(PersonaRes(id_persona, nombre, rol, correoDb, dni)) 
        } else {
          Left("Contraseña incorrecta")
        }
      case Nil =>
        Left("Usuario no encontrado")
      case _ =>
        Left("Error inesperado")
    }
    .handleErrorWith { e =>
      IO {
        println(s"Error durante login: ${e.getMessage}")
        e.printStackTrace()
      } *> IO.pure(Left("Error en la base de datos"))
    }
}

}

