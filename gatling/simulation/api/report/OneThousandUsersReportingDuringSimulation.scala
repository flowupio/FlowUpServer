package api.report

import api.report.flowupapi._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import scala.concurrent.duration._

/**
  * Stress simulation based on some users collecting data for some time and sending it to our servers when a WiFi
  * connection is available. The report data sent to our server contains data associated to 1000 user using the app
  * during some hours and opening two different screens during this time.
  */
class OneThousandUsersReportingDuringSimulation extends Simulation {
  setUp(
    Report.oneUserUsingTheAppSomeTimesPerHourForSomeHours(2, 8).inject(rampUsers(1000) over 3.hours).protocols(httpConf)
  ).assertions(
    global.responseTime.max.lessThan(200),
    global.successfulRequests.percent.is(100)
  )

}
