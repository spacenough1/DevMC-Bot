/*
 * Author spacenough 2024.
 */

package pl.codelen.edutor.bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import pl.codelen.edutor.bot.Bot;

import java.time.Instant;
import java.util.Objects;

public class CommandsHandler extends ListenerAdapter {
  public final Button ticketButton = Button.primary("ticketID", "Utworz ticket");
  public final Button verifyButton = Button.primary("verifyID", "Weryfikacja");

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    String noPermission = "Nie masz wymaganych uprawnień.";
    String eventName = event.getName();

    if ("ticket".equals(eventName)) {
      if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR)) {
        event.reply(noPermission).setEphemeral(true).queue();
        return;
      }
      event.deferReply().complete();

      EmbedBuilder eb = new EmbedBuilder()
              .setTitle("Ticket")
              .setDescription("Kliknij przycisk poniżej, aby utworzyć zgłoszenie i zadać pytanie.")
              .setTimestamp(Instant.now());

      event.getHook().sendMessageEmbeds(eb.build()).addActionRow(ticketButton).queue();
      return;
    }

    if ("weryfikacja".equals(eventName)) {
      if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR)) {
        event.reply(noPermission).setEphemeral(true).queue();
        return;
      }
      event.deferReply().complete();

      EmbedBuilder eb = new EmbedBuilder()
              .setTitle("Weryfikacja")
              .setDescription("Kliknij przycisk poniżej, aby dostać rolę zweryfikowanego i uzyskać dostęp do wszystkich kanałów.")
              .setTimestamp(Instant.now());

      event.getHook().sendMessageEmbeds(eb.build()).addActionRow(verifyButton).queue();
    }
  }

  @Override
  public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
    if (!event.getComponentId().equalsIgnoreCase("verifyID")) {
      return;
    }

    Role role = Objects.requireNonNull(event.getGuild())
            .getRoleById(Bot.getInstance().getConfigFile().getString("verify-role-id"));

    if (role == null) {
      event.getChannel().sendMessage("Rola nie istnieje!").queue();
      return;
    }

    event.deferEdit().complete();
    event.getGuild().addRoleToMember(Objects.requireNonNull(event.getMember()), role).queue();
    Objects.requireNonNull(event.getMember()).getUser().openPrivateChannel().submit()
            .thenCompose(channel -> channel.sendMessage("Zostałeś zweryfikowany!")
                    .setActionRow(Button.secondary("sended", "Wiadomość wysłana przez " + (Objects.requireNonNull(event.getGuild())).getName()).asDisabled()).submit())
            .whenComplete((message, error) -> {});
  }
}
