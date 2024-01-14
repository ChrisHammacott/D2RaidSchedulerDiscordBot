package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers.model.WebRaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.mongoRepositories.RaidInfoRepository;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services.PostService;
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
            Map<String, String> emojiDateFormattedMap = new HashMap<>();
            for (Map.Entry<Long, Long> entry : raidInfo.getEmojiIdDateMap().entrySet()) {
                emojiDateFormattedMap.put(jda.getEmojiById(entry.getKey()).getName(), PostService.getDateIdentifier(entry.getValue()));
            }
            List<String> userNameList = new ArrayList<>();
            for (long userId : raidInfo.getUserIdList()) {
                userNameList.add(jda.getUserById(userId).getName());
            }
            webRaidInfoList.add(new WebRaidInfo(raidInfo.getId(), raidInfo.getRaidName(), raidInfo.getPostId(), reminderChannel, raidInfo.getMinRaiders(), emojiDateFormattedMap, userNameList));
        }
        return webRaidInfoList;
    }

    public Optional<RaidInfo> getRaidPost(long postId){
        return raidInfoRepository.findRaidInfoByPostId(postId);
    }
}
