package org.jorgetrujillo.elitebot

import groovy.util.logging.Slf4j
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.jorgetrujillo.elitebot.domain.Sender
import org.jorgetrujillo.elitebot.services.RequestProcessorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@EnableAutoConfiguration
@Slf4j
class Application implements CommandLineRunner {

  @Autowired
  RequestProcessorService processorService

  @Value('${discord.token:#null}')
  String discordToken

  @Value('${discord.client_id:#null}')
  Long clientId

  @Override
  void run(String... args) {

    if (discordToken && clientId) {
      log.info('Starting up Elite Bot...')
      DiscordApi api = new DiscordApiBuilder().setToken(discordToken).login().join()

      // Add a listener which answers with "Pong!" if someone writes "!ping"
      api.addMessageCreateListener { MessageCreateEvent event ->

        Sender sender = new Sender(
            clientId: event.message.author.id,
            name: event.message.author.name
        )

        // Only process message if I was mentioned
        if (sender.clientId != clientId && event.message.mentionedUsers.find { it.id == clientId }) {

          // Print an ack message
          event.getChannel().sendMessage("I'll see if I can help with that @${event.message.author.name}")

          String response = processorService.processMessage(event.getMessage().getContent())

          log.info("Message from ${event.message.author} in ${event.message.channel}: ${event.message.content}")
          log.info("Response: ${response}")

          // Send the response

          if (response) {
            event.getChannel().sendMessage(response)
          }

        }
      }

      // Print the invite url of your bot
      log.info("You can invite the bot by using the following url: ${api.createBotInvite()}")
    } else {
      log.error('Required discord token and client ID were not provided!')
    }
  }

  static void main(String[] args) {
    SpringApplication.run(Application, args)
  }
}

