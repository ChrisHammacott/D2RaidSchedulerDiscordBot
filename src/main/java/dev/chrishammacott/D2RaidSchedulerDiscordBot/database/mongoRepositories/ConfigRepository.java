package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.mongoRepositories;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.Config;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigRepository extends MongoRepository<Config, String> {
}
