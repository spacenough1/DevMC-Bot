/*
 * Author spacenough 2024.
 */

package pl.codelen.edutor.bot.join;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.internal.Internal;
import org.jetbrains.annotations.NotNull;
import pl.codelen.edutor.bot.Bot;

import java.time.Instant;

public class JoinListener extends ListenerAdapter {

  @Override
  public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
    TextChannel channel = event.getGuild().getTextChannelById(Bot.getInstance().getConfigFile().getString("welcome-channel-id"));

    EmbedBuilder builder = new EmbedBuilder();

    builder.setTitle("Witaj na serwerze!");
    builder.setDescription("Miło Cię widzieć, " + event.getMember().getAsMention() + "!");
    builder.setThumbnail(event.getMember().getAvatarUrl());
    builder.setTimestamp(Instant.now());
    if (channel == null){
      System.out.println("Kanal od powitan nie istnieje, lub ID jest niepoprane");
      return;
    }

    channel.sendMessageEmbeds(builder.build()).queue();
  }
}