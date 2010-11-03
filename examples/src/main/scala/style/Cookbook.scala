import org.geoscript.style_new._
import org.geoscript.filter.Expression.enrichString

object Cookbook {
  object Lines {
    val simpleLine = Stroke("#000000", width=3)

    val lineWithBorder = 
      Stroke("#333333", width=5, linecap="round", zIndex=1) and
      Stroke("#6699FF", width=3, linecap="round", zIndex=2)

    val dashedLine = Stroke("#0000FF", width=3, dasharray=Array(5, 2))

    val railroad =
      Stroke("#333333", width=3) and
      Stroke(Symbol("shape://vertline", size=12, stroke=Stroke("#333333")))

    val lineWithDefaultLabel =
      Stroke("#FF0000") and
      Label("name".cql, fontFill=Fill("#000000"))

    val labelFollowingLine =
      Stroke("#FF0000") and
      Label("name".cql, fontFill=Fill("#000000"), followLine=true)

    val optimizedLabel =
      Stroke("#FF0000") and
      Label("name".cql, fontFill=Fill("#000000"), followLine=true,
        maxAngleDelta=90, maxDisplacement=400, repeat=150)

    val optimizedAndStyledLabel =
      Stroke("#FF0000") and
      Label("name".cql, fontFill=Fill("#000000"),
        font=Font("Arial", size=10, style="normal", weight="bold"),
        followLine=true, maxAngleDelta=90, maxDisplacement=400, repeat=150)

    val attributeBasedLine =
      (Stroke("#009933", width=2) where ("type = 'local-road'")) and
      (Stroke("#0055CC", width=3) where ("type = 'secondary'")) and
      (Stroke("#FF0000", width=6) where ("type = 'highway'"))

    val alternateAttributeBasedLine =
      Seq(("#009933", 2, "local-road"),
        ("#0055CC", 3, "secondary"),
        ("#FF0000", 6, "highway")
      ).map {
        case (c, w, t) => Stroke(c, width=w) where ("type = '"+t+"'")
      } reduceLeft (_ and _)

    val zoomBasedLine =
      (Stroke("#009933", width=6) belowScale 180000000) and
      (Stroke("#009933", width=4) aboveScale(180000000) belowScale(360000000)) and
      (Stroke("#009933", width=2) aboveScale(360000000))
  }

  object Points {
    val simplePoint = Symbol("circle", fill=Fill("#FF0000"), size=6)

    val simplePointWithStroke =
      Symbol("circle", fill=Fill("#FF0000"), stroke=Stroke("#000000", width=2), size=6)

    val rotatedSquare =
      Symbol("square", fill=Fill("#009900"), size=12, rotation=45)

    val transparentTriangle =
      Symbol("triangle", fill=Fill("#009900", opacity=0.2), stroke=Stroke("#000000", width=2), size=12)

    val pointAsGraphic = Graphic("smileyface.png", size=32)

    val pointWithDefaultLabel =
      Symbol("circle", fill=Fill("#FF0000"), size=6) and
      Label("name".cql, fontFill=Fill("#000000"))

    val pointWithStyledLabel =
      Symbol("circle", fill=Fill("#FF0000"), size=6) and
      Label("name".cql, fontFill=Fill("#000000"), font=Font("Arial", 12, "normal", "bold"), anchor=(1.5, 0.0), displacement=(0, 5))

    val pointWithRotatedLabel =
      Symbol("circle", fill=Fill("#FF0000"), size=6) and
      Label("name".cql, fontFill=Fill("#990099"), 
        font=Font("Arial", size=12, style="normal", weight="bold"),
        anchor=(0.5, 0), displacement=(0, 25), rotation= -45)

    val attribute =
      (Symbol("circle", fill=Fill("#0033CC"), size=8) where ("pop < 50000")) and
      (Symbol("circle", fill=Fill("#0033CC"), size=12) where ("pop > 50000 and pop < 100000")) and
      (Symbol("circle", fill=Fill("#0033CC"), size=16) where ("pop > 100000"))

    val zoom =
      (Symbol("circle", fill=Fill("#CC3300"), size=12) belowScale(160000000)) and
      (Symbol("circle", fill=Fill("#CC3300"), size=8) aboveScale(160000000) belowScale(320000000)) and
      (Symbol("circle", fill=Fill("#CC3300"), size=4) aboveScale(320000000))
  }

  object Polygon {
    val simplePolygon = Fill("#000080")

    val simplePolygonWithStroke = Fill("#000080") and Stroke("#FFFFFF", 2)

    val transparentPolygon = Fill("#000080", 0.5) and Stroke("#FFFFFF", 2)

    val graphicFill = Fill(Graphic("colorblocks.png", 93))

    val hatchingFill = Fill(Symbol("shape://times", stroke=Stroke("#990099", 1), size=16))

    val polygonWithDefaultLabel =
      Fill("#40FF40") and Stroke("#FFFFFF", 2) and Label("name".cql) 

    val labelHalo =
      Fill("#40FF40") and Stroke("#FFFFFF", 2) and 
      Label("name".cql, halo=Fill("#FFFFFF"))

    val polygonWithStyledLabel =
      Fill("#40FF40") and Stroke("#FFFFFF", 2) and
      Label("name".cql, anchor=(0.5, 0.5), fontFill=Fill("#000000"),
        font=Font("Arial", 11, "normal", "bold"), autoWrap=60,
        maxDisplacement=150)

    val attributeBasedPolygon =
      (Fill("#66FF66") where ("pop < 200000")) and
      (Fill("#33CC33") where ("pop >= 200000 and pop < 500000")) and
      (Fill("#009900") where ("pop >= 500000"))

    val zoomBasedPolygon =
      (Fill("#0000CC") and Stroke("#000000", 7) and Label("name".cql) belowScale(100000000)) and
      (Fill("#0000CC") and Stroke("#000000", 4) aboveScale(100000000) belowScale(200000000)) and
      (Fill("#0000CC") and Stroke("#000000", 1) aboveScale(200000000))
  }
}
