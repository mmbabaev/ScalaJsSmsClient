import org.scalajs.dom
import org.scalajs.dom._

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scalatags.JsDom.all._
import upickle.default._

//From Client
sealed trait ClientWSMessage
case class ClientPhoneInPut(phone: String) extends ClientWSMessage
case class ClientLogin(phone: String, password: Int) extends ClientWSMessage



//From Server
sealed trait ServerWSMessage
case class ServerSuccessPhone() extends ServerWSMessage
case class ServerSuccessRegistration() extends ServerWSMessage
case class ServerError(title: String, message: String) extends ServerWSMessage

object ExampleSmsApp extends JSApp {


  val ws = new dom.WebSocket("ws://localhost:8080/registration")

  if (ws != null) {
    ws.onmessage = (x: MessageEvent) => readSocketMessage(x.data.toString)
    ws.onopen = (x: Event) => println("Client socket was opened")
    ws.onerror = (x: dom.ErrorEvent) => println("some error has occured " + x.message)
    ws.onclose = (x: dom.CloseEvent) => println("Client socket was closed!")
  }

  val phoneBox = input(
    `type`:="tel",
    placeholder:="Phone number"
  ).render

  val passwordBox = input(
    `type` := "text",
    placeholder:="Password"
  ).render

  val sendPhoneButton = input(
    `type` := "button",
    value := "Get SMS with password"
  ).render

  sendPhoneButton.onclick = (e: dom.Event) => {
    val phone = phoneBox.value
    val message = write(ClientPhoneInPut(phone))
    ws.send(message)
  }


  val registerButton = input(
    `type` := "button",
    value := "Register"
  ).render

  registerButton.onclick = (e: dom.Event) => {
    val phone = phoneBox.value
    try {
      val password = passwordBox.value.toInt
      val message = write(ClientLogin(phone, password))
      ws.send(message)
    }
    catch {
      case e: Exception => alert("Wrong password!")
    }
  }

  def main(): Unit = {


    //
    document.body.appendChild(
      div(
        h1("Example application"),
        div(phoneBox),
        div(sendPhoneButton)
      ).render
    )
  }



  def readSocketMessage(messageString: String): Unit = {
    val message = read[ServerWSMessage](messageString)

    message match {
      case ServerSuccessPhone() =>

        alert("Wait for SMS with password!")
        document.body.appendChild(
          div(
            div(passwordBox),
            div(registerButton)
          ).render
        )
      case ServerSuccessRegistration() => alert("Welcome! :)")
      case ServerError(title: String, message: String) => alert(title + "\n" + message)
    }
  }

}