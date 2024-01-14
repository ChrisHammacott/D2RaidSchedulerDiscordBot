package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.Config;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.mongoRepositories.ConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigService {

    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public Optional<Config> getConfig() {
        return configRepository.findAll().stream().findFirst();
    }

    public void upsertConfig(Config config) {
        Optional<Config> result = configRepository.findAll().stream().findFirst();
        configRepository.insert(config);
        result.ifPresent(value -> configRepository.deleteById(value.id()));
    }
}
