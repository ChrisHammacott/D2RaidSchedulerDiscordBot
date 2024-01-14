package dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers.model;

import java.util.List;

public record WebRaidInfo(String id, String raidName, long postId, String reminderChannel, int minRaiders,
                          String dateTime, List<String> userNameList) {

}
