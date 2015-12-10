package net.selenate
package server
package sessions
package actions

import akka.actor.TypedProps
import com.instantor.selenium.InstantorWebDriver
import common.comms.res._
import common.comms.req._
import org.openqa.selenium.remote.RemoteWebDriver
import scala.collection.JavaConverters._
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds._
import java.nio.file.Path
import java.io.{FileNotFoundException, File}
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.WatchService
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

class DownloadFileAction(val d: RemoteWebDriver, sessionID: String)(implicit context: ActionContext)
    extends IAction[SeReqDownloadFile, SeResDownloadFile]
    with ActionCommons {

  protected val log = Log(classOf[DownloadFileAction])


  private def downloadOld(arg: SeReqDownloadFile): SeResDownloadFile = {
    log.info("Invoked DownloadFileAction for %s".format(arg.toString))

    val watchService = FileSystems.getDefault.newWatchService()
    val ffDownloads = new File("/tmp/ff-downloads")
    if(!ffDownloads.isDirectory) ffDownloads.mkdir
    val dir         = new File("/tmp/ff-downloads/%s".format(sessionID))
    if (!dir.isDirectory) dir.mkdir()
    val path     = dir.toPath

    path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY)
    log.info("Registered watch service on %s".format(dir.toString))

    //    val findelementreq = new SeReqFindElement(arg.method, arg.query)
    //    val metadata = new FindElementAction(d).act(findelementreq).element

    log.info("Before click...")
    new ClickAction(d).act(new SeReqClick(arg.windowHandle, arg.framePath, arg.method, arg.query))
    log.info("After click...")

    try{
      val (ba, ext) = Await.result(Future{tryPollResult(path, watchService)}, 30 seconds)
      new SeResDownloadFile(ba, ext)
    } catch {
      case e: Throwable =>
        log.error("No file was created under firefox downloads", e)
        throw new IllegalArgumentException(e)
    } finally {
      watchService.close()
    }
  }

  implicit class ScalaFn2JavaFn[A, R](f: A => R) {
    def asJava = new java.util.function.Function[A, R] {
        override def apply(a: A) = f(a)
    }
  }

  def act = { arg =>
    d match {
      case id: InstantorWebDriver =>

        val f : RemoteWebDriver => java.lang.Boolean = { idd =>
          tryb {
            findElement(arg.method, arg.query)
          } match {
            case true =>
              findElement(arg.method, arg.query).click()
              true
            case x => x
          }
        }

        val dfOpt = id.downloadFile(f.asJava)

        if(dfOpt.isPresent) {
          val file = dfOpt.get()
          new SeResDownloadFile(file.content, FilenameUtils.getExtension(file.name))
        } else {
          throw new FileNotFoundException("Download file failed.")
        }
      case _ => downloadOld(arg)
    }

  }

  private def listenerOverFile(parentDir: Path, parentDirFiles: Array[File], filePath: Path): (Array[Byte], String) = {
    // Sleep between repeated pinging to download folder
    Thread.sleep(2000)
    /*
     * Firefox creates partial files while downloading.
     * On download complete, partials are deleted and real file is filled.
     * There are no part files, file size isn't changed. Seems that download is complete
     */
    if (parentDirFiles.zip(parentDir.toFile.listFiles).forall( f => f._1.length == f._2.length)){
      dumpFile(parentDir.resolve(filePath).toFile)
    } else {
      listenerOverFile(parentDir, parentDir.toFile.listFiles, filePath)
    }
  }

  private def tryPollResult(parentPath: Path, watchService: WatchService): (Array[Byte], String) = {
    try{
      watchService.take().pollEvents().asScala.find{ e=>
        val cond = e.context().isInstanceOf[Path] && e.kind == ENTRY_CREATE && !e.context.asInstanceOf[Path].toString().endsWith("part")
        log.info("Event %s fired for %s".format(e.kind, e.context.asInstanceOf[Path].toString()))
        cond
      } match {
        case Some(event) =>
          log.info("Fired blocking await for resolve file %s".format(event.context().asInstanceOf[Path].toString))
          val res = Await.result(Future{listenerOverFile(parentPath, parentPath.toFile.listFiles, event.context().asInstanceOf[Path])}, 30 seconds)
          log.info("Resolved download for file %s".format(event.context().asInstanceOf[Path].toString))
          res
        case None => tryPollResult(parentPath, watchService)
      }
    } catch {
      case ie: InterruptedException =>
        log.error("tryPollResult was interrupted", ie)
        throw ie
      case other: Throwable         =>
        log.error("Error on tryPoll", other)
        throw other
    }

  }

  private def dumpFile(file: File): (Array[Byte], String) = {
    val ext  = FilenameUtils.getExtension(file.toString)
    val ba = FileUtils.readFileToByteArray(file)
    (ba, ext)
  }
}
