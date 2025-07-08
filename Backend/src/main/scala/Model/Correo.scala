package Model

import jakarta.mail._
import jakarta.mail.internet._
import java.util.Properties
import java.io.File
import cats.effect.IO


object Enviar_correo {

  def enviar_libro_coreo(destinatario: String, nombrepdf: String): IO[Unit] = IO.blocking {
    try {
      println(s"Enviando correo a: $destinatario con archivo: $nombrepdf")

      
      if (destinatario.isEmpty || nombrepdf.isEmpty) {
        throw new IllegalArgumentException("desstinatario y nombre de archivo son nullos")
      }

      val username = "libro.store.lp@gmail.com"

      val password = "limy yisw syye tjos" 

      val props = new Properties()
      props.put("mail.smtp.auth", "true")

      props.put("mail.smtp.starttls.enable", "true")
      props.put("mail.smtp.host", "smtp.gmail.com")

      props.put("mail.smtp.port", "587")
      props.put("mail.smtp.ssl.trust", "smtp.gmail.com")

      val session = Session.getInstance(props, new Authenticator {
        override def getPasswordAuthentication: PasswordAuthentication =
          new PasswordAuthentication(username, password)
      })

      val mensaje = new MimeMessage(session)
      mensaje.setFrom(new InternetAddress(username))
      mensaje.setRecipient(Message.RecipientType.TO, new InternetAddress(destinatario))
      mensaje.setSubject("Tu libro comprado")

      val cuerpo = new MimeBodyPart()

      cuerpo.setText("Gracias por tu compra. Adjunto encontrarás el libro en formato PDF.")

      val adjunto = new MimeBodyPart()
      val archivo = new File(s"librospdf/$nombrepdf")
      
      
      
      if (!archivo.exists()) {
        throw new RuntimeException(s"no esxiste el pdf: ${archivo.getAbsolutePath}")
      }
      
      adjunto.attachFile(archivo)

      val multipart = new MimeMultipart()
      multipart.addBodyPart(cuerpo)
      multipart.addBodyPart(adjunto)

      mensaje.setContent(multipart)

      Transport.send(mensaje)
     
      
    } catch {
      case e: Exception =>
        println(s" fallo al enviar el correo: ${e.getMessage}")

        e.printStackTrace()
        throw e
    }
  }
}

