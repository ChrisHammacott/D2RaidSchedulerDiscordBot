package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public record Config(@Id String id, Long defaultPostChannel, Long defaultReminderChannel, Long defaultRole) {

    public Config(Long defaultPostChannel, Long defaultReminderChannel, Long defaultRole){
        this(null, defaultPostChannel, defaultReminderChannel, defaultRole);
    }
}
