package org.geoscript.plumbing

trait Sink { def out: java.io.OutputStream }

object Sink {
  implicit def fromFile(f: java.io.File) =
    new Sink { def out = new java.io.FileOutputStream(f) }

  implicit def fromPath(path: String) = 
    fromFile(new java.io.File(".", path))

  implicit def fromStream(o: java.io.OutputStream) = 
    new Sink { val out = o } 
}
