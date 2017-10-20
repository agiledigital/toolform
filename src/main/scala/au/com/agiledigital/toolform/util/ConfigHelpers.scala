package au.com.agiledigital.toolform.util

import pureconfig.error.{CannotConvert, ConfigValueLocation}

import scala.reflect.ClassTag

/**
  * Extensions to PureConfig functionality.
  */
object ConfigHelpers {

  /**
    * Convert a `String => Either[String, T]` into a `String => Option[ConfigValueLocation] => Either` such that after application
    * - `Right(t)` becomes `_ => Right(t)`
    * - `Left(message)` becomes `location => Left(CannotConvert(value, type, message, location)`
    */
  def handleErrorWithEither[T](f: String => Either[String, T])(implicit ct: ClassTag[T]): String => Option[ConfigValueLocation] => Either[CannotConvert, T] =
    string =>
      location =>
        f(string) match {
          case Right(t)      => Right(t)
          case Left(message) => Left(CannotConvert(string, ct.runtimeClass.getName, message, location, ""))
    }
}
