package im.xun.setpropex

import scala.collection.Seq
import scala.collection.immutable.TreeMap

class Trie(key: Option[String]) {

  def this() {
    this(None)
  }

  var nodes = new TreeMap[String, Trie]

  var value : Option[String] = None

  def put(k: String, v: String) : Trie= {

    normalise(k) match {
      case Seq() =>
        this.value = Some(v)
        this
      case Seq(h,t @ _*) =>
        val node = this.nodes.get(h) match {
          case None =>
            val n = new Trie(Some(h))
            this.nodes = this.nodes.insert(h, n)
            n
          case Some(n) => n
        }
        node.put(t.mkString("."),v)
    }
  }


  private def normalise(s: String) : Seq[String]= {
    if(s.isEmpty) {
      Seq()
    }else{
      s.split('.')
    }
  }

  def nodeFor(k: String): Option[Trie]=  {
    normalise(k) match {
      case Seq()          => Some(this)
      case Seq(h, t @ _*) => nodes.get(h).flatMap { n => n.nodeFor(t.mkString(".")) }
    }
  }

  def get(k: String): Option[String] = {
    nodeFor(k).flatMap { (n) => n.value }
  }

  override def toString = {
    key.getOrElse("None") ++ ":" ++ value.getOrElse("") ++ "\n" ++ nodes.values.map { n => n.toString.split("\n").map { l => "  " + l }.mkString("\n") }.mkString("\n")
  }

}