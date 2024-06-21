/*
 * Author spacenough 2024.
 */

package pl.codelen.edutor.bot.ticket;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import pl.codelen.edutor.bot.Bot;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TicketListener extends ListenerAdapter {

  @Override
  public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
    if (event.getComponentId().equalsIgnoreCase("ticketID")) {
      TextInput problem = TextInput.create("ticket-problem", "Problem/Pytanie", TextInputStyle.PARAGRAPH)
              .setPlaceholder("Napisz swój problem, lub pytanie")
              .setMinLength(8)
              .setMaxLength(512)
              .setRequired(true)
              .build();

      Modal ticket = Modal.create("ticket", "Ticket " + Objects.requireNonNull(event.getMember()).getUser().getName())
              .addComponents( ActionRow.of(problem))
              .build();
      event.replyModal(ticket).queue();
    }

    if (event.getComponentId().equalsIgnoreCase("closeTicketID")) {
      event.getChannel().delete().queueAfter(3L, TimeUnit.SECONDS);
      event.reply("Ticket zostal zamkniety!").queue();
    }
  }

  @Override
  public void onModalInteraction(@NotNull ModalInteractionEvent event) {
    if (event.getModalId().equals("ticket")) {
      String body = Objects.requireNonNull(event.getValue("ticket-problem")).getAsString();
      String name = "ticket-" + event.getUser().getName();

      Objects.requireNonNull(event.getMember()).getUser().openPrivateChannel().submit()
              .thenCompose(channel -> channel.sendMessage("Ticket został utworzony")
                      .setActionRow(Button.secondary("sended", "Wiadomosc wyslana przez " + (Objects.requireNonNull(event.getGuild())).getName()).asDisabled()).submit())
              .whenComplete((message, error) -> {});

      TextChannel ticketChannel = (Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getCategoryById(Bot.getInstance().getConfigFile().getString("ticket-category-id")))).createTextChannel(name).complete();

      ticketChannel.upsertPermissionOverride((event.getMember())).setAllowed(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND).queue();
      ticketChannel.getManager().setSlowmode(5).complete();

      EmbedBuilder embed = new EmbedBuilder();
      embed.setTitle("Ticket " + event.getMember().getUser().getName());
      embed.addField("Treść ticketa", body, false);

      ticketChannel.sendMessageEmbeds(embed.build()).setActionRow(Button.danger("closeTicketID", "Zamknij ticket")).queue();
      event.deferEdit().queue();
    }
  }
}