package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document
public class RaidInfo {

    @Id
    private String id;
    private final String raidName;
    private final long postId;
    private final long reminderChannelId;
    private final int minRaiders;
    private Long roleId;
    private final Map<Long,Long> emojiIdDateMap;
    private final List<Long> userIdList;

    @PersistenceCreator
    public RaidInfo(String id, String raidName, long postId, long reminderChannelId, int minRaiders, Long roleId, Map<Long, Long> emojiIdDateMap, List<Long> userIdList) {
        this.id = id;
        this.raidName = raidName;
        this.postId = postId;
        this.reminderChannelId = reminderChannelId;
        this.minRaiders = minRaiders;
        this.roleId = roleId;
        this.emojiIdDateMap = emojiIdDateMap;
        this.userIdList = userIdList;
    }

    public RaidInfo(String raidName, long postId, long reminderChannelId, int minRaiders, Map<Long, Long> emojiIdDateMap) {
        this.raidName = raidName;
        this.postId = postId;
        this.reminderChannelId = reminderChannelId;
        this.minRaiders = minRaiders;
        this.emojiIdDateMap = emojiIdDateMap;
        userIdList = new ArrayList<>();
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

    public long getReminderChannelId() {
        return reminderChannelId;
    }

    public int getMinRaiders() {
        return minRaiders;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Map<Long, Long> getEmojiIdDateMap() {
        return emojiIdDateMap;
    }

    public Long getDate(long emojiId) {
        return emojiIdDateMap.get(emojiId);
    }

    public boolean hasVote() {
        return emojiIdDateMap.size() > 1;
    }

    public boolean isRegisteredEmoji(long emoji) {
        return emojiIdDateMap.containsKey(emoji);
    }

    public List<Long> getUserIdList() {
        return userIdList;
    }

    public void addUser(long userId) {
        userIdList.add(userId);
    }

    public void removeUser(long userId) {
        userIdList.remove(userId);
    }
}
