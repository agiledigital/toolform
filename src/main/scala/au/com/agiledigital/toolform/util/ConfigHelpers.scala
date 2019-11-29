package au.com.agiledigital.toolform.util

import pureconfig.error.{CannotConvert, FailureReason}

import scala.reflect.ClassTag

/**
  * Extensions to PureConfig functionality.
  */
object ConfigHelpers {

  /**
    * Convert a `String => Either[String, T]` into a `String => Either[FailureReason, T]` such that after application
    * - `Right(t)` becomes `_ => Right(t)`
    * - `Left(message)` becomes `message => Left(CannotConvert(value, type, message)`
    */
  def handleErrorWithEither[T](f: String => Either[String, T])(implicit ct: ClassTag[T]): String => Either[FailureReason, T] =
    string =>
      f(string).left.map { message =>
        CannotConvert(string, ct.runtimeClass.getName, message)
    }
}
