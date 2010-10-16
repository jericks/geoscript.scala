package org.geoscript.style_new

import org.geoscript.filter._

import scala.collection.JavaConversions._

sealed abstract trait Style {
  def where(filter: Filter): Style
  def aboveScale(s: Double): Style
  def belowScale(s: Double): Style
  def and(other: Style): Style
  def underlying: org.geotools.styling.Style
}

sealed abstract trait Paint {
  def asStroke(
    width: Expression,
    opacity: Expression,
    linejoin: Expression,
    linecap: Expression,
    dasharray: Seq[Float],
    dashoffset: Expression,
    mode: Stroke.Mode
  ): org.geotools.styling.Stroke

  def asFill(
    opacity: Expression
  ): org.geotools.styling.Fill
}

object Style {
  val Factory =
    org.geotools.factory.CommonFactoryFinder.getStyleFactory(null)
}

object Stroke {
  sealed abstract trait Mode
  object Tile extends Mode
  object Follow extends Mode
}

abstract class StyleImpl extends Style {
  private val styles =
    org.geotools.factory.CommonFactoryFinder.getStyleFactory(null)
  private val filters =
    org.geotools.factory.CommonFactoryFinder.getFilterFactory2(null)

  def filter: Option[Filter]

  def minScale: Option[Double]

  def maxScale: Option[Double]

  def symbolizers: Seq[org.geotools.styling.Symbolizer]
  
  override def where(p: Filter): Style = {
    val outer = this
    new StyleImpl {
      def filter = 
        outer.filter.map(filters.and(p, _): Filter).orElse(Some(p))
      def minScale = outer.minScale
      def maxScale = outer.maxScale
      def symbolizers = outer.symbolizers
    }
  }

  override def aboveScale(s: Double): Style = {
    val outer = this
    new StyleImpl {
      def filter = outer.filter
      def minScale = outer.minScale.map(math.max(_, s)).orElse(Some(s))
      def maxScale = outer.maxScale
      def symbolizers = outer.symbolizers
    }
  }

  override def belowScale(s: Double): Style = {
    val outer = this
    new StyleImpl {
      def filter = outer.filter
      def minScale = outer.minScale
      def maxScale = outer.maxScale.map(math.min(_, s)).orElse(Some(s))
      def symbolizers = outer.symbolizers
    }
  }

  override def and(that: Style): Style = {
    val outer = this
    new StyleImpl {
      def filter = outer.filter
      def minScale = outer.minScale
      def maxScale = outer.maxScale
      def symbolizers = outer.symbolizers

      override def aboveScale(s: Double): Style =
        outer.aboveScale(s) and that.aboveScale(s)

      override def belowScale(s: Double): Style =
        outer.belowScale(s) and that.belowScale(s)

      override def where(p: Filter): Style =
        outer.where(p) and that.where(p)

      override def underlying = {
        val style = outer.underlying
        style.featureTypeStyles.addAll(that.underlying.featureTypeStyles)
        style
      }
    }
  }

  def underlying = {
    val rule = styles.createRule()
    for (f <- filter) rule.setFilter(f.underlying)
    for (s <- minScale) rule.setMinScaleDenominator(s)
    for (s <- maxScale) rule.setMaxScaleDenominator(s)
    rule.symbolizers.addAll(symbolizers)

    val ftstyle = styles.createFeatureTypeStyle()
    ftstyle.rules.add(rule)

    val style = styles.createStyle()
    style.featureTypeStyles.add(ftstyle)
    style
  }
}

object Paint {
  val NamedColors = Map(
    "red"   -> "#FF0000",
    "green" -> "#00FF00",
    "blue"  -> "#0000FF"
  )

  implicit def stringToPaint(colorName: String): Paint =
    if (NamedColors contains colorName)
      Color(NamedColors(colorName))
    else if (colorName matches ".*") // TODO: regex for hex color codes
      Color(colorName)
    else
      Color("#000000")
}

case class Color(rgb: String) extends Paint {
  private val factory =
    org.geotools.factory.CommonFactoryFinder.getStyleFactory(null)
  private val filter =
    org.geotools.factory.CommonFactoryFinder.getFilterFactory2(null)

  def asStroke(
    width: Expression,
    opacity: Expression,
    linejoin: Expression,
    linecap: Expression,
    dasharray: Seq[Float],
    dashoffset: Expression,
    mode: Stroke.Mode
  ): org.geotools.styling.Stroke = {
    factory.createStroke(
      filter.literal(rgb),
      if (width == null) null else width,
      if (opacity == null) null else opacity,
      if (linejoin == null) null else linejoin,
      if (linecap == null) null else linecap,
      if (dasharray == null) null else dasharray.toArray,
      if (dashoffset == null) null else dashoffset,
      null,
      null
    )
  }

  def asFill(
    opacity: Expression
  ): org.geotools.styling.Fill = {
    factory.fill(
      null,
      filter.literal(rgb),
      opacity
    )
  }
}

case class Fill(fill: Paint = null, opacity: Expression = null)
extends StyleImpl
{
  private val factory =
    org.geotools.factory.CommonFactoryFinder.getStyleFactory(null)

  override val maxScale = None
  override val minScale = None
  override val filter = None
  override val symbolizers =
    Seq(factory.createPolygonSymbolizer(null, fill.asFill(opacity), null))
}

case class Stroke(
  stroke: Paint = null,
  width: Expression = null,
  opacity: Expression = null,
  linecap: Expression = null,
  linejoin: Expression = null,
  dashoffset: Expression = null,
  dasharray: Seq[Float] = null,
  mode: Stroke.Mode = Stroke.Follow
) extends StyleImpl {
  private val factory =
    org.geotools.factory.CommonFactoryFinder.getStyleFactory(null)

  override val maxScale = None
  override val minScale = None
  override val filter = None
  override val symbolizers =
    Seq(factory.createLineSymbolizer(
      stroke.asStroke(
        width, opacity, linejoin, linecap, dasharray, dashoffset, mode
      ),
      null
    ))
}

case class Label(
  text: Expression,
  geometry: Expression
) extends StyleImpl {
  private val factory =
    org.geotools.factory.CommonFactoryFinder.getStyleFactory(null)

  val maxScale = None
  val minScale = None
  val filter = None
  val symbolizers = {
    val sym = factory.createTextSymbolizer()
    sym.setLabel(text)
    sym.setGeometry(geometry)
    Seq(sym)
  }
}

case class Symbol(
  shape: Expression,
  fill: Fill,
  stroke: Stroke,
  size: Expression,
  rotation: Expression,
  opacity: Expression
) extends StyleImpl with Paint {
  val filter = None
  val maxScale = None
  val minScale = None
  val symbolizers = 
    Seq(
      new org.geotools.styling.StyleBuilder(Style.Factory)
        .createPointSymbolizer(graphic)
    )


  def asStroke(
    width: Expression,
    opacity: Expression,
    linejoin: Expression,
    linecap: Expression,
    dasharray: Seq[Float],
    dashoffset: Expression,
    mode: Stroke.Mode
  ): org.geotools.styling.Stroke = {
    Style.Factory.createStroke(
      null,
      width,
      opacity,
      linejoin,
      linecap,
      dasharray.toArray,
      dashoffset,
      if (mode == Stroke.Tile) graphic else null,
      if (mode == Stroke.Follow) graphic else null
    )
  }

  def asFill(
    opacity: Expression
  ): org.geotools.styling.Fill = {
    Style.Factory.fill(
      graphic,
      null,
      opacity
    )
  }

  def graphic = 
    Style.Factory.createGraphic(
      null,
      Array(
        Style.Factory.createMark(
          shape,
          stroke.stroke.asStroke(
            stroke.width,
            stroke.opacity,
            stroke.linejoin,
            stroke.linecap,
            stroke.dasharray,
            stroke.dashoffset,
            stroke.mode
          ),
          fill.fill.asFill(fill.opacity),
          size,
          rotation
        )
      ),
      null,
      opacity,
      size,
      rotation
    )
}

case class Graphic(
  url: String,
  opacity: Expression,
  size: Expression,
  rotation: Expression
) extends StyleImpl with Paint {
  private val factory =
    org.geotools.factory.CommonFactoryFinder.getStyleFactory(null)

  val filter = None
  val maxScale = None
  val minScale = None
  val symbolizers = 
    Seq(
      new org.geotools.styling.StyleBuilder(factory)
        .createPointSymbolizer(graphic)
    )

  def asStroke(
    width: Expression,
    opacity: Expression,
    linejoin: Expression,
    linecap: Expression,
    dasharray: Seq[Float],
    dashoffset: Expression,
    mode: Stroke.Mode
  ): org.geotools.styling.Stroke = {
    factory.createStroke(
      null,
      width,
      opacity,
      linejoin,
      linecap,
      dasharray.toArray,
      dashoffset,
      if (mode == Stroke.Tile) graphic else null,
      if (mode == Stroke.Follow) graphic else null
    )
  }

  def asFill(
    opacity: Expression
  ): org.geotools.styling.Fill = {
    factory.fill(
      graphic,
      null,
      opacity
    )
  }

  def graphic = 
    factory.createGraphic(
      Array(factory.createExternalGraphic(url, "image/png")),
      null,
      null,
      opacity,
      size,
      rotation
    )
}
