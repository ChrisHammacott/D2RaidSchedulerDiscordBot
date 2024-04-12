package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidInfoService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.PartialPost;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.PartialPostContainer;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.services.RaidPostService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.services.ReminderSchedulerService;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Service
public class ModalService {

    Logger logger = LoggerFactory.getLogger(ModalService.class);
    private final RaidInfoService raidInfoService;
    private final ReminderSchedulerService reminderSchedulerService;
    private final RaidPostService raidPostService;
    private final PartialPostContainer partialPostContainer;

    public ModalService(RaidInfoService raidInfoService, ReminderSchedulerService reminderSchedulerService, RaidPostService raidPostService, PartialPostContainer partialPostContainer) {
        this.raidInfoService = raidInfoService;
        this.reminderSchedulerService = reminderSchedulerService;
        this.raidPostService = raidPostService;
        this.partialPostContainer = partialPostContainer;
    }

    public void postModal(ModalInteractionEvent event) {
        PartialPost partialPost = partialPostContainer.getPartialPost();
        String date = event.getValue("date").getAsString();
        String time = event.getValue("time").getAsString();
        String message = event.getValue("message").getAsString();

        Date dateTime;
        try {
            dateTime = getDateTime(date,time);
        } catch (ParseException e) {
            event.reply(String.format("Unable to process given date [%s] and time [%s] values", date, time)).queue();
            return;
        }
        partialPost.setMessage(message);

        HashMap<RichCustomEmoji, Instant> emojiDateHashMap = new HashMap<>();
        List<RichCustomEmoji> emojiList = event.getGuild().getEmojis();
        RichCustomEmoji emoji = emojiList.get(new Random().nextInt(emojiList.size()));
        emojiDateHashMap.put(emoji, dateTime.toInstant());

        partialPost.setEmojiDateHashMap(emojiDateHashMap);
        partialPost.setMessage(message);

        raidPostService.postRaidListing(partialPost, (messageId) -> {
            RaidInfo raidInfo = new RaidInfo(partialPost.getRaidName(), messageId, partialPost.getPostChannel(), partialPost.getReminderChannel(), partialPost.getMinRaiders(), convertEmojiDateMap(partialPost.getEmojiDateHashMap()));
            raidInfoService.save(raidInfo);
            reminderSchedulerService.scheduleCloseRaidPost(partialPost.getLastDate().toEpochMilli(), messageId);
            logger.info("Raid Post Created");
        });
        event.reply("Raid Post has been queued").queue();
    }

    private Date getDateTime(String date, String time) throws ParseException {
        SimpleDateFormat inputFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK);
        String dateTimeString = date + " " + time;
        return inputFormatter.parse(dateTimeString);
    }

    private Map<Long, Long> convertEmojiDateMap(Map<RichCustomEmoji, Instant> emojiDateMap) {
        Map<Long, Long> newMap = new HashMap<>();
        for (var entry : emojiDateMap.entrySet()) {
            newMap.put(entry.getKey().getIdLong(), entry.getValue().toEpochMilli());
        }
        return newMap;
    }
}
