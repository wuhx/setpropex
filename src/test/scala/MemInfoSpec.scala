package im.xun.setpropex

import org.scalatest.FlatSpec

class MemInfoSpec extends FlatSpec with MemInfoTools {
  it should "convert line to MemInfo" in {
    val line = "b6edd000-b6efd000 rw-s 00000000 00:0b 1074       /dev/__properties__"
    val res = parseLine(line)
    assert(res.get.name =="/dev/__properties__")
    println("res: "+res)
  }

  ignore should "parse file ok" in {
    val f = scala.io.Source.fromFile("maps")
    //f.getLines.flatMap(parseLine(_)).foreach(println)
    val res = f.getLines.flatMap(parseLine).find( item => item.name == "/dev/__properties__" && item.perm== "rw-s")
    println("res: " + res)

    f.close
  }
}