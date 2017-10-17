package au.com.agiledigital.toolform.plugin

import au.com.agiledigital.toolform.app.{ToolFormConfiguration, ToolFormError}
import au.com.agiledigital.toolform.model.Project
import scopt.OptionParser

/**
  * Extension point to add new commands to toolform.
  * Uses Java SPI - See [[java.util.ServiceLoader]] for details.
  * Implement the trait and register the new implementation in
  * META-INF/services/au.com.agiledigital.toolform.plugin.ToolFormPlugin
  * on the runtime classpath.
  */
trait ToolFormPlugin {
  def commandName: String
  def commandVersion: String
  def commandLineArgs(parser: OptionParser[ToolFormConfiguration]): Unit
  def configureCommand(toolConfig: ToolFormConfiguration): Either[ToolFormError, ToolFormCommand]
}

/**
  * The command that will be executed when the plugin is invoked by the toolform app.
  */
trait ToolFormCommand {
  def execute(project: Project): Either[ToolFormError, String]
}
