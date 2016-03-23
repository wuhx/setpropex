package im.xun.setpropex

//https://android.googlesource.com/platform/bionic/+/master/libc/bionic/system_properties.cpp
/*
 * Properties are stored in a hybrid trie/binary tree structure.
 * Each property's name is delimited at '.' characters, and the tokens are put
 * into a trie structure.  Siblings at each level of the trie are stored in a
 * binary tree.  For instance, "ro.secure"="1" could be stored as follows:
 *
 * +-----+   children    +----+   children    +--------+
 * |     |-------------->| ro |-------------->| secure |
 * +-----+               +----+               +--------+
 *                       /    \                /   |
 *                 left /      \ right   left /    |  prop   +===========+
 *                     v        v            v     +-------->| ro.secure |
 *                  +-----+   +-----+     +-----+            +-----------+
 *                  | net |   | sys |     | com |            |     1     |
 *                  +-----+   +-----+     +-----+            +===========+
 */

case class Node(namelen: Byte, prop: Int, left: Int, right: Int, children: Int, name: String, offset: Int)
case class PropInfo(serial: Int, value: String, name: String, offset: Int)

class SystemProperties(data: Array[Byte]) {

  val PROP_VALUE_MAX = 92
  val PROP_NAME_MAX = 32
  val PROP_AREA_MAGIC = 0x504f5250
  val PROP_AREA_VERSION= 0xfc6ed0ab
  val PROP_AREA_HEADER_SIZE=128

  val magic = intValueAt(8)(data)
  assert(magic == PROP_AREA_MAGIC, s"data not valid! magic:$magic should be $PROP_AREA_MAGIC")

  implicit val mem = data.slice(PROP_AREA_HEADER_SIZE,data.length).clone()

  def propGet(name: String): String = {
    propFor(name).map(_.name).getOrElse("")
  }

  def propSet(name: String, value: String) = {
    println(s"propSet $name $value")

    propFor(name) match {
      case Some(p) =>
        println(s"Find prop: $p")
        var serial = p.serial | 1
        serial = (value.length << 24) | (serial + 1) & 0xffffff
        val prop = p.copy(serial=serial,value=value)
        println(s"Set prop : $prop")
        propWriteToBuffer(prop)
      case None =>
        println(s"property: $name not found!")
    }
    mem
  }

  def propWriteToBuffer(prop: PropInfo) = {
    if(prop.name.length > PROP_NAME_MAX && prop.value.length > PROP_VALUE_MAX ){
      println(s"param length exceed limit! PROP_NAME_MAX: $PROP_NAME_MAX PROP_VALUE_MAX: $PROP_VALUE_MAX")
    } else {

      intValueWriteAt(prop.serial, prop.offset)
      stringValueWriteAt(prop.value, prop.offset + 4)
      //stringValueWriteAt(prop.name, prop.offset +PROP_VALUE_MAX)
    }
  }

  def propAt(offset: Int) = {
    val serial = intValueAt(offset)
    val value = stringValueAt(offset + 4, PROP_VALUE_MAX)
    val name = stringValueAt(offset + PROP_VALUE_MAX, PROP_NAME_MAX)
    PropInfo(serial, value, name, offset)
  }

  def nodeAt(offset: Int) = {
    assert(offset < mem.size)

    val namelen = mem(offset)
    val prop = intValueAt(offset + 4)
    val left = intValueAt(offset + 8)
    val right = intValueAt(offset + 12)
    val children = intValueAt(offset + 16)
    val name = stringValueAt(offset + 20, namelen)
    Node(namelen, prop, left, right, children, name, offset)

  }

  def intValueAt(offset: Int)(implicit mem: Array[Byte]): Int = {
    mem(0 + offset) & 0xFF |
      (mem(1 + offset) & 0xFF) << 8 |
      (mem(2 + offset) & 0xFF) << 16 |
      (mem(3 + offset) & 0xFF) << 24
  }

  def intValueWriteAt(v: Int, offset: Int) = {
    mem(0 + offset) = (v & 0xFF).toByte
    mem(1 + offset) = (v >> 8  & 0xFF).toByte
    mem(2 + offset) = (v >> 16 & 0xFF).toByte
    mem(3 + offset) = (v >> 24 & 0xFF).toByte
  }

  def stringValueWriteAt(v: String, offset: Int) = {
    val ba = v.getBytes("utf8")
    for( i <- 0 until ba.length)  {
      mem(offset + i) = ba(i)
    }
    mem(offset + ba.length) = 0
  }

  def stringValueAt(offset: Int, max: Int) = {
    val strLength = mem.indexOf(0, offset) - offset
    val len = if(strLength > max) max else strLength
    new String(mem, offset, len , "utf8")
  }

  def strcmp(s1: String, s2: String): Int = {
    if(s1.length > s2.length) {
      1
    } else if(s1.length < s2.length) {
      -1
    } else {
      s1.compareTo(s2)
    }
  }

  def nodeFor(name: String, root: Node): Option[Node] = {
    val result = strcmp(name, root.name)
//    println(s"search $name from node: ${root.name} result: $result")

    result match {
      case res if res < 0 && root.left > 0 =>
        val left = nodeAt(root.left)
        nodeFor(name, left)
      case res if res > 0 && root.right > 0 =>
        val right = nodeAt(root.right)
        nodeFor(name, right)
      case res if res == 0 =>
        Some(root)
      case e =>
        println(s"nodeFor $name search failed! compare ${root.name} get:$e R:${root.right} L:${root.left}")
        None
    }
  }

  def propFor(name: String): Option[PropInfo] = {
    val nodeNames = name.split('.')

    var currentNode = nodeAt(0)
    var node: Option[Node] = Some(currentNode)

    for (nodeName <- nodeNames if node.nonEmpty) {
      currentNode = nodeAt(node.get.children)
      node = nodeFor(nodeName, currentNode)
    }

    if (node.nonEmpty && node.get.prop != 0) {
      val pi = propAt(node.get.prop)
      Some(pi)
    } else {
      None
    }

  }

//  def prop_area(ba: Array[Byte]) = {
    //    var index = 0
    //    val PROP_AREA_MAGIC = 0x504f5250
    //    val PROP_AREA_VERSION= 0xfc6ed0ab
    //
    //    val bytes_used = byteArrayToIntLe(ba,0)
    //    val serial = byteArrayToIntLe(ba,4)
    //    val magic = byteArrayToIntLe(ba, 8)
    //    val version = byteArrayToIntLe(ba, 12)
    //    val dataIndex = 16 + 28*4
//  }
}





