package api.report

import api.report.flowupapi._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import scala.concurrent.duration._

/**
  * Stress simulation based on some users collecting data for some time and sending it to our servers when a WiFi
  * connection is available. The report data sent to our server contains data associated to one user using the app
  * during one hour and opening two different screens during this time.
  */
class MultiUserRegularReportsWithRampedUsersSimulation extends Simulation {

  setUp(
    Report.oneUserUsingTheAppTwoTimesPerHourForSomeHours(1).inject(rampUsers(20) over 10.seconds).protocols(httpConf)
  ).assertions(
    global.responseTime.max.lessThan(200),
    global.successfulRequests.percent.is(100)
  )
}
