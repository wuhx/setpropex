package im.xun.setpropex

import Libc._
import scala.sys.process._
import java.io.{File,PrintWriter}
import com.sun.jna.{Pointer, NativeLong}

case class DebuggeeInfo(pid: Int, addr: Pointer, data: NativeLong)

object TraceMe {

  val name = "traceme"

  val code =
    """
      |#include <stdio.h>
      |int main() {
      |  long n=0xABCD12345678;
      |  while(1){
      |     printf("data @ %p = %lX \n",&n, n);
      |     usleep(1000);
      |  }
      |}
    """.stripMargin

  def parseDebuggeeOutput() = {
    //data @ 0x7fff59ff78d0 = 4141414141414141
    var lines = s"tail traceme.log".lineStream.headOption
    while(lines.isEmpty){
      //TODO may deadly block here
      lines = s"tail traceme.log".lineStream.headOption
    }
    assert(lines.nonEmpty, s"parseDebuggeeOutput fail! traceme.log empty!")
    val line =  lines.map(_.split(" +")).get
    val addr = java.lang.Long.parseUnsignedLong(line(2).substring(2), 16)
    val data = java.lang.Long.parseUnsignedLong(line(4), 16)
    (addr, data)
  }

  //search pids use grep
  def pidsSearch(name: String): List[String] = {
    ("ps aux" #> s"grep $name" #> "grep -v grep" lineStream_!).map(_.split(" +")(1)).toList
  }

  //USER PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND
  //column COMMAND match name exactly
  def pidsFor(name: String): List[String] = {
    ("ps aux" lineStream_!).map(_.split(" +")).filter(x=> x(10) == name).map(x => x(1)).toList
  }

  def killAll(name: String) = {
    for( pid <- pidsFor(name)) {
      "kill -9 $pid".!!
    }
  }

  def stop = {
    killAll(name)
  }

  def run = {

    val name = "traceme"
    val pr = new PrintWriter(s"traceme.c")
    pr.write(code)
    pr.close()

    s"gcc traceme.c -o traceme".!

    killAll("./traceme")

    s"./traceme" #> new File(s"traceme.log") run

    val (addr, data) = parseDebuggeeOutput()

    val pids = pidsFor("./traceme")
    assert(pids.size == 1, s"non or multi debuggee with same name running! num: ${pids.size}")

    val pid = java.lang.Integer.parseInt(pids.head)

    DebuggeeInfo(pid, addr, data)
  }
}