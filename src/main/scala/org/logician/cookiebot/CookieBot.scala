package org.logician.cookiebot

import org.openqa.selenium.firefox.FirefoxDriver
import scala.swing._
import scala.actors.Actor._
import scala.swing.event.{SelectionChanged, ButtonClicked}
import scala.actors.Actor
import org.openqa.selenium.{WebDriverException, StaleElementReferenceException}
import org.openqa.selenium.chrome.ChromeDriver
import com.opera.core.systems.OperaDriver
import org.openqa.selenium.ie.InternetExplorerDriver

/**
 * Created with IntelliJ IDEA.
 * User: Austin
 * Date: 10/22/13
 * Time: 5:10 PM
 * To change this template use File | Settings | File Templates.
 */

object CookieBot extends SimpleSwingApplication{

  private val reactor = actor{
    loop{
      react{
        case 'Start =>{
          logicLoop()
          self ! 'Start
        }
        case 'Stop => exit()
      }
    }
  }

  private def reactorState(start: Boolean){
    if(start){
      if(reactor.getState == Actor.State.Terminated)
        reactor.restart()
      reactor ! 'Start
    }
    else
      reactor ! 'Stop
  }

  private var cookieClicker: CookieClicker = _


  private val browserCombo = new ComboBox(Seq("Firefox", "Chrome", "Opera", "IE"))

  private val runToggle = new ToggleButton {
    enabled = false
    text = "Run"
    reactions += {
      case _: ButtonClicked => reactorState(selected)
    }
  }

  val top = new MainFrame {
    title = "CookieBot"
    contents = new BoxPanel(Orientation.Vertical){

      contents += new Label("Browser:")

      contents += browserCombo

      contents += Swing.VStrut(10)

      contents += new ToggleButton{
        text = "Start"

        reactions += {
          case _: ButtonClicked => {
            if(selected){
              try{
                createCookieClicker(browserCombo.selection.item)
                text = "Stop"
                cookieClicker.start()
                runToggle.enabled = true
              } catch {
                case _: Exception =>{
                  invalidBrowserDialog()
                  selected = false
                }
              }
            } else{
              runToggle.enabled = false
              runToggle.selected = false
              cookieClicker.end()
              text = "Start"
            }
          }
        }
      }

      contents += Swing.VStrut(10)

      contents += runToggle

      contents += Swing.VStrut(10)

      contents += new Button{
        text = "Quit"
        reactions += {
          case _: ButtonClicked => quit()
        }
      }
    }
  }

  def createCookieClicker(browser: String){
    val driver = browser match{
      case "Firefox" => firefoxDriver
      case "Chrome" => chromeDriver
      case "Opera" => operaDriver
      case "IE" => ieDriver
    }

    cookieClicker = new CookieClicker(driver)
  }

  def firefoxDriver = new FirefoxDriver()

  def chromeDriver = new ChromeDriver()

  def operaDriver = new OperaDriver()

  def ieDriver = new InternetExplorerDriver()

  def invalidBrowserDialog(){
    Dialog.showMessage(
      message =
        "Failed to start selected browser.\n" +
          "Either the browser is not installed, or you don't have the required driver.\n" +
          "Please see the documentation for more info.",
      title = "Failed to initialize!",
      messageType = Dialog.Message.Error
    )
  }

  override def shutdown(){
    reactor ! 'Stop

    if(cookieClicker != null)
      cookieClicker.end()
  }

  private val clickRange = 0 to 10

  def logicLoop(){
    clickRange.foreach(_ => cookieClicker.cookie.click())

    if(cookieClicker.isGoldenCookieVisible)
      cookieClicker.goldenCookie.click()

    buySomething()
  }

  def buySomething(){
    try{
      val firstUpgrade = cookieClicker.firstAvailableUpgrade
      if(firstUpgrade.isDefined){
        firstUpgrade.get.click()
        buySomething()
      }
    } catch{
      // An exception here means that our upgrades have reshuffled because we unlocked something.
      case e: StaleElementReferenceException => buySomething()
    }

    val firstProduct = cookieClicker.firstAvailableProduct
    if(firstProduct.isDefined){
      firstProduct.get.click()
      buySomething()
    }
  }
}
