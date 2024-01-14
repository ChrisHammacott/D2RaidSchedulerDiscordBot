package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.ReminderRecord;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.mongoRepositories.ReminderRepository;
import org.springframework.stereotype.Service;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;

    public ReminderService(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    public void insert(ReminderRecord reminderRecord) {
        reminderRepository.insert(reminderRecord);
    }

    public void delete(String postId) {
        reminderRepository.deleteById(postId);
    }

    public void deleteByPostId(Long postId) {
        reminderRepository.deleteReminderRecordByDiscordPostId(postId);
    }
}
