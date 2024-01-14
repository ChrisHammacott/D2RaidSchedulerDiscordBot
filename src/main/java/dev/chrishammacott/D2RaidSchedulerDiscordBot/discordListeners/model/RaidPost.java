package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidPostRecord;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

import java.util.Date;
import java.util.Map;

public class RaidPost {

    private String id;
    private final Long discordPostId;
    private final Long discordReminderChannel;
    private final Integer minimumRaiders;
    private final Map<RichCustomEmoji, Date> emojiDateMap;
    private Role assignedRaidRole = null;

    public RaidPost(Long discordPostId, Long discordReminderChannel, Integer minimumRaiders, Map<RichCustomEmoji, Date> emojiDateMap) {
        this.discordPostId = discordPostId;
        this.discordReminderChannel = discordReminderChannel;
        this.minimumRaiders = minimumRaiders;
        this.emojiDateMap = emojiDateMap;
    }

    public RaidPost(RaidPostRecord raidPostRecord, Role assignedRaidRole, Map<RichCustomEmoji, Date> emojiDateMap) {
        this.id = raidPostRecord.getId();
        this.discordPostId = raidPostRecord.getDiscordPostId();
        this.discordReminderChannel = raidPostRecord.getDiscordReminderChannel();
        this.minimumRaiders = raidPostRecord.getMinimumRaiders();
        this.emojiDateMap = emojiDateMap;
        this.assignedRaidRole = assignedRaidRole;
    }

    public String getId() {
        return id;
    }

    public Long getDiscordPostId() {
        return discordPostId;
    }

    public Long getDiscordReminderChannel() {
        return discordReminderChannel;
    }

    public Integer getMinimumRaiders() {
        return minimumRaiders;
    }

    public Date getDate(Emoji emoji) {
        for (Map.Entry<RichCustomEmoji, Date> entry : emojiDateMap.entrySet()) {
            if (emoji.getName().equals(entry.getKey().getName())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Map<RichCustomEmoji, Date> getEmojiDateMap() {
        return emojiDateMap;
    }

    public Role getAssignedRaidRole() {
        return assignedRaidRole;
    }

    public void setAssignedRaidRole(Role assignedRaidRole) {
        this.assignedRaidRole = assignedRaidRole;
    }

    public boolean isRaidActive() {
        return assignedRaidRole != null;
    }

    public boolean hasVote() {
        return emojiDateMap.size() > 1;
    }

    public boolean isRegisteredEmoji(Emoji eventEmoji) {
        for (Map.Entry<RichCustomEmoji, Date> entry : emojiDateMap.entrySet()) {
            if (eventEmoji.getName().equals(entry.getKey().getName())) {
                return true;
            }
        }
        return false;
    }
}
