package au.com.agiledigital.toolform.util

import scala.util.control.Exception._

/**
  * A collection of useful functions for working with strings.
  */
object StringUtil {

  /**
    * Shorthand method for checking if a value can be converted to an integer.
    * @param string the string to check.
    * @return true if the value can be safely converted to an integer, otherwise false.
    */
  def isInt(string: String): Boolean = toIntOpt(string).isDefined

  /**
    * Tries to parse a string into an int.
    * If it fails, it will return None rather than throw an exception.
    * @param string the string to parse into an int.
    * @return an integer value on success, otherwise None.
    */
  def toIntOpt(string: String): Option[Int] = catching(classOf[NumberFormatException]) opt string.toInt
}
