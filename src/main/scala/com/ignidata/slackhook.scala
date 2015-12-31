package com.ignidata

import akka.actor._
import akka.camel.{CamelExtension, CamelMessage, Producer}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.apache.camel.component.slack.SlackComponent
import org.psnively.scala.context.function.{FunctionalConfigApplicationContext, FunctionalConfiguration}

import scala.concurrent.duration._

class SlackConfiguration extends FunctionalConfiguration {

  val slack = bean("slack") {
    val slack = new SlackComponent()
        slack.setWebhookUrl(Configuration.config.getString("slack.token"))
        slack

  }
}
class MySlackProducer extends Actor with Producer {
  def endpointUri = Configuration.config.getString("slack.destination")
}

object MySlackProducer {
  val props:Props = Props(new MySlackProducer())
}

object Configuration {
  val config = ConfigFactory.load()
}

object Slackhook extends App {

  import akka.pattern.ask

  implicit val timeout = Timeout(10 seconds)

  val system = ActorSystem("Slackhook", Configuration.config)
  val applicationContext = FunctionalConfigApplicationContext[SlackConfiguration]

  val camel = CamelExtension(system)
      camel.context.addComponent("slack", applicationContext.getBean("slack").asInstanceOf[SlackComponent])

  val msp = system.actorOf(MySlackProducer.props, "SlackProducer")
      msp.ask("This is posted to #test and comes from databot which isn't an autobot, yet again, it isn't a decepticon either").mapTo[CamelMessage]
}



