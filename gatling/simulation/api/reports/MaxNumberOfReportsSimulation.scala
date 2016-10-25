package api.reports

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
  * Stress simulation based on some users collecting data for some time and sending it to our servers when a WiFi
  * connection is available. Our max request size right now is 10 MB and that means that we can put 251 reports into
  * one single request. As we collect data every 10 seconds that means 251 reports is equivalent to 2510 seconds
  * monitored and this is almost an hour.
  */
class MaxNumberOfReportsSimulation extends Simulation {

  val httpConf = http
    .baseURL("https://api.flowupapp.com")
    .acceptHeader("application/json")
    .header("Content-Type", "application/json")
    .header("X-Api-Key", "15207698c544f617e2c11151ada4972e1e7d6e8e")
    .header("Content-Encoding", "gzip")
    .userAgentHeader("FlowUpAndroidSDK/1.0.0")

  setUp(
    Report.oneUserUsingTheAppDuringLessThanOneHour.inject(atOnceUsers(1)).protocols(httpConf),
    Report.oneUserUsingTheAppDuringMoreThanOneHour.inject(atOnceUsers(1)).protocols(httpConf),
    Report.oneUserUsingTheAppForSomeHours(2).inject(atOnceUsers(10)).protocols(httpConf)
  ).assertions(
    global.responseTime.max.lessThan(200),
    global.successfulRequests.percent.is(100)
  )
}
