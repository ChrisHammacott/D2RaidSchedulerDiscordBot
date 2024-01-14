package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services.ModalService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ModalListener extends ListenerAdapter {

    Logger logger = LoggerFactory.getLogger(ModalListener.class);
    private final ModalService modalService;

    public ModalListener(JDA jda, ModalService modalService) {
        jda.addEventListener(this);
        this.modalService = modalService;
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("post_modal")){
            logger.info("Modal Post event triggered");
            modalService.postModal(event);
        }
        if (event.getModalId().equals("post_vote_modal")){
            logger.info("Modal Post Vote event triggered");
            modalService.postVoteModal(event);
        }
    }

    public static Modal getPostVoteModal() {
        TextInput dateTimeInput = TextInput.create("date_time", "Dates and Times", TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(33,70)
                .setPlaceholder("dd/MM/yyyy@HH:mm - e.g. 30/10/23@13:10")
                .build();

        return Modal.create("post_vote_modal", "Raid Scheduler")
                .addActionRow(dateTimeInput)
                .addActionRow(getMessageSection())
                .build();
    }

    public static Modal getPostModal() {
        TextInput dateInput = TextInput.create("date", "Date", TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(10,10)
                .setPlaceholder("dd/mm/yyyy - e.g. 30/10/23")
                .build();
        TextInput timeInput = TextInput.create("time", "Time", TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(5,5)
                .setPlaceholder("HH:mm - e.g. 19:30 (24 hour clock)")
                .build();

        return Modal.create("post_modal", "Raid Scheduler")
                .addActionRow(dateInput)
                .addActionRow(timeInput)
                .addActionRow(getMessageSection())
                .build();
    }

    private static TextInput getMessageSection() {
        return TextInput.create("message", "Message", TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setMinLength(5)
                .setValue("Hey All! ")
                .build();
    }
}
