package au.com.agiledigital.toolform.tasks.generate

import scala.compat.Platform.EOL
import scalaz._

trait YamlWriter {

  type Result[A] = State[WriterContext, A]
  type IndexedState[A] = IndexedStateT[scalaz.Id.Id, WriterContext, WriterContext, A]

  private val indentSize = 2

  def write(text: String): Result[Unit] = State[WriterContext, Unit] { context =>
    {
      val indentRange = 0 until context.indentLevel * this.indentSize
      indentRange.foreach(_ => context.writer.write(" "))
      context.writer.write(text)
      context.writer.write(EOL)
      (context, ())
    }
  }

  def indent(): Result[Int] = State[WriterContext, Int] { context =>
    (context.copy(indentLevel = context.indentLevel + 1), context.indentLevel)
  }

  def resetIndent(indexLevel: Int): Result[Int] = State[WriterContext, Int] { context =>
    (context.copy(indentLevel = indexLevel), context.indentLevel)
  }

  def indented(innerState: IndexedState[Unit]): IndexedState[Unit] =
    for {
      initialIndentation <- indent()
      _                  <- innerState
      _                  <- resetIndent(initialIndentation)
    } yield ()
}

/**
  * Provides context to the generator while it is writing.
  *
  * @param writer      The object used to do the actual writing to the file.
  * @param indentLevel The number of indent levels deep the current context is.
  */
final case class WriterContext(writer: java.io.Writer, indentLevel: Int = 0)
