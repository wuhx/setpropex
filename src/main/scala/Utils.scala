package im.xun.setpropex

import java.io.{File, FileOutputStream, BufferedOutputStream}

import com.sun.jna.Native

object Utils {
  def memToFile(mem: Array[Byte], name: String) = {
    val out = new BufferedOutputStream(new FileOutputStream(new File(name)))
    out.write(mem)
    out.flush()
    out.close()
  }

  //get a word for ptrace to process, a word is 32bit when running on 32bit machine, 64bit on 64bit machine
  def getWord(ba: Array[Byte], offset: Int): Long = {
    //c语言中32位机器的long是32位,64位机器的long是64位,而java中的Long始终是64位的,需要特殊处理.
    //TODO: check byte order at runtime using java.nio.ByteOrder.nativeOrder
    // assume little endian here
    var n: Long = 0
    for(i <- 0 until Native.LONG_SIZE) {
      n <<= 8
      n |= (ba(offset + Native.LONG_SIZE - 1 - i) & 0xFF)
    }
    n
  }
}
