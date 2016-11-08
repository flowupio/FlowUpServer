package utils

import org.junit.{After, Before}
import play.api.db.evolutions.Evolutions
import play.api.db.Databases
import play.test.WithApplication


class WithFlowUpApplication extends WithApplication {

  @After
  override def stopPlay() = {
    cleanDatabase()
    super.stopPlay()
  }

  private def cleanDatabase(): Unit = {
    val database = Databases("com.mysql.jdbc.Driver",
      "jdbc:mysql://localhost/flowupdb",
      "flowupdb",
      Map("user" -> "flowupUser",
        "password" -> "flowupPassword"))
    Evolutions.cleanupEvolutions(database)
  }
}
