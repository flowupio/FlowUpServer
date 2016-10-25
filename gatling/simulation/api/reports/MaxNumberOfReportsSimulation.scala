package api.reports

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

import com.fasterxml.jackson.databind.ObjectMapper
import controllers.ReportRequest
import controllers.ReportRequest._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import usecases.StatisticalValue

import scala.collection.JavaConversions._

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
    Report.oneReportFull251ReportsCollapsed.inject(atOnceUsers(1)).protocols(httpConf),
    Report.oneUserUsingTheAppDuringMoreThanOneHour.inject(atOnceUsers(1)).protocols(httpConf)
  ).assertions(
    //global.responseTime.max.lessThan(200),//TODO: Enable this to see our tests failing.
    global.successfulRequests.percent.is(100)
  )
}

object Report {

  private val anyVersionName = "1.0.0"
  private val anyOSVersion = "API24"
  private val anyBatterySaverValue = true
  private val anyScreen = "MainActivity"
  private val maxConsumption = 100
  private val appPackage = "io.flowup.example.garlicstresstest"
  private val uuid = "1e54751e.28be.404a.88c0.5004140323d8"
  private val deviceModel = "Samsung Galaxy S3"
  private val screenDensity = "xxxhdpi"
  private val screenSize = "1080X1794"
  private val numberOfCores = 6
  private val anyTimestamp = System.currentTimeMillis()
  private val anyAmountOfBytes = 1024
  private val anyStatisticalValue = new StatisticalValue(
    1000,
    1000,
    1000,
    1000,
    1000,
    1000,
    1000,
    1000,
    1000,
    1000,
    1000,
    1000,
    1000,
    1000)

  val oneUserUsingTheAppDuringMoreThanOneHour = scenario("One user using the app during more than one hour").repeat(2) {
    exec(http("One user using the app during more than one hour")
      .post("/report")
      .body(ByteArrayBody(generateGzippedMaxReportRequest()))
      .check(status.is(201)))
  }

  val oneReportFull251ReportsCollapsed = scenario("One report full of data")
      .exec(http("One report full of data")
      .post("/report")
      .body(ByteArrayBody(generateGzippedMaxReportRequest()))
      .check(status.is(201)))

  /**
    * Generates an array of bytes containing the gzipped json associated to a user using the app during 2510 second
    * (251 reports = 10 mb of raw json) in the worst scenario possible. 251 reports collapsed into a single request
    * with a user moving around 10 different screens in 10 seconds.
    */
  private def generateGzippedMaxReportRequest() = {
    val reportRequest = generateMaxSizeReport()
    val json = toJson(reportRequest)
    toGzip(json)
  }

  private def generateMaxSizeReport(): ReportRequest = {
    val maxNumberOfReportsPerRequest = 251
    new ReportRequest(
      appPackage,
      deviceModel,
      screenDensity,
      screenSize,
      uuid,
      numberOfCores,
      generateNetworkMetrics(maxNumberOfReportsPerRequest),
      generateUIMetrics(maxNumberOfReportsPerRequest),
      generateCPUMetrics(maxNumberOfReportsPerRequest),
      List(),
      generateMemoryMetrics(maxNumberOfReportsPerRequest),
      generateDiskMetrics(maxNumberOfReportsPerRequest)
    )
  }

  private def generateNetworkMetrics(numberOfMetrics: Int): List[Network] = {
    (0 until numberOfMetrics).toList.map { _ =>
      new Network(anyTimestamp, anyVersionName, anyOSVersion, anyBatterySaverValue, anyAmountOfBytes, anyAmountOfBytes)
    }
  }

  /**
    * This method generates 10 times more metrics than the rest of the users because for every screen opened by the app
    * user opening the screen will generate a new item in the Ui metrics array. For this test we can asume that a user
    * can open and close 10 different screens.
    */
  private def generateUIMetrics(numberOfMetrics: Int): List[Ui] = {
    (0 until numberOfMetrics * 10).toList.map { _ =>
      new Ui(anyTimestamp,
        anyVersionName,
        anyOSVersion,
        anyBatterySaverValue,
        anyScreen,
        anyStatisticalValue,
        anyStatisticalValue,
        anyStatisticalValue,
        anyStatisticalValue,
        anyStatisticalValue,
        anyStatisticalValue,
        anyStatisticalValue,
        anyStatisticalValue,
        anyStatisticalValue)
    }
  }

  private def generateCPUMetrics(numberOfMetrics: Int): List[Cpu] = {
    (0 until numberOfMetrics).toList.map { _ =>
      new Cpu(anyTimestamp,
        anyVersionName,
        anyOSVersion,
        anyBatterySaverValue,
        maxConsumption)
    }
  }

  private def generateMemoryMetrics(numberOfMetrics: Int): List[Memory] = {
    (0 until numberOfMetrics).toList.map { _ =>
      new Memory(anyTimestamp,
        anyVersionName,
        anyOSVersion,
        anyBatterySaverValue,
        maxConsumption,
        anyAmountOfBytes)
    }
  }

  private def generateDiskMetrics(numberOfMetrics: Int): List[Disk] = {
    (0 until numberOfMetrics).toList.map { _ =>
      new Disk(anyTimestamp,
        anyVersionName,
        anyOSVersion,
        anyBatterySaverValue,
        maxConsumption,
        anyAmountOfBytes)
    }
  }

  private def toJson(reportRequest: ReportRequest): String = {
    val mapper = new ObjectMapper()
    mapper.writeValueAsString(reportRequest)
  }

  private def toGzip(str: String): Array[Byte] = {
    if ((str == null) || (str.length == 0)) {
      return null
    }
    val obj: ByteArrayOutputStream = new ByteArrayOutputStream
    val gzip: GZIPOutputStream = new GZIPOutputStream(obj)
    gzip.write(str.getBytes("UTF-8"))
    gzip.close()
    obj.toByteArray
  }

}