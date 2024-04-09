package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

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
    private final Map<Long, Long> emojiDateTimeMap;
    private final Map<Long, List<Long>> emojiUserListMap;

    @PersistenceCreator
    public RaidInfo(String id, String raidName, long postId, long postChannelId, long reminderChannelId, int minRaiders, Long roleId, HashMap<Long, Long> emojiDateTimeMap, Map<Long, List<Long>> emojiUserListMap) {
        this.id = id;
        this.raidName = raidName;
        this.postId = postId;
        this.postChannelId = postChannelId;
        this.reminderChannelId = reminderChannelId;
        this.minRaiders = minRaiders;
        this.roleId = roleId;
        this.emojiDateTimeMap = emojiDateTimeMap;
        this.emojiUserListMap = emojiUserListMap;
    }

    public RaidInfo(String raidName, long postId, long postChannelId, long reminderChannelId, int minRaiders, Map<Long, Long> emojiDateTimeMap) {
        this.raidName = raidName;
        this.postId = postId;
        this.postChannelId = postChannelId;
        this.reminderChannelId = reminderChannelId;
        this.minRaiders = minRaiders;
        this.emojiDateTimeMap = emojiDateTimeMap;
        emojiUserListMap = new HashMap<>();
        for (var entry : emojiDateTimeMap.entrySet()) {
            emojiUserListMap.put(entry.getKey(), new ArrayList<>());
        }
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

    public Map<Long, Long> getEmojiDateTimeMap() {
        return emojiDateTimeMap;
    }

    public Map<Long, List<Long>> getEmojiUserListMap() {
        return emojiUserListMap;
    }

    public boolean hasVote() {
        return emojiDateTimeMap.size() > 1;
    }

    public boolean isRegisteredEmoji(long emojiId) {
        return emojiDateTimeMap.containsKey(emojiId);
    }

    public long getDateTime(long emojiId) {
        return emojiDateTimeMap.get(emojiId);
    }

    public List<Long> getUserIdList(long emojiId) {
        return emojiUserListMap.get(emojiId);
    }

    public void addUser(long emojiId, long userId) {
        emojiUserListMap.get(emojiId).add(userId);
    }

    public void removeUser(long emojiId, long userId) {
        emojiUserListMap.get(emojiId).remove(userId);
    }
}
