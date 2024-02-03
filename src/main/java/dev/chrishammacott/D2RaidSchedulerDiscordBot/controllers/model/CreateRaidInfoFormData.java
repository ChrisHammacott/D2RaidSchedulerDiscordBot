package dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreateRaidInfoFormData {

    private String raidName;
    private long userId;
    private int minRaiders;
    private long postChannelId;
    private long reminderChannelId;
    private long mentionRoleId;

    private List<LocalDateTime> dateTimeList;
    private String message;

    public String getRaidName() {
        return raidName;
    }

    public void setRaidName(String raidName) {
        this.raidName = raidName;
    }

    public List<LocalDateTime> getDateTimeList() {
        return dateTimeList;
    }

    public void setDateTimeList(List<LocalDateTime> dateTimeList) {
        this.dateTimeList = dateTimeList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMinRaiders() {
        return minRaiders;
    }

    public void setMinRaiders(int minRaiders) {
        this.minRaiders = minRaiders;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getPostChannelId() {
        return postChannelId;
    }

    public void setPostChannelId(long postChannelId) {
        this.postChannelId = postChannelId;
    }

    public long getReminderChannelId() {
        return reminderChannelId;
    }

    public void setReminderChannelId(long reminderChannelId) {
        this.reminderChannelId = reminderChannelId;
    }

    public long getMentionRoleId() {
        return mentionRoleId;
    }

    public void setMentionRoleId(long mentionRoleId) {
        this.mentionRoleId = mentionRoleId;
    }
}
