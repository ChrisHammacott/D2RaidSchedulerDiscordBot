package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.mongoRepositories;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.ReminderRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReminderRepository extends MongoRepository<ReminderRecord, String> {

    void deleteReminderRecordByDiscordPostId(Long discordPostId);

}
