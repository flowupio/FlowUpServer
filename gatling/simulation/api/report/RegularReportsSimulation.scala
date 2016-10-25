package api.report

import api.report.flowupapi._
import io.gatling.core.Predef._

class RegularReportsSimulation extends Simulation {

  setUp(
    Report.oneUserUsingTheAppTwoTimesPerHourForSomeHours(1).inject(atOnceUsers(1)).protocols(httpConf),
    Report.oneUserUsingTheAppTwoTimesPerHourForSomeHours(2).inject(atOnceUsers(1)).protocols(httpConf),
    Report.oneUserUsingTheAppTwoTimesPerHourForSomeHours(3).inject(atOnceUsers(1)).protocols(httpConf)
  ).assertions(
    global.responseTime.max.lessThan(200),
    global.successfulRequests.percent.is(100)
  )

}
