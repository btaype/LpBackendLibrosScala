error id: file:///F:/LPFinal/Backend/src/main/scala/Model/personas.scala:`<none>`.
file:///F:/LPFinal/Backend/src/main/scala/Model/personas.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 630
uri: file:///F:/LPFinal/Backend/src/main/scala/Model/personas.scala
text:
```scala
package Model

import org.mindrot.jbcrypt.BCrypt
import cats.effect.IO
import DB.ConexionBD
import slick.jdbc.PostgresProfile.api._

case class Persona(nombre: String, contrasena: String, correo: String, dni: String) {
  override def toString: String = s"Persona(nombre=$nombre, rol=$rol, correo=$correo, dni=$dni)"
}
case class PersonaRes(id_persona: Int, nombre: String, rol: String, correo: String, dni: String)
case class LoginRequest(correo: String, contrasena: String)


object PersonaQueries {

  
  def hashPassword(password: String): String = {
    BCrypt.hashpw(password, BCrypt.gensalt()) 
  }


 @@def insertarPersona(nombre: String, contrasena: String, correo: String, dni: String): IO[Boolean] = {
    val query = sql"""
      SELECT insertar_persona($nombre, $rol, $contrasena, $correo, $dni)
    """.as[String]
    
    IO.fromFuture(IO(ConexionBD.db.run(query)))
      .map(result => {
        val mensaje = result.headOption.getOrElse("Sin respuesta de la función")
        println(s"Resultado de insertar_persona: '$mensaje'")
        println(s"Parámetros enviados: nombre='$nombre', rol='$rol', correo='$correo', dni='$dni'")
        mensaje
      })
      .handleErrorWith { e =>
        val errorMsg = s"Error al ejecutar insertar_persona: ${e.getMessage}"
        IO {
          println(errorMsg)
          println(s"Parámetros que causaron error: nombre='$nombre', rol='$rol', correo='$correo', dni='$dni'")
          e.printStackTrace()
        } *> IO.pure(s"error: ${e.getMessage}")
      }
  }
  def loginPersona(correo: String, contrasenaIngresada: String): IO[Either[String, PersonaRes]] = {
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
        Left("Error inesperado: múltiples usuarios encontrados")
    }
    .handleErrorWith { e =>
      IO {
        println(s"Error durante login: ${e.getMessage}")
        e.printStackTrace()
      } *> IO.pure(Left("Error en la base de datos"))
    }
}

}


```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.