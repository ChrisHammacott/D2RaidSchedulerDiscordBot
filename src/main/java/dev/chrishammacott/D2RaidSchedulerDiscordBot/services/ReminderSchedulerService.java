package dev.chrishammacott.D2RaidSchedulerDiscordBot.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidInfoService;
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
    private final RaidInfoService raidInfoService;
    private final Map<Long, ScheduledFuture<?>> scheduledRemindersMap = new HashMap<>();
    private final Map<Long, ScheduledFuture<?>> scheduledClosuresMap = new HashMap<>();
    private final JDA jda;

    public ReminderSchedulerService(RaidInfoService raidInfoService, JDA jda) {
        this.raidInfoService = raidInfoService;
        this.jda = jda;
    }

    public void shutdown() {
        scheduleReminders.shutdown();
    }

    public ScheduledFuture<?> scheduleReminder(Long raidTime, RaidInfo raidInfo) {
        Long reminderTime = raidTime - Instant.now().toEpochMilli() - REMINDER_OFFSET;
        String message = jda.getRoleById(raidInfo.getRoleId()).getAsMention() + " - Raid in " + "<t:" + (raidTime/1000) + ":R>";
        Runnable task = () -> {
            jda.getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(message).queue();
        };
        ScheduledFuture<?> scheduledFuture = scheduleReminders.schedule(task, reminderTime, TimeUnit.MILLISECONDS);
        scheduledRemindersMap.put(raidInfo.getPostId(), scheduledFuture);
        return scheduledFuture;
    }

    public ScheduledFuture<?> scheduleCloseRaidPost(Long raidTime, RaidInfo raidInfo) {
        Long closureTime = raidTime - Instant.now().toEpochMilli() + REMINDER_OFFSET;
        Runnable task = () -> {
            raidInfoService.deleteByPostId(raidInfo.getPostId());
            if (jda.getRoleById(raidInfo.getRoleId()) != null) {
                jda.getRoleById(raidInfo.getRoleId()).delete().queue();
            }
        };
        ScheduledFuture<?> scheduledFuture = scheduleReminders.schedule(task, closureTime, TimeUnit.MILLISECONDS);
        scheduledClosuresMap.put(raidInfo.getPostId(), scheduledFuture);
        return scheduledFuture;
    }

    public void cancelReminder(Long postId) {
        scheduledRemindersMap.get(postId).cancel(true);
    }

    public void cancelClosure(Long postId) {
        scheduledClosuresMap.get(postId).cancel(false);
    }
}
