package utils

import play.api.db.evolutions.Evolutions
import play.api.db.{Database, Databases}
import play.test.WithApplication


class WithFlowUpApplication extends WithApplication {

  var database: Database = null

  override def startPlay(): Unit = {
    super.startPlay()
    initDatabase();
  }

  override def stopPlay(): Unit = {
    cleanDatabase();
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
  }
}
