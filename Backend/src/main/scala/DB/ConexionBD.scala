package DB

import slick.jdbc.PostgresProfile.api._

object ConexionBD {
  lazy val db: Database = Database.forConfig("mi-bd")
}
