package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.mongoRepositories;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RaidInfoRepository extends MongoRepository<RaidInfo, String> {

    Optional<RaidInfo> findRaidInfoByPostId(long postId);
    void deleteRaidInfoByPostId(long postId);
}
