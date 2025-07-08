error id: file:///F:/LPFinal/Backend/src/main/scala/Model/Correo.scala:`<none>`.
file:///F:/LPFinal/Backend/src/main/scala/Model/Correo.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1187
uri: file:///F:/LPFinal/Backend/src/main/scala/Model/Correo.scala
text:
```scala
import jakarta.mail._
import jakarta.mail.internet._
import java.util.Properties
import java.io.File

object enviar_correo {

def enviarCorreoConPDF(destinatario: String, nombreArchivo: String): IO[Unit] = IO {
  val username = "libro.store.lp@gmail.com"     
  val password = "libro12345"     

  val props = new Properties()
  props.put("mail.smtp.auth", "true")
  props.put("mail.smtp.starttls.enable", "true")
  props.put("mail.smtp.host", "smtp.gmail.com")
  props.put("mail.smtp.port", "587")

  val session = Session.getInstance(props, new Authenticator {
    override def getPasswordAuthentication: PasswordAuthentication =
      new PasswordAuthentication(username, password)
  })

  val mensaje = new MimeMessage(session)
  mensaje.setFrom(new InternetAddress(username))
  mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario))
  mensaje.setSubject("Tu libro comprado")

  // Parte del cuerpo
  val cuerpo = new MimeBodyPart()
  cuerpo.setText("Gracias por tu compra. Adjunto encontrarás el libro en formato PDF.")

  // Parte del adjunto
  val adjunto = new MimeBodyPart()
  adjunto.attachFile(new File(s"ruta/lib@@rospdf/$nombreArchivo")) 

  val multipart = new MimeMultipart()
  multipart.addBodyPart(cuerpo)
  multipart.addBodyPart(adjunto)

  mensaje.setContent(multipart)

  Transport.send(mensaje)
}

}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.