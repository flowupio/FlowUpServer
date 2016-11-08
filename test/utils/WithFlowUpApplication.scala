package utils

import org.junit.Before
import play.api.db.evolutions.Evolutions
import play.api.db.{Database, Databases}
import play.test.WithApplication


class WithFlowUpApplication extends WithApplication {

  var database: Database = null

  @Before
  override def startPlay() = {
    super.startPlay()
    initDatabase()
  }

  @Before
  override def stopPlay() = {
    cleanDatabase()
    super.stopPlay()
  }

  private def initDatabase() = {
    database = Databases("com.mysql.jdbc.Driver",
      "jdbc:mysql://localhost/flowupdb",
      "flowupdb",
      Map("user" -> "flowupUser",
        "password" -> "flowupPassword"))
  }

  private def cleanDatabase(): Unit = {
    Evolutions.cleanupEvolutions(database)
    database.shutdown()
  }
}
