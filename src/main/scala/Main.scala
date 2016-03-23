package im.xun.setpropex

import java.io.RandomAccessFile

import Utils._
import com.sun.jna.{Native, Memory, Pointer}

object Main extends MemInfoTools {
  import Libc._
  import libc._

  def main(args: Array[String]): Unit = {

    if(args.length != 2) {
      println("useage example: ./setpropex ro.debuggable 1")
      return
    }

    val propname=args(0)
    val propvalue=args(1)
    println(s"set $propname to $propvalue")

    setProp(propname,propvalue)

  }

  def setProp(name: String, value: String) = {

    //use ptrace attach to /init proc, who's pid always be 1
    val initPid = 1
    val res = libc.ptrace(PTRACE_ATTACH, initPid, 0L, 0)
    assert(res != -1, "Fail to attach /init proc!")

    //find system properties's raw data stored in init's memroy
    val mi = search_maps(initPid, "rw-s", "/dev/__properties__").get
    val size = (mi.end - mi.start).toInt
    println(s"Find properties memory maps: \nstart:${mi.start} end:${mi.end} mem size:${size/1024}K")

    //dump system properties's raw data to buffer
    val memFileName = s"/proc/$initPid/mem"
    val mem_buffer = new Array[Byte](size)

    val memFile = new RandomAccessFile(memFileName,"r")
    memFile.seek(mi.start)
    memFile.read(mem_buffer,0,size)
    memFile.close()

    //    memToFile(mem_buffer.slice(128, data.length), "orgin.dump")

    //parse buffer to scala object
    val systemProperties = new SystemProperties(mem_buffer)
    val mem_buffer_modified = systemProperties.propSet(name,value)

    //    memToFile(mem_buffer_modified,"new.dump")

    val PROP_AREA_HEADER_SIZE=128
    val startPtr: Long = mi.start + PROP_AREA_HEADER_SIZE

    //diff data, only writeback changed memory for efficiency
    for( i <- mem_buffer_modified.indices by Native.LONG_SIZE)  {
      val origin= getWord(mem_buffer,PROP_AREA_HEADER_SIZE + i)
      val after = getWord(mem_buffer_modified, i)
      if( after != origin){
//        var pread = ptrace(PTRACE_PEEKDATA, initPid, startPtr + i, 0)
//        println(s"1. ptr: %X old: %X new: %X read: %X".format(startPtr+i, origin,after,pread.longValue()))
        ptrace(PTRACE_POKEDATA, initPid, startPtr + i, after)
//        pread = ptrace(PTRACE_PEEKDATA, initPid, startPtr + i, 0)
//        println(s"2. ptr: %X old: %X new: %X read: %X".format(startPtr+i, origin,after,pread.longValue))
      }
    }

    //take finished, detach /init
    ptrace(PTRACE_DETACH, initPid, 0L ,0)
  }


}
