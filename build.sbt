import sbt.IO
import sbt.Keys._

name := "setpropex"

scalaVersion := "2.11.8"

exportJars := true

test in assembly := {}

libraryDependencies += "net.java.dev.jna" % "jna" % "4.2.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"

val dalvikTask = TaskKey[Unit]("dalvik", "build a command line program for Android")

dalvikTask <<= (assembly, streams) map { (asm, s) =>

  val appName = "setpropex"

  val dexFileName = s"$appName.dex"
  s"dx --dex --output=$dexFileName ${asm.getPath}".!

  val target = new File(appName)
  IO.copyFile(new File("self-running.sh"), target , true)

  val appData="data.tar"
  s"tar -cf $appData run.sh com $dexFileName".!

  IO.append(target, IO.readBytes(new File(appData)))

  s"chmod +x $appName".!

  s.log.info(s"Android cmdline app: $appName build successful!")
  s.log.info("Output: " + new File(".").getCanonicalPath + "/" + appName)

  s"adb push $appName /data/local/tmp".!
  s"adb shell /data/local/tmp/$appName".!

}

