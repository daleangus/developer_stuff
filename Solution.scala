package codingtest

import scala.io.Source

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import java.io.PrintWriter
import java.io.File
import org.apache.spark.sql.Encoders
import java.sql.Date
import java.util.Calendar
import java.text.SimpleDateFormat
import org.apache.spark.sql.Row

case class DemoRow(
  patient_id: String,
  birth_date: String,
  gender:     String)

case class EventsRow(
  patient_id:  String,
  date:        String,
  icd_version: String,
  icd_code:    String)

case class Event(
  //  patient_id: String,
  date:   String,
  system: String,
  code:   String)

case class JoinedRow(
  patient_id: String,
  birth_date: String,
  gender:     String,
  events:     Event)

case class Json(
  patient_id: String,
  birth_date: String,
  gender:     String,
  events:     List[Event])

/*
 *  Author: Dale Angus (daleangus@hotmail.com)
 *  Have a Scala job for me? 925 642 4780
 *  
 */
object Main {
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  /*
  * Set me to true, to print out the JSON and statistics output
  */
  var writeToFile = false

  val spark: SparkSession =
    SparkSession
      .builder()
      .appName("Company")
      .config("spark.master", "local")
      .getOrCreate()
  val sc = spark.sparkContext

  // For implicit conversions like converting RDDs to DataFrames
  import spark.implicits._

  /** Main function */
  def main(args: Array[String]): Unit = {
    if (args.length > 0 && "writetofile".equals(args(0).toLowerCase()))
      writeToFile = true

    val data: Dataset[JoinedRow] = prepData()
    // println(data.schema.prettyJson)
    // data.toJSON.foreach(f => println(f))

    // When writeToFile=true, the files are written under "output" with the name <patient_id>.json
    manuallyCreateJson(data)

    // When writeToFile=true, the statistics.txt is written under "output"
    stats(data)

    sc.stop()
  }

  /*
   * Prepares the data
   */
  def prepData(): Dataset[JoinedRow] = {

    val demoLines = sc.textFile("src\\main\\resources\\company\\d3mo.psv")
    val eventsLines = sc.textFile("src\\main\\resources\\company\\ev3nts.psv")

    val demoDS: Dataset[DemoRow] = demoLines.mapPartitionsWithIndex((i, it) => if (i == 0) it.drop(1) else it)
      .map(f => f.split('|'))
      .map(s => {
        s match {
          case Array(patient_id, birth_date, gender) => demoRow(s)
          case _                                     => demoRow(Array("", "", ""))
        }
      }).filter(f => f.patient_id != "").toDS().orderBy("patient_id")
    //demoDS.foreach(f => println(f.patient_id))
    //println(demoDS.count())

    val eventsDS: Dataset[EventsRow] = eventsLines.mapPartitionsWithIndex((i, it) => if (i == 0) it.drop(1) else it)
      .map(f => f.split('|'))
      .map(s => {
        s match {
          case Array(patient_id, date, icd_version, icd_code) => eventRow(s)
          case _ => eventRow(Array("", "", "", ""))
        }
      }).filter(f => f.patient_id != "").toDS().orderBy("patient_id")
    //println(eventsDS.count())

    val joined = demoDS.joinWith(eventsDS, demoDS("patient_id") === eventsDS("patient_id"))
      .map(record => {
        JoinedRow(
          record._1.patient_id,
          record._1.birth_date,
          record._1.gender, Event(
            record._2.date,
            if (record._2.icd_version == "10") "http://hl7.org/fhir/sid/icd-10" else "http://hl7.org/fhir/sid/icd-9-cm",
            record._2.icd_code))
      }).orderBy("patient_id").persist()
    //println(joined.count())
    //joined.printSchema
    joined
  }

  /*
   * Helper function
   */
  def demoRow(s: Array[String]): DemoRow = {
    s match {
      case Array(patient_id, birth_date, gender) => DemoRow(patient_id, birth_date, gender)
      case _                                     => DemoRow("", "", "")
    }
  }

  /*
   * Helper function
   */
  def eventRow(s: Array[String]): EventsRow = {
    //println(s.mkString(","))
    s match {
      case Array(patient_id, date, icd_version, icd_code) => EventsRow(patient_id, date, icd_version, icd_code)
      case _ => EventsRow("", "", "", "")
    }
  }

  /*
   *  Encountered a problem with Encoders when attempting to use a Dataset within a Dataset so let's do it by hand!
   *
   *  Generates the JSON of each patient. Saves file under folder output. Filename is <patient_id>.json
   *  Uses Json4s
   */
  def manuallyCreateJson(data: Dataset[JoinedRow]) = {
    var prev_patient_id: String = ""
    var tmpEventList: List[Event] = Nil
    var json: Json = null
    var prev_joinedRow: JoinedRow = null
    for (d <- data) {
      if (prev_joinedRow != null) {

        if (prev_patient_id == d.patient_id) {
          tmpEventList = tmpEventList :+ Event(d.events.date, d.events.system, d.events.code)
        } else {

          //complete previous patient_id
          json = Json(prev_joinedRow.patient_id, prev_joinedRow.birth_date, prev_joinedRow.gender, tmpEventList)
          //render json
          val template = (("patient_id" -> json.patient_id) ~
            ("birth_date" -> json.birth_date) ~
            ("gender" -> json.gender) ~
            ("events" -> json.events.map { w => (("date" -> w.date) ~ ("system" -> w.system) ~ ("code" -> w.code)) }))

          if (writeToFile) {
            val pw: PrintWriter = new PrintWriter(new File(s"output/" + json.patient_id + ".json"))
            pw.write(pretty(render(template)))
            pw.close
          } else { println(compact(render(template))) }

          //start new one
          tmpEventList = Nil :+ Event(d.events.date, d.events.system, d.events.code)
        }
      } else {
        tmpEventList = Nil :+ Event(d.events.date, d.events.system, d.events.code)
      }
      prev_patient_id = d.patient_id
      prev_joinedRow = d
    }
    //not so purrrty but it works!
  }

  /*
   * Calculates some statistics. File saved as output/statistics.txt
   */
  def stats(data: Dataset[JoinedRow]) = {

    var pw: PrintWriter = null
    if (writeToFile) {
      pw = new PrintWriter(new File(s"output/statistics.txt"))
    }

    println("Total number of valid patients: " + data.select("patient_id").distinct().count())
    println("Count of males: " + data.filter(f => f.gender == "M").select("patient_id").distinct().count())
    println("Count of females: " + data.filter(f => f.gender == "F").select("patient_id").distinct().count())
    if (writeToFile) {
      pw.println("Total number of valid patients: " + data.select("patient_id").distinct().count())
      pw.println("Count of males: " + data.filter(f => f.gender == "M").select("patient_id").distinct().count())
      pw.println("Count of females: " + data.filter(f => f.gender == "F").select("patient_id").distinct().count())
    }

    val ageDF: DataFrame = data.groupBy($"patient_id", $"birth_date").agg(Map("events.date" -> "max"))
    //ageDF.printSchema()
    val sdf = new SimpleDateFormat("YYYY-MM-DD")
    val age: Array[Long] = ageDF.collect().map(f => {
      (sdf.parse(f(2).toString()).getTime - sdf.parse(f(1).toString()).getTime) / 1000 / 60 / 60 / 24 / 365
    }).sorted
    println("Maximum age of patient: " + age(age.length - 1) + " years old")
    println("Minimum age of patient: " + age(0) + " years old")
    println("Median age of patient: " + age(age.length / 2) + " years old")
    if (writeToFile) {
      pw.println("Maximum age of patient: " + age(age.length - 1) + " years old")
      pw.println("Minimum age of patient: " + age(0) + " years old")
      pw.println("Median age of patient: " + age(age.length / 2) + " years old")
    }

    val tlmaxDF: DataFrame = data.select($"events", data("patient_id").as("maxpid")).groupBy($"maxpid").agg(Map("events.date" -> "max")).orderBy("maxpid")
    //tlmaxDF.printSchema()
    //tlmaxDF.foreach(f => println(f))
    val tlminDF: DataFrame = data.select($"events", data("patient_id").as("minpid")).groupBy($"minpid").agg(Map("events.date" -> "min")).orderBy("minpid")
    //tlminDF.printSchema()
    //tlminDF.foreach(f => println(f))
    val tljoin: Dataset[(Row, Row)] = tlmaxDF.joinWith(tlminDF, tlmaxDF("maxpid") === tlminDF("minpid"))
    //tljoin.printSchema()
    //tljoin.foreach(f => println(f))
    val tl = tljoin.collect().map(f => {
      val mx: Long = sdf.parse(f._1(1).toString()).getTime()
      val mn: Long = sdf.parse(f._2(1).toString()).getTime()
      (mx - mn) / 1000 / 60 / 60 / 24
    }).sorted
    println("Maximum timeline: " + tl(tl.length - 1) + " days")
    println("Minimum timeline: " + tl(0) + " days")
    println("Median timeline: " + tl(tl.length / 2) + " days")
    if (writeToFile) {
      pw.println("Maximum timeline: " + tl(tl.length - 1) + " days")
      pw.println("Minimum timeline: " + tl(0) + " days")
      pw.println("Median timeline: " + tl(tl.length / 2) + " days")
      pw.close()
    }
  }
}

