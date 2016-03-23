package im.xun.setpropex

import java.io.{BufferedInputStream, FileInputStream}

import org.scalatest.{Matchers, FlatSpec}

class SystemPropertiesSpec extends FlatSpec with Matchers {
  ignore should "works" in {

    val config =
      """
        |ro.dalvik.vm.native.bridge=1
        |ro.debuggable=0
        |ro.crypto.state=unencrypted
        |ro.product.cpu.abilist=armeabi-v7a,armeabi
        |selinux.reload_policy=1
        |sys.boot_completed=1
        |sys.sysctl.tcp_def_init_rwnd=60
        |service.bootanim.exit=0
        |wlan.driver.status=ok
      """.stripMargin
    val props = config.split("\n").map(_.split("=")).filter(_.size==2).map{ case Array(name,value) => (name,value)}
    assert(props.length > 8)

    val tm = new Trie(None)

//    props.map((tm.put _).tupled)
    props.map { p =>
      tm.put(p._1, p._2)
    }

    props.foreach { p =>
      assert(tm.get(p._1).get == p._2)
    }

//    tm.put("ro.dalvik.vm.native.bridge","1")
//    tm.put("ro.debuggable","0")
//    tm.put("ro.crypto.state","unencrypted")
//    tm.put("ro.product.cpu.abilist","armeabi-v7a,armeabi")
//    tm.put("selinux.reload_policy","1")
//    tm.put("sys.boot_completed","1")
//    tm.put("sys.sysctl.tcp_def_init_rwnd","60")
//    tm.put("service.bootanim.exit","0")
//    tm.put("wlan.driver.status","ok")

//    println(tm)
  }

  it should "build SystemPropertie from byte array" in {
    //val testData = "prop.data"
    val testData = "test.log"
    val bis = new BufferedInputStream(new FileInputStream(testData))
    val ba = Stream.continually(bis.read).takeWhile(_ != -1).map(_.toByte).toArray
    bis.close()

    val systemProperties = new SystemProperties(ba)

    var result = systemProperties.propFor("ro.product.cpu.abilist")
    print("prop value: " + result.get.value)
    assert(result.get.value === "armeabi-v7a,armeabi")

    systemProperties.propSet("ro.product.cpu.abilist","X86")
    result = systemProperties.propFor("ro.product.cpu.abilist")
    assert(result.get.value === "X86")

  }

}