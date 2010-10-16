package org.geoscript.example 

import org.geoscript._
import style_new._
import filter.Expression.enrichString

object Render {
  def main(args: Array[String]) {
    val states = layer.Shapefile("geoscript/src/test/resources/data/states.shp")
    val theme =
      Label("STATE_ABBR".cql, geometry="the_geom".cql) and
      (Fill("#4DFF4D", opacity=0.7) where "PERSONS <  2e6") and
      (Fill("#FF4D4D", opacity=0.7) where "PERSONS >= 2e6 AND PERSONS <4e6") and
      (Fill("#4D4DFF", opacity=0.7) where "PERSONS >= 4e6") and
      Stroke("black", width="PERSONS/10000000".cql)
    val viewport = render.Viewport(states.bounds)
    viewport.render(Seq(states -> theme)).writeTo(render.PNG("states.png"))
  }
}
