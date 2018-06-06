import scala.collection.mutable.ArrayBuffer

/*
 * Author: Dale Angus (daleangus@hotmail.com)
 *
 * An imperative programming way of multiplying two 64-digit numbers.
 * Exactly how a grade 4 would do it. :)
 * O(n^2)
 *
 * Did you say Karatsuba? Next time...
 */
object Main extends App {

  def grade4(f: String, s: String): String = {
    
    def col2sum(x: ArrayBuffer[Int], y: ArrayBuffer[Int]): ArrayBuffer[Int] = {
      x.zipAll(y, 0, 0).map(pair => pair._1 + pair._2)
    }

    def colsum(a: ArrayBuffer[ArrayBuffer[Int]]): ArrayBuffer[Int] = {
      a.reduce(col2sum)
    }

    println(f)
    println("times")
    println(s)
    println("equals")

    val vofv: ArrayBuffer[ArrayBuffer[Int]] = ArrayBuffer.fill(s.length, f.length + s.length)(0)
    var colpos = f.length + s.length - 1
    
    for (i <- (s.length - 1) to 0 by -1) {
      val innerVector: ArrayBuffer[Int] = ArrayBuffer.fill(f.length + s.length)(0)
      var innercolpos = 0
      var carry = 0
      for (j <- (f.length - 1) to 0 by -1) {
        val p = f(j).asDigit * s(i).asDigit
        val digit: Int = (p + carry) % 10
        carry = (p + carry) / 10
        innerVector(colpos - innercolpos) = digit
        if (j == 0)
          innerVector(colpos - innercolpos - 1) = (p + carry) / 10
        innercolpos = innercolpos + 1
      }
      vofv(i) = innerVector
      colpos = colpos - 1
    }

    val columnSum: ArrayBuffer[Int] = (colsum(vofv))
    var sumCol = columnSum.length
    var carry = 0
    val rowValues: ArrayBuffer[Int] = ArrayBuffer.fill(columnSum.length + 1)(0)

    for (i <- (columnSum.length - 1) to 0 by -1) {
      val digit = (columnSum(i) + carry) % 10
      carry = (columnSum(i) + carry) / 10
      rowValues(sumCol) = digit
      if (i == 0)
        rowValues(sumCol - 1) = (columnSum(i) + carry) / 10
      sumCol = sumCol - 1
    }
    
    rowValues.foldRight("")(_ + _).dropWhile(p => p == '0')
  }

  val f: String = "3141592653589793238462643383279502884197169399375105820974944593"
  val s: String = "2718281828459045235360287471352662497757247093699959574966967624"
  println(grade4(f, s))
}
