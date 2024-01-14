package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model;

import net.dv8tion.jda.api.entities.Role;


public class PartialPost {

    private final String raidName;
    private final Long postChannel;
    private final Long reminderChannel;
    private final Role role;
    private final String organiser;
    private final Integer minRaiders;

    public PartialPost(String raidname, Long postChannel, Long reminderChannel, Role role, String organiser, Integer minRaiders) {
        this.raidName = raidname;
        this.postChannel = postChannel;
        this.reminderChannel = reminderChannel;
        this.role = role;
        this.organiser = organiser;
        this.minRaiders = minRaiders;
    }

    public String getRaidName() {
        return raidName;
    }

    public Long getPostChannel() {
        return postChannel;
    }

    public Long getReminderChannel() {
        return reminderChannel;
    }

    public Role getRole() {
        return role;
    }

    public String getOrganiser() {
        return organiser;
    }

    public Integer getMinRaiders() {
        return minRaiders;
    }
}
