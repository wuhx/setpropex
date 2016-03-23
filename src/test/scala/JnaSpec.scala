package im.xun.setpropex

import java.io.{FileOutputStream, FileInputStream, PrintWriter}

import com.sun.jna.Native
import org.scalatest.{BeforeAndAfter, FlatSpec}
import Libc._

class JnaSpec extends FlatSpec {

  it should "attach to debuggee successfully" in {
    import libc._

    val debuggee = TraceMe.run
    println("traceme: "+debuggee)
    val pid = debuggee.pid

    var result = ptrace(PTRACE_ATTACH, pid, 0L, 0)
    assert( -1 != result.intValue)

    val addr = debuggee.addr
    var data = ptrace(PTRACE_PEEKDATA, pid, addr , 0)
    println("1. ptrace read data: 0x%X ".format(data.longValue))
    assert(data === debuggee.data)

    val testData = 0x5555ABCDL
    result = ptrace(PTRACE_POKEDATA, pid, addr, testData)

    data = ptrace(PTRACE_PEEKDATA, pid, addr , 0)
    println("2. ptrace read data: 0x%X ".format(data.longValue))
    assert(data.longValue === testData)

    result = ptrace(PTRACE_DETACH, pid, 0L,0)
    assert(result.intValue != -1)


  }
}