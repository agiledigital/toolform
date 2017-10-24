package au.com.agiledigital.toolform.util

import pureconfig.error.{CannotConvert, ConfigReaderFailure, ConfigValueLocation}

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
  def handleErrorWithEither[T](f: String => Either[String, T])(implicit ct: ClassTag[T]): String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T] =
    string =>
      location =>
        f(string).left.map { message =>
          CannotConvert(string, ct.runtimeClass.getName, message, location, "")
    }
}
