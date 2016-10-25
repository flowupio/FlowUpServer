package api.report

import io.gatling.core.Predef._
import api.report.flowupapi._

/**
  * Stress simulation based on some users collecting data for some time and sending it to our servers when a WiFi
  * connection is available. Our max request size right now is 10 MB and that means that we can put 251 reports into
  * one single request. As we collect data every 10 seconds that means 251 reports is equivalent to 2510 seconds
  * monitored and this is almost an hour.
  */
class MaxNumberOfReportsSimulation extends Simulation {

  setUp(
    Report.oneUserUsingTheAppIntensivelyDuringLessThanOneHour.inject(atOnceUsers(1)).protocols(httpConf),
    Report.oneUserUsingTheAppIntensivelyDuringMoreThanOneHour.inject(atOnceUsers(1)).protocols(httpConf),
    Report.oneUserUsingTheAppIntensivelyForSomeHours(3).inject(atOnceUsers(10)).protocols(httpConf).protocols(httpConf)
  ).assertions(
    global.responseTime.max.lessThan(200),
    global.successfulRequests.percent.is(100)
  )
}