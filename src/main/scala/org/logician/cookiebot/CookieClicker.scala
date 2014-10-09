package org.logician.cookiebot

import org.openqa.selenium.{WebElement, By, WebDriver}

/**
 * Created with IntelliJ IDEA.
 * User: Austin
 * Date: 10/22/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
class CookieClicker(val driver: WebDriver) {

  def start() = driver.get("http://orteil.dashnet.org/cookieclicker/")

  def end() = driver.close()

  lazy val cookie = driver.findElement(By.id("bigCookie"))

  private lazy val cookieCounter = driver.findElement(By.id("cookies"))

  def cookieCount = {
    val cookieString = cookieCounter.getText

    cookieString.substring(0, getCookiesIndex(cookieString)).filter(_ != ',').toInt
  }

  private def getCookiesIndex(cookieString: String) = cookieString.indexOf(" cookie")

  private lazy val store = driver.findElement(By.id("store"))

  def firstAvailableProduct = firstAvailableElement(By.id("products"), By.className("enabled"))

  def firstAvailableUpgrade = firstAvailableElement(By.id("upgrades"), By.className("enabled"))
  
  private def firstAvailableElement(containerBy: By, elementBy: By): Option[WebElement] = {
    val availableElements = store.findElement(containerBy).findElements(elementBy)

    if(availableElements.isEmpty)
      Option.empty[WebElement]
    else
      Option(availableElements.get(0))
  }

  lazy val goldenCookie = driver.findElement(By.id("goldenCookie"))

  def isGoldenCookieVisible = !goldenCookie.getCssValue("display").equals("none")

}
