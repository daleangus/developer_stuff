import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/*
 *
 * The data consists of two columns

A B
A C
A D
B A
B C
B D
B E
C A
C B
C D
C E
D A
D B
D C
D E
E B
E C
E D

In the first row, think of it as A is friends with B, etc. How do I find their common friends?

E.g., (A,B) -> (C D)

 */
object commonfriends {
  val spark: SparkSession =
    SparkSession
      .builder()
      .appName("Time Usage")
      .config("spark.master", "local")
      .getOrCreate()

  /** Main function */
  def main(args: Array[String]): Unit = {
    go()
    spark.stop()
  }

  def go() = {
    val rdd: RDD[String] = spark.sparkContext.textFile("twocols.txt")
    val splitrdd: RDD[(String, String)] = rdd.map { s =>
      var str = s.split(" ")
      new Tuple2(str(0), str(1))
    }

    //DO NOT DO THIS
    //    val group: RDD[(String, Iterable[String])] = splitrdd.groupByKey()
    //    group.foreach(println)

    //    val arr: Array[(String, Iterable[String])] = group.collect()
    //    //arr.foreach(println)
    //    var arr2 = scala.collection.mutable.Set[((String, String), List[String])]()
    //    for (i <- arr)
    //      for (j <- arr)
    //        if (i != j) {
    //          val s1 = i._2.toSet
    //          val s2 = j._2.toSet
    //          val s3 = s1.intersect(s2).toList
    //          //println(s3)
    //          val pair = if (i._1 < j._1) (i._1, j._1) else (j._1, i._1)
    //          arr2 += ((pair, s3))
    //        }
    //
    //    arr2.foreach(println)

    //SWAP ELEMENTS
    val swapped = splitrdd.map(_.swap)
    //SELF JOIN
    val shared = swapped.join(swapped)
    //REMOVE DUPLICATES (A,A), (B,B)s THEN SWAP AGAIN
    val nodups = shared.filter(f => f._2._1 != f._2._2).map(_.swap)
    //FILTER OUT (A,B) and (B,A)s
    val filtered = nodups.filter {
      case ((x, y), _) => x < y
    }
    //GROUP BY KEY
    val group = filtered.groupByKey()
    group.foreach(println)
  }
}

OUTPUT:
((B,E),CompactBuffer(C, D))
((A,E),CompactBuffer(B, C, D))
((D,E),CompactBuffer(B, C))
((A,B),CompactBuffer(C, D))
((A,D),CompactBuffer(B, C))
((B,C),CompactBuffer(A, E, D))
((C,E),CompactBuffer(B, D))
((A,C),CompactBuffer(B, D))
((B,D),CompactBuffer(A, C, E))
((C,D),CompactBuffer(B, A, E))
