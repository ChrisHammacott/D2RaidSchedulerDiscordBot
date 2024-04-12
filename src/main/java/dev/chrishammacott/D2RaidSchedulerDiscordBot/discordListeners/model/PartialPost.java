package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

import java.time.Instant;
import java.util.*;


public class PartialPost {

    private final String raidName;
    private final long postChannel;
    private final long reminderChannel;
    private final Role mentionRole;
    private final String organiser;
    private final int minRaiders;
    private HashMap<RichCustomEmoji, Instant> emojiDateHashMap;
    private String message;

    public PartialPost(String raidName, Long postChannel, Long reminderChannel, Role mentionRole, String organiser, Integer minRaiders, HashMap<RichCustomEmoji, Instant> emojiDateHashMap, String message) {
        this.raidName = raidName;
        this.postChannel = postChannel;
        this.reminderChannel = reminderChannel;
        this.mentionRole = mentionRole;
        this.organiser = organiser;
        this.minRaiders = minRaiders;
        this.emojiDateHashMap = emojiDateHashMap;
        this.message = message;
    }

    public PartialPost(String raidName, long postChannel, long reminderChannel, Role mentionRole, String organiser, int minRaiders) {
        this.raidName = raidName;
        this.postChannel = postChannel;
        this.reminderChannel = reminderChannel;
        this.mentionRole = mentionRole;
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

    public Role getMentionRole() {
        return mentionRole;
    }

    public String getOrganiser() {
        return organiser;
    }

    public Integer getMinRaiders() {
        return minRaiders;
    }

    public HashMap<RichCustomEmoji, Instant> getEmojiDateHashMap() {
        return emojiDateHashMap;
    }

    public String getMessage() {
        return message;
    }

    public Instant getLastDate() {
        List<Instant> list = new ArrayList<>(emojiDateHashMap.values());
        Instant lastDate = Instant.ofEpochMilli(0);
        for (Instant date : list) {
            if (date.isAfter(lastDate)) {
                lastDate = date;
            }
        }
        return lastDate;
    }

    public void setEmojiDateHashMap(HashMap<RichCustomEmoji, Instant> emojiDateHashMap) {
        this.emojiDateHashMap = emojiDateHashMap;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
