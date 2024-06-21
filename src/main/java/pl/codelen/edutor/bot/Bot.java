/*
 * Author spacenough 2024.
 */

/*
 * Author spacenough 2024.
 */

package pl.codelen.edutor.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import pl.codelen.edutor.bot.commands.CommandsHandler;
import pl.codelen.edutor.bot.join.JoinListener;
import pl.codelen.edutor.bot.ticket.TicketListener;
import pl.codelen.edutor.bot.yaml.YamlBase;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

public class Bot extends ListenerAdapter {
  private YamlBase configFile;
  private JDA jda;
  private static Bot instance;

  public static void main(String[] args) {
    new Bot().run();
  }

  public void run() {
    instance = this;
    configFile = new YamlBase(new File("edutor", "config.yml"), getResource("config.yml"));
    JDALogger.setFallbackLoggerEnabled(false);

    jda = JDABuilder.create(
            Arrays.asList(GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.AUTO_MODERATION_EXECUTION, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_WEBHOOKS))
            .setToken(configFile.getString("token"))
            .setActivity(Activity.watching("edutor.pl"))
            .setAutoReconnect(true)
            .disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.SCHEDULED_EVENTS)
            .build();

    jda.addEventListener(
            new Bot(),
            new TicketListener(),
            new CommandsHandler(),
            new JoinListener()
    );
  }

  @Override
  public void onReady(@NotNull ReadyEvent event) {
    event.getJDA().updateCommands().addCommands(
            Commands.slash("weryfikacja", "Dodaję na kanał wiadomość z weryfikacją."),
            Commands.slash("ticket", "Dodaję na kanał wiadomość ze zgłoszeniem.")
    ).complete();
  }

  public InputStream getResource(String file) {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    return classloader.getResourceAsStream(file);
  }

  public JDA getJda() {
    return jda;
  }

  public YamlBase getConfigFile() {
    return configFile;
  }

  public static Bot getInstance() {
    return instance;
  }
}