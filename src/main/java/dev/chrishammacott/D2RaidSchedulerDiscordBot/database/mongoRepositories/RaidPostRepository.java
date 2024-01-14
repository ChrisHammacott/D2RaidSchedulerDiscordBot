package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.mongoRepositories;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidPostRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RaidPostRepository extends MongoRepository<RaidPostRecord, String> {

    Optional<RaidPostRecord> findRaidPostRecordByDiscordPostId(Long discordPostId);
    void deleteRaidPostByDiscordPostId(Long discordPostId);
}
