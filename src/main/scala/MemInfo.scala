package im.xun.setpropex

import java.io.{FileInputStream, Closeable}

import com.sun.jna.Pointer

import scala.util.{Success, Try}
import scala.util.control.Exception._

case class MemInfo(start: Long, end: Long, perm: String, name: String)
trait MemInfoTools {

  //b6edd000-b6efd000 rw-s 00000000 00:0b 1074       /dev/__properties__
  def parseLine(line: String): Option[MemInfo] = {
    val parsed = line.split(" +")
    Try {
      val addr_range= parsed(0).split("-")
      val start = java.lang.Long.parseLong(addr_range(0),16)
      val end = java.lang.Long.parseLong(addr_range(1),16)
      val perm = parsed(1)
      val name =  catching(classOf[ArrayIndexOutOfBoundsException]) opt parsed(5)
      MemInfo(start, end,perm, name.getOrElse("") )
    } match {
      case Success(mi) =>
        Some(mi)
      case fail =>
        println("parseLine fail: "+fail)
        None
    }
  }

  def search_maps(pid: Int, perm: String, name: String): Option[MemInfo] = {
    val procFile = s"/proc/$pid/maps"
    val f = scala.io.Source.fromFile(procFile)
//    val mi = f.getLines.flatMap(parseLine).find(_.name == "/dev/__properties__")
    val mi = f.getLines.flatMap(parseLine).find( item =>
        item.name == name && item.perm== perm)
    f.close()
    mi
  }

}