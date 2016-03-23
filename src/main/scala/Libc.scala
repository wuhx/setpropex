package im.xun.setpropex

import com.sun.jna._

trait Libc extends Library {
  def open(path:String, flag:Int):Int
  def ioctl(fd:Int, request:Int, args:Array[_]):Int
  def close(fd:Int):Unit

  def chmod(filename: String, mode: Int)
  def chown(filename: String, user: Int, group: String)
  def rename(oldpath: String, newpath: String)
  def kill(pid: Int, signal: Int)
  def link(oldpath: String, newpath: String)
  def mkdir(path: String, mode: Int)
  def rmdir(path: String)

  def ptrace(request: Int, pid: Int, addr: Pointer, data: NativeLong): NativeLong

}

object Libc {

  def isLittleEndian = {
    java.nio.ByteOrder.nativeOrder() == java.nio.ByteOrder.LITTLE_ENDIAN
  }

  //TODO: implicit type conversion for ptrace api, may not safe
  implicit def intToNativeLong(n: Int): NativeLong = new NativeLong(n)
  implicit def longToNativeLong(n: Long): NativeLong = new NativeLong(n)
  implicit def longToNativePointer(n: Long): Pointer = new Pointer(n)
  implicit def nativeLongToLong(n: NativeLong): Long = n.longValue

  lazy val libc = Native.loadLibrary(Platform.C_LIBRARY_NAME, classOf[Libc]).asInstanceOf[Libc]

  val O_ACCMODE    =   3
  val O_RDONLY     =   0
  val O_WRONLY     =   1
  val O_RDWR       =   2
  val O_CREAT      = 100
  val IOCTL_TRIM   = 0x1277

  val PTRACE_PEEKDATA = 2
  val PTRACE_POKEDATA = 5
  val PTRACE_ATTACH = 16
  val PTRACE_DETACH = 17
}
