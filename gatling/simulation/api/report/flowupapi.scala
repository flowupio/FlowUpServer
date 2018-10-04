package api.report

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object flowupapi extends Simulation {

  val httpConf = http
    .baseURL("https://api.flowupapp.com")
    .acceptHeader("application/json")
    .header("Content-Type", "application/json")
    .header("X-Api-Key", "15207698c544f617e2c11151ada4972e1e7d6e8e")
    .header("X-UUID", "a4972e1e7d6e8e-f617e2c11151ada4-152076")
    .header("Content-Encoding", "gzip")
    .userAgentHeader("FlowUpAndroidSDK/1.0.0")

}
