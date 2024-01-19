package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class RaidInfo {

    @Id
    private String id;
    private final String raidName;
    private final long postId;
    private final long postChannelId;
    private final long reminderChannelId;
    private final int minRaiders;
    private Long roleId;
    private final long emojiId;
    private final long dateTime;
    private final List<Long> userIdList;

    @PersistenceCreator
    public RaidInfo(String id, String raidName, long postId, long postChannelId, long reminderChannelId, int minRaiders, Long roleId, long emojiId, long dateTime, List<Long> userIdList) {
        this.id = id;
        this.raidName = raidName;
        this.postId = postId;
        this.postChannelId = postChannelId;
        this.reminderChannelId = reminderChannelId;
        this.minRaiders = minRaiders;
        this.roleId = roleId;
        this.emojiId = emojiId;
        this.dateTime = dateTime;
        this.userIdList = userIdList;
    }

    public RaidInfo(String raidName, long postId, long postChannelId, long reminderChannelId, int minRaiders, long emojiId, long dateTime) {
        this.raidName = raidName;
        this.postId = postId;
        this.postChannelId = postChannelId;
        this.reminderChannelId = reminderChannelId;
        this.minRaiders = minRaiders;
        this.emojiId = emojiId;
        this.dateTime = dateTime;
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

    public long getPostChannelId() {
        return postChannelId;
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

    public long getEmojiId() {
        return emojiId;
    }

    public long getDateTime() {
        return dateTime;
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
