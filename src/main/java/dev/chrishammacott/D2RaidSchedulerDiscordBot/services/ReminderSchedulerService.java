package dev.chrishammacott.D2RaidSchedulerDiscordBot.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.ReminderRecord;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidPostService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.ReminderService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.RaidPost;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ReminderSchedulerService {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${reminder.offset}")
    private Long REMINDER_OFFSET;
    private final ScheduledExecutorService scheduleReminders = Executors.newSingleThreadScheduledExecutor();
    private final RaidPostService raidPostService;
    private final ReminderService reminderService;
    private final Map<Long, ScheduledFuture<?>> scheduledRemindersMap = new HashMap<>();
    private final Map<Long, ScheduledFuture<?>> scheduledClosuresMap = new HashMap<>();
    private final JDA jda;

    public ReminderSchedulerService(RaidPostService raidPostService, ReminderService reminderService, JDA jda) {
        this.raidPostService = raidPostService;
        this.reminderService = reminderService;
        this.jda = jda;
    }

    public void shutdown() {
        scheduleReminders.shutdown();
    }

    public ScheduledFuture<?> scheduleReminder(Long raidTime, RaidPost raidPost) {
        Long reminderTime = raidTime - Instant.now().toEpochMilli() - REMINDER_OFFSET;
        String message = raidPost.getAssignedRaidRole().getAsMention() + " - Raid in " + "<t:" + (raidTime/1000) + ":R>";
        Runnable task = () -> {
            jda.getChannelById(TextChannel.class, raidPost.getDiscordReminderChannel()).sendMessage(message).queue();
        };
        ScheduledFuture<?> scheduledFuture = scheduleReminders.schedule(task, reminderTime, TimeUnit.MILLISECONDS);
        scheduledRemindersMap.put(raidPost.getDiscordPostId(), scheduledFuture);
        reminderService.insert(new ReminderRecord(raidPost.getDiscordPostId(), raidPost.getDiscordReminderChannel(), reminderTime, message));
        return scheduledFuture;
    }

    public ScheduledFuture<?> scheduleCloseRaidPost(Long raidTime, RaidPost raidPost) {
        Long closureTime = raidTime - Instant.now().toEpochMilli() + REMINDER_OFFSET;
        Runnable task = () -> {
            raidPostService.deleteByPostId(raidPost.getDiscordPostId());
            reminderService.deleteByPostId(raidPost.getDiscordPostId());
            raidPost.getAssignedRaidRole().delete().queue();
        };
        ScheduledFuture<?> scheduledFuture = scheduleReminders.schedule(task, closureTime, TimeUnit.MILLISECONDS);
        scheduledClosuresMap.put(raidPost.getDiscordPostId(), scheduledFuture);
        return scheduledFuture;
    }

    public void cancelReminder(Long postId) {
        scheduledRemindersMap.get(postId).cancel(true);
    }

    public void cancelClosure(Long postId) {
        scheduledClosuresMap.get(postId).cancel(false);
    }
}
