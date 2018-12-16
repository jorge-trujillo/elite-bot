package org.jorgetrujillo.elitebot

import groovy.util.logging.Slf4j
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.channel.ChannelType
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import org.jorgetrujillo.elitebot.domain.Sender
import org.jorgetrujillo.elitebot.services.RequestProcessorService
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@Slf4j
class Application implements CommandLineRunner {

  @Autowired
  RequestProcessorService processorService

  @Value('${discord.token:#{null}}')
  String discordToken

  @Value('${discord.log_user_id:#{null}}')
  Long logUserId

  @Value('${discord.log_channel_id:#{null}}')
  Long logChannelId

  @Override
  void run(String... args) {

    if (discordToken) {
      log.info('Starting up Elite Bot...')
      DiscordApi api = new DiscordApiBuilder().setToken(discordToken).login().join()

      // Get ID and other state vars
      long selfId = api.getYourself().id
      User logUser
      if (logUserId) {
        logUser = api.getUserById(logUserId).get()
      }

      // Listen for messages
      api.addMessageCreateListener { MessageCreateEvent event ->

        MDC.put('request_id', UUID.randomUUID().toString())

        Sender sender = new Sender(
            clientId: event.message.author.id,
            name: event.message.author.name
        )

        // Only process message if I was mentioned, or if in private channel
        boolean isPrivateChannel = (event.message.channel.type == ChannelType.PRIVATE_CHANNEL)
        if (sender.clientId != selfId &&
            (event.message.mentionedUsers.find { it.id == selfId } || isPrivateChannel)) {

          // Print an ack message
          event.getChannel().sendMessage("I'll see if I can help with that ${event.message.author.name}...")

          // Get the response
          String response = processorService.processMessage(event.getMessage().getContent())

          log.info("Message from ${event.message.author} in ${event.message.channel.type}: ${event.message.content}")
          log.info("Response: ${response}")

          // Send the response. Directly mention the user if not a private channel
          if (response) {
            User messageAuthor = event.message.userAuthor.orElse(null)
            String mention = (messageAuthor && !isPrivateChannel) ? messageAuthor.mentionTag : ''
            event.getChannel().sendMessage(mention + ' ' + response)

            // Send a log message to me
            logMessage(logUser, sender, event.getMessage().getContent(), response)
          }

        }
      }

      // Print the invite url of your bot
      log.info("You can invite the bot by using the following url: ${api.createBotInvite()}")
    } else {
      log.error('Required discord token and client ID were not provided!')
    }
  }

  private static void logMessage(User logUser, Sender sender, String message, String response) {

    if (logUser) {
      String logEntry = "**Request from ${sender.name}**: \n" +
          "```${message.replaceAll(/@[^\s]+/, '')}```\n" +
          "**Response**: ${response}"
      logUser.openPrivateChannel().get().sendMessage(logEntry)
    }
  }

  static void main(String[] args) {
    SpringApplication.run(Application, args)
  }

}

