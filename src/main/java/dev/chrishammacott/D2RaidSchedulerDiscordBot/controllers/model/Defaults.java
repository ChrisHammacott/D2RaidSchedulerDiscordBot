package dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers.model;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.Config;

import java.util.Optional;

public class Defaults {

    private long defaultPostChannelId;
    private long defaultReminderChannelId;
    private long defaultRoleId;
    private boolean set = false;

    public Defaults(Optional<Config> configOptional) {
        if (configOptional.isPresent()){
            set = true;
            Config config = configOptional.get();
            this.defaultPostChannelId = config.defaultPostChannel();
            this.defaultReminderChannelId = config.defaultReminderChannel();
            this.defaultRoleId = config.defaultRole();
        } else {
            this.defaultPostChannelId = 0;
            this.defaultReminderChannelId = 0;
            this.defaultRoleId = 0;
        }
    }

    public long getDefaultPostChannelId() {
        return defaultPostChannelId;
    }

    public long getDefaultReminderChannelId() {
        return defaultReminderChannelId;
    }

    public long getDefaultRoleId() {
        return defaultRoleId;
    }

    public boolean isSet() {
        return set;
    }
}
