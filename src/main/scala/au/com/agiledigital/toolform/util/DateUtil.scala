package au.com.agiledigital.toolform.util

import java.text.SimpleDateFormat
import java.util.Calendar

/**
  * A collection of useful functions for working with dates.
  */
object DateUtil {

  /**
    * Gets the current date (without time) as a string in format dd/MM/yyyy.
    * @return a formatted date string.
    */
  def formattedDateString: String = {
    val cal      = Calendar.getInstance
    val dateOnly = new SimpleDateFormat("dd/MM/yyyy")
    dateOnly.format(cal.getTime)
  }
}
