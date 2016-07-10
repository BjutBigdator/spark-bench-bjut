/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package src.main.scala

import scala.math.random

import org.apache.spark._

/** Computes an approximation to pi */
object SimplePI {
  def main(args: Array[String]): Unit = {
    //mainOrg(args)
    //main2LocalTest(args)
    main2(args)
  }

  def mainOrg(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Spark Pi")
    val spark = new SparkContext(conf)
    val slices = if (args.length > 0) args(0).toInt else 2
    val pointPerTask = if (args.length > 1) args(1).toLong else 100000L
    val N = (slices * pointPerTask)
    //val n = math.min(100000L * slices, Int.MaxValue).toInt // avoid overflow
    val count = spark.parallelize(1L until N, slices).map { i =>
      val x = random * 2 - 1
      val y = random * 2 - 1
      if (x*x + y*y < 1) 1L else 0L
    }.reduce(_ + _)
    val res = 4.0 * count / N
    println("Pi is roughly " + 4.0 * count / N)
    spark.stop()
  }

  def main2(args: Array[String]): Unit = {
    //spark读取文件调用的都是hadoop 文件接口，所以本地需要hadoop的一个接口库
    //System.setProperty("hadoop.home.dir", "d:\\dev-home\\winutils\\")
    //local模式下，任务以线程形式进行执行，需要setMaster("local[4]")，4指的是4个核数
    //LOCAL模式下，worker，executor等概念都不存在
    val conf = new SparkConf().setAppName("Spark Pi")
    val spark = new SparkContext(conf)
    val slices = if (args.length > 0) args(0).toInt else 2
    val pointPerTask = if (args.length > 1) args(1).toLong else 1000000000L
    //val N = (slices * pointPerTask)
    //val n = math.min(100000L * slices, Int.MaxValue).toInt // avoid overflow
    val arr = new Array[Long](slices)
    val rdd1 = spark.parallelize(arr.map(item => pointPerTask), slices).mapPartitions { iter =>

      val start = System.currentTimeMillis()
      var cnt = 0L

      val upper = if (iter.hasNext) iter.next() else 0L
      println(upper)
      for (i <- 0L to upper) {
        val x = random * 2 - 1
        val y = random * 2 - 1
        if (x*x + y*y < 1) cnt += 1
      }

      val end = System.currentTimeMillis()
      println("task execution time: " + (end - start)/1000)

      Iterator(cnt)

      //Array[Long](cnt).iterator
      //iter.map(upp => cnt)
    }

    //val arr2 = rdd1.collect()

    val count = rdd1.reduce(_ + _)

    val res = 4.0 * count / slices / pointPerTask

    println("Pi is roughly " + res)

    spark.stop()
  }

  def main2LocalTest(args: Array[String]): Unit = {
    //spark读取文件调用的都是hadoop 文件接口，所以需要hadoop的一个接口库
    System.setProperty("hadoop.home.dir", "d:\\dev-home\\winutils\\")
    //local模式下，任务以线程形式进行执行，需要setMaster("local[4]")，4指的是4个核数
    //LOCAL模式下，worker，executor等概念都不存在
    val conf = new SparkConf().setAppName("Spark Pi").setMaster("local[4]")
    val spark = new SparkContext(conf)
    val slices = if (args.length > 0) args(0).toInt else 2
    val pointPerTask = if (args.length > 1) args(1).toLong else 1000000000L
    //val N = (slices * pointPerTask)
    //val n = math.min(100000L * slices, Int.MaxValue).toInt // avoid overflow
    val arr = new Array[Long](slices)
    val rdd1 = spark.parallelize(arr.map(item => pointPerTask), slices).mapPartitions { iter =>

      val start = System.currentTimeMillis()
      var cnt = 0L

      val upper = if (iter.hasNext) iter.next() else 0L
      println(upper)
      for (i <- 0L to upper) {
        val x = random * 2 - 1
        val y = random * 2 - 1
        if (x*x + y*y < 1) cnt += 1
      }

      val end = System.currentTimeMillis()
      println("task execution time: " + (end - start)/1000)

      Iterator(cnt)

      //Array[Long](cnt).iterator
      //iter.map(upp => cnt)
    }

    //val arr2 = rdd1.collect()

    val count = rdd1.reduce(_ + _)

    val res = 4.0 * count / slices / pointPerTask

    println("Pi is roughly " + res)

    spark.stop()
  }
}
