package au.com.agiledigital.toolform.plugin

import au.com.agiledigital.toolform.app.ToolFormError
import com.monovore.decline._

/**
  * Extension point to add new commands to toolform.
  * Uses Java SPI - See [[java.util.ServiceLoader]] for details.
  * Implement the trait and register the new implementation in
  * META-INF/services/au.com.agiledigital.toolform.plugin.ToolFormPlugin
  * on the runtime classpath.
  */
trait ToolFormPlugin {
  def command: Opts[Either[ToolFormError, String]]
}
