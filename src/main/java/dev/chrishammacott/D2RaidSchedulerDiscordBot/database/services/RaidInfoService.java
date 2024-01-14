package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.mongoRepositories.RaidInfoRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RaidInfoService {

    private final RaidInfoRepository raidInfoRepository;

    public RaidInfoService(RaidInfoRepository raidInfoRepository) {
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

    public Optional<RaidInfo> getRaidPost(long postId){
        return raidInfoRepository.findRaidInfoByPostId(postId);
    }
}
