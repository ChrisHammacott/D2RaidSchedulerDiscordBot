package dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers.model;

import java.util.List;
import java.util.Map;

public class WebRaidInfo {

    private final String id;
    private final String raidName;
    private final long postId;
    private final String reminderChannel;
    private final int minRaiders;
    private final Map<String, String> emojiDateMap;
    private final List<String> userNameList;

    public WebRaidInfo(String id, String raidName, long postId, String reminderChannel, int minRaiders, Map<String, String> emojiDateMap, List<String> userNameList) {
        this.id = id;
        this.raidName = raidName;
        this.postId = postId;
        this.reminderChannel = reminderChannel;
        this.minRaiders = minRaiders;
        this.emojiDateMap = emojiDateMap;
        this.userNameList = userNameList;
    }

    public String getId() {
        return id;
    }

    public String getRaidName() {
        return raidName;
    }

    public long getPostId() {
        return postId;
    }

    public String getReminderChannel() {
        return reminderChannel;
    }

    public int getMinRaiders() {
        return minRaiders;
    }

    public Map<String, String> getEmojiDateMap() {
        return emojiDateMap;
    }

    public List<String> getUserNameList() {
        return userNameList;
    }
}
