package dev.chrishammacott.D2RaidSchedulerDiscordBot.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidInfoService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class StartUpService {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JDA jda;
    private final RaidInfoService raidInfoService;
    private final ReminderSchedulerService reminderSchedulerService;

    public StartUpService(JDA jda, RaidInfoService raidInfoService, ReminderSchedulerService reminderSchedulerService) {
        this.jda = jda;
        this.raidInfoService = raidInfoService;
        this.reminderSchedulerService = reminderSchedulerService;
    }

//    @EventListener(ApplicationReadyEvent.class)
//    public void retrackRaidPosts() {
//        logger.info("Re-tracking stored raid posts");
//        List<RaidInfo> raidInfoList = raidInfoService.getAllPosts();
//        for (RaidInfo raidInfo : raidInfoList) {
//            if (raidInfo.getDateTime() < Instant.now().toEpochMilli()) {
//                raidInfoService.deleteByPostId(raidInfo.getPostId());
//                continue;
//            }
//            jda.getChannelById(TextChannel.class, raidInfo.getPostChannelId()).retrieveMessageById(raidInfo.getPostId()).queue(message -> {
//                // removes all other reactions
//                Emoji emoji = null;
//                for (MessageReaction reaction : message.getReactions()) {
//                    try {
//                        if (reaction.getEmoji().asCustom().getIdLong() != raidInfo.getEmojiId()) {
//                            message.removeReaction(reaction.getEmoji()).queue();
//                        } else {
//                            emoji = reaction.getEmoji();
//                        }
//                    } catch (IllegalStateException e) {
//                        message.removeReaction(reaction.getEmoji()).queue();
//                    }
//                }
//                //checks to see if user list is the same on post as is stored
//
//
//            });
//        }
//    }
}
