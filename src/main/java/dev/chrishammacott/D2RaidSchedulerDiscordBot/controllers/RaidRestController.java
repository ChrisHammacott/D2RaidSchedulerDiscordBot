package dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers.model.CreateRaidInfoFormData;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.ConfigService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidInfoService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.PartialPost;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.services.RaidPostService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.services.ReminderSchedulerService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Controller
public class RaidRestController {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JDA jda;
    private final RaidInfoService raidInfoService;
    private final RaidPostService raidPostService;
    private final ReminderSchedulerService reminderSchedulerService;
    private final ConfigService configService;

    public RaidRestController(JDA jda, RaidInfoService raidInfoService, RaidPostService raidPostService, ReminderSchedulerService reminderSchedulerService, ConfigService configService) {
        this.jda = jda;
        this.raidInfoService = raidInfoService;
        this.raidPostService = raidPostService;
        this.reminderSchedulerService = reminderSchedulerService;
        this.configService = configService;
    }

    @HxRequest
    @PostMapping("/raid")
    public HtmxResponse createNewRaid(@ModelAttribute CreateRaidInfoFormData formData, Model model) {
        PartialPost partialPost = convertToPartialPost(formData);
        raidPostService.postRaidListing(partialPost, (messageId) -> {
            RaidInfo raidInfo = new RaidInfo(partialPost.getRaidName(), messageId, partialPost.getPostChannel(), partialPost.getReminderChannel(), partialPost.getMinRaiders(), convertEmojiDateMap(partialPost.getEmojiDateHashMap()));
            raidInfoService.save(raidInfo);
            reminderSchedulerService.scheduleCloseRaidPost(partialPost.getLastDate().toEpochMilli(), messageId);
            logger.info("Raid Post Created");
        });
        return RaidViewController.createRaidForm(jda, model, configService.getConfig()).trigger("updateRaidList").build();
    }

    private PartialPost convertToPartialPost(CreateRaidInfoFormData formData) {
        Role role = jda.getRoleById(formData.getMentionRoleId());
        String organiser = jda.getUserById(formData.getUserId()).getAsMention();
        HashMap<RichCustomEmoji, Instant> emojiDateHashMap = new HashMap<>();
        List<RichCustomEmoji> emojiList = new ArrayList<>(jda.getEmojis());
        for (LocalDateTime localDateTime : formData.getDateTimeList()) {
            RichCustomEmoji emoji = emojiList.get(new Random().nextInt(emojiList.size()));
            emojiDateHashMap.put(emoji, localDateTime.atZone(ZoneId.of("Europe/London")).toInstant());
            emojiList.remove(emoji);
        }
        return new PartialPost(formData.getRaidName(), formData.getPostChannelId(), formData.getReminderChannelId(), role, organiser, formData.getMinRaiders(), emojiDateHashMap, formData.getMessage());
    }

    private Map<Long, Long> convertEmojiDateMap(Map<RichCustomEmoji, Instant> emojiDateMap) {
        Map<Long, Long> newMap = new HashMap<>();
        for (var entry : emojiDateMap.entrySet()) {
            newMap.put(entry.getKey().getIdLong(), entry.getValue().toEpochMilli());
        }
        return newMap;
    }
}
