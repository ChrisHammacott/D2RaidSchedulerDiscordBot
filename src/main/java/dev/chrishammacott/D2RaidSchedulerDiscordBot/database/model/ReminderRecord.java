package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public record ReminderRecord(@Id String id, Long discordPostId, Long discordReminderChannel, Long reminderDateTime, String reminderMessage) {

    public ReminderRecord(Long discordPostId, Long discordReminderChannel, Long reminderDateTime, String reminderMessage){
        this(null, discordPostId, discordReminderChannel, reminderDateTime, reminderMessage);
    }
}
