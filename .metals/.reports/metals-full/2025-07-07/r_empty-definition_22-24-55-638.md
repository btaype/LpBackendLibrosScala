error id: file:///F:/LPFinal/Backend/src/main/scala/Model/corre.scala:`<none>`.
file:///F:/LPFinal/Backend/src/main/scala/Model/corre.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 749
uri: file:///F:/LPFinal/Backend/src/main/scala/Model/corre.scala
text:
```scala
import jakarta.mail._
import jakarta.mail.internet._
import java.util.Properties
import java.io.File

def enviarCorreoConPDF(destinatario: String, nombreArchivo: String): IO[Unit] = IO {
  val username = "tucorreo@gmail.com"     // Cambiar
  val password = "tupassword_o_token"     // Usar token si es Gmail moderno

  val props = new Properties()
  props.put("mail.smtp.auth", "true")
  props.put("mail.smtp.starttls.enable", "true")
  props.put("mail.smtp.host", "smtp.gmail.com")
  props.put("mail.smtp.port", "587")

  val session = Session.getInstance(props, new Authenticator {
    override def getPasswordAuthentication: PasswordAuthentication =
      new PasswordAuthentication(username, password)
  })

  val mensaje = n@@ew MimeMessage(session)
  mensaje.setFrom(new InternetAddress(username))
  mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario))
  mensaje.setSubject("Tu libro comprado")

  // Parte del cuerpo
  val cuerpo = new MimeBodyPart()
  cuerpo.setText("Gracias por tu compra. Adjunto encontrarás el libro en formato PDF.")

  // Parte del adjunto
  val adjunto = new MimeBodyPart()
  adjunto.attachFile(new File(s"ruta/pdf/$nombreArchivo"))  // <-- asegúrate que la ruta es correcta

  val multipart = new MimeMultipart()
  multipart.addBodyPart(cuerpo)
  multipart.addBodyPart(adjunto)

  mensaje.setContent(multipart)

  Transport.send(mensaje)
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.