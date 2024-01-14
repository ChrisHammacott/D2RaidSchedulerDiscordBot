package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.RaidPost;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document
public class RaidPostRecord {

    @Id
    private String id;
    private final Long discordPostId;
    private final Long discordReminderChannel;
    private final Integer minimumRaiders;
    private final Long assignedRole;
    private final Map<Long,Long> emojiIdDateMap;

    @PersistenceCreator
    public RaidPostRecord(String id, Long discordPostId, Long discordReminderChannel, Integer minimumRaiders, Long assignedRole, Map<Long, Long> emojiIdDateMap) {
        this.id = id;
        this.discordPostId = discordPostId;
        this.discordReminderChannel = discordReminderChannel;
        this.minimumRaiders = minimumRaiders;
        this.assignedRole = assignedRole;
        this.emojiIdDateMap = emojiIdDateMap;
    }

    public RaidPostRecord(RaidPost raidPost) {
        this.id = raidPost.getId();
        this.discordPostId = raidPost.getDiscordPostId();
        this.discordReminderChannel = raidPost.getDiscordReminderChannel();
        this.minimumRaiders = raidPost.getMinimumRaiders();
        if (raidPost.getAssignedRaidRole() != null) {
            this.assignedRole = raidPost.getAssignedRaidRole().getIdLong();
        } else {
            this.assignedRole = null;
        }
        Map<Long, Long> emojiIdDateMap = new HashMap<>();
        raidPost.getEmojiDateMap().forEach((emoji, date) -> emojiIdDateMap.put(emoji.getIdLong(), date.getTime()));
        this.emojiIdDateMap = emojiIdDateMap;
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

    public Long getAssignedRole() {
        return assignedRole;
    }

    public Map<Long, Long> getEmojiIdDateMap() {
        return emojiIdDateMap;
    }
}
