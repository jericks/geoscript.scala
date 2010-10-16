package org.geoscript.filter

sealed abstract trait Expression {
  def underlying: org.opengis.filter.expression.Expression
}

object Expression {
  case class Wrapped(underlying: org.opengis.filter.expression.Expression)
  extends Expression

  private val filter =
    org.geotools.factory.CommonFactoryFinder.getFilterFactory2(null)

  implicit def enrichString(s: String) =
    new {
      def cql: Expression =
        Wrapped(org.geotools.filter.text.ecql.ECQL.toExpression(s))
    }

  implicit def literalToExpression(x: String): Expression =
    Wrapped(filter.literal(x))

  implicit def literalToExpression(x: Double): Expression =
    Wrapped(filter.literal(x))

  implicit def unwrap(wrapped: Expression)
  : org.opengis.filter.expression.Expression
  = wrapped.underlying

  implicit def wrap(underlying: org.opengis.filter.expression.Expression)
  : Expression
  = Wrapped(underlying)
}


