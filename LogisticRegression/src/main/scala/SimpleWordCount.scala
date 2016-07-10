package src.main.scala

import java.util.Random
import java.util.concurrent.TimeUnit

import breeze.linalg.{Vector, DenseVector, squaredDistance}
import org.apache.hadoop.conf.Configuration
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.scheduler.InputFormatInfo
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkContext, SparkConf}
import org.json4s.jackson.JsonMethods._

import scala.math._

/**
 * Created by hwang on 2016/6/19.
 */
object SimpleLR {

  val D = 10   // Numer of dimensions
  val rand = new Random(42)

  case class DataPoint(x: Vector[Double], y: Double)

  def parsePoint(line: String): DataPoint = {
    val tok = new java.util.StringTokenizer(line, " ")
    var y = tok.nextToken.toDouble
    var x = new Array[Double](D)
    var i = 0
    while (i < D) {
      x(i) = tok.nextToken.toDouble; i += 1
    }
    DataPoint(new DenseVector(x), y)
  }

  def main(args: Array[String]) {
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN);
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF);
    if (args.length < 4) {
      println("usage: <input> <output> <numClusters> <maxIterations> <StorageLevel>")
      System.exit(0)
    }

    val input = args(0)
    val output = args(1)
    //val K = args(2).toInt
    val maxIterations = args(2).toInt
    val storageLevel=args(3)
    //val runs = calculateRuns(args)

    var sl:StorageLevel=StorageLevel.MEMORY_ONLY;
    if(storageLevel=="MEMORY_AND_DISK_SER")
      sl=StorageLevel.MEMORY_AND_DISK_SER
    else if(storageLevel=="MEMORY_AND_DISK")
      sl=StorageLevel.MEMORY_AND_DISK
    else if(storageLevel=="OFF_HEAP")
      sl=StorageLevel.OFF_HEAP
    else if(storageLevel=="NONE")
      sl=StorageLevel.NONE


    val sparkConf = new SparkConf().setAppName("Spark Simple LR with storageLevel"+storageLevel)
    val conf = new Configuration()
    val sc = new SparkContext(sparkConf,
      InputFormatInfo.computePreferredLocations(
        Seq(new InputFormatInfo(conf, classOf[org.apache.hadoop.mapred.TextInputFormat], input))
      ))
    val lines = sc.textFile(input, maxIterations)
    val points = if (storageLevel != "NONE") {
      lines.map(parsePoint _).persist(sl)
    } else {
      lines.map(parsePoint _)
    }

    // Initialize w to a random value
    var w = DenseVector.fill(D){2 * rand.nextDouble - 1}
    println("Initial w: " + w)

    for (i <- 1 to maxIterations) {
      println("On iteration " + i)
      val gradient = points.map { p =>
        p.x * (1 / (1 + exp(-p.y * (w.dot(p.x)))) - 1) * p.y
      }.reduce(_ + _)
      w -= gradient
    }

    println("Final w: " + w)
    sc.stop()
  }

}
