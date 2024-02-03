package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers.model.WebRaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.mongoRepositories.RaidInfoRepository;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services.ReactionPostService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RaidInfoService {

    private final JDA jda;
    private final RaidInfoRepository raidInfoRepository;

    public RaidInfoService(JDA jda, RaidInfoRepository raidInfoRepository) {
        this.jda = jda;
        this.raidInfoRepository = raidInfoRepository;
    }

    public void save(RaidInfo raidInfo) {
        raidInfoRepository.save(raidInfo);
    }

    public void delete(String objectId) {
        raidInfoRepository.deleteById(objectId);
    }

    public void deleteByPostId(long postId) {
        raidInfoRepository.deleteRaidInfoByPostId(postId);
    }

    public List<RaidInfo> getAllPosts() {
        return raidInfoRepository.findAll();
    }

    public List<WebRaidInfo> getAllFormattedPosts() {
        List<RaidInfo> raidInfoList = raidInfoRepository.findAll();
        List<WebRaidInfo> webRaidInfoList = new ArrayList<>();
        for (RaidInfo raidInfo : raidInfoList) {
            String reminderChannel = jda.getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).getName();
            String dateTime;
            List<String> userNameList;
            if (raidInfo.hasVote()) {
                dateTime = "N/A";
                userNameList = userListsToStringWithDate(raidInfo.getEmojiDateTimeMap(), raidInfo.getEmojiUserListMap());
            } else {
                dateTime = ReactionPostService.getDateIdentifier(raidInfo.getEmojiDateTimeMap().entrySet().stream().findFirst().get().getValue());
                userNameList = userListToString(raidInfo.getEmojiUserListMap());
            }
            webRaidInfoList.add(new WebRaidInfo(raidInfo.getId(), raidInfo.getRaidName(), raidInfo.getPostId(), reminderChannel, raidInfo.getMinRaiders(), dateTime, userNameList));
        }
        return webRaidInfoList;
    }

    private List<String> userListsToStringWithDate(Map<Long, Long> emojiDateTimeMap, Map<Long, List<Long>> emojiUserListMap) {
        List<String> userList = new ArrayList<>();
        for (var entry : emojiUserListMap.entrySet()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(ReactionPostService.getDateIdentifier(emojiDateTimeMap.get(entry.getKey())));
            for (int i = 0; i < entry.getValue().size(); i++) {
                stringBuilder.append(" | ");
                stringBuilder.append(jda.getUserById(entry.getValue().get(i)).getName());
            }
            userList.add(stringBuilder.toString());
        }
        return userList;
    }

    private List<String> userListToString(Map<Long, List<Long>> emojiUserListMap) {
        List<String> userList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (var entry : emojiUserListMap.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                stringBuilder.append(jda.getUserById(entry.getValue().get(i)).getName());
                if (i != entry.getValue().size()-1) {
                    stringBuilder.append(" | ");
                }
            }
            userList.add(stringBuilder.toString());
        }
        return userList;
    }

    public Optional<RaidInfo> getRaidPost(long postId){
        return raidInfoRepository.findRaidInfoByPostId(postId);
    }
}
