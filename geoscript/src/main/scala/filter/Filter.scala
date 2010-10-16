package org.geoscript.filter

sealed abstract trait Filter {
  def underlying: org.opengis.filter.Filter
}

object Filter {
  val Include = Filter.Wrapped(org.opengis.filter.Filter.INCLUDE)
  val Exclude = Filter.Wrapped(org.opengis.filter.Filter.EXCLUDE)
  val Factory =
    org.geotools.factory.CommonFactoryFinder.getFilterFactory2(null)

  case class Wrapped(underlying: org.opengis.filter.Filter) extends Filter

  implicit def cqlToFilter(cql: String): Filter =
    Wrapped(org.geotools.filter.text.ecql.ECQL.toFilter(cql))

  implicit def unwrap(wrapped: Filter): org.opengis.filter.Filter =
    wrapped.underlying

  implicit def wrap(underlying: org.opengis.filter.Filter): Filter =
    Wrapped(underlying)
}
