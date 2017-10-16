package au.com.agiledigital.toolform.tasks

import au.com.agiledigital.toolform.app.{ToolFormConfiguration, ToolFormError}
import au.com.agiledigital.toolform.model.Project

trait Task {

  def run(toolFormConfiguration: ToolFormConfiguration, project: Project): Either[ToolFormError, String]
}
