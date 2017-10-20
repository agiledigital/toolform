package au.com.agiledigital.toolform.command.generate

import cats.data._

import scala.compat.Platform.EOL

trait YamlWriter {

  type Result[A] = State[WriterContext, A]

  /**
    * A function that just passes state through without modifying it
    */
  protected val identity: State[WriterContext, Unit] = State[WriterContext, Unit] { context =>
    (context, ())
  }

  private val indentSize = 2

  protected def write(text: String): Result[Unit] = State[WriterContext, Unit] { context =>
    {
      val indentRange = 0 until context.indentLevel * this.indentSize
      indentRange.foreach(_ => context.writer.write(" "))
      context.writer.write(text)
      context.writer.write(EOL)
      (context, ())
    }
  }

  protected def indent(): Result[Int] = State[WriterContext, Int] { context =>
    (context.copy(indentLevel = context.indentLevel + 1), context.indentLevel)
  }

  protected def resetIndent(indexLevel: Int): Result[Int] = State[WriterContext, Int] { context =>
    (context.copy(indentLevel = indexLevel), context.indentLevel)
  }

  protected def indented(innerState: Result[Unit]): Result[Unit] =
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
protected final case class WriterContext(writer: java.io.Writer, indentLevel: Int = 0)
