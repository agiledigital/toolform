package au.com.agiledigital.toolform.util

import java.text.SimpleDateFormat
import java.util.Calendar

object DateUtil {

  def formattedDateString: String = {
    val cal      = Calendar.getInstance
    val dateOnly = new SimpleDateFormat("dd/MM/yyyy")
    dateOnly.format(cal.getTime)
  }
}
