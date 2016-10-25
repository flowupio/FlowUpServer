package api.report

import api.report.flowupapi._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

class MultiUserRegularReportsSimulation extends Simulation {

  setUp(
    Report.oneUserUsingTheAppIntensivelyDuringLessThanOneHour.inject(rampUsers(20) over 10).protocols(httpConf)
  ).assertions(
    global.responseTime.max.lessThan(200),
    global.successfulRequests.percent.is(100)
  )
}
