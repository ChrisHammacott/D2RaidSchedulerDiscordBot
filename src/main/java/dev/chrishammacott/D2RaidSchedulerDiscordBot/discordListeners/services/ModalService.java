package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidInfoService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.PartialPost;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.PartialPostContainer;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.services.ReminderSchedulerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Service
public class ModalService {

    Logger logger = LoggerFactory.getLogger(ModalService.class);
    private final RaidInfoService raidInfoService;
    private final ReminderSchedulerService reminderSchedulerService;
    private final PartialPostContainer partialPostContainer;

    public ModalService(RaidInfoService raidInfoService, ReminderSchedulerService reminderSchedulerService, PartialPostContainer partialPostContainer) {
        this.raidInfoService = raidInfoService;
        this.reminderSchedulerService = reminderSchedulerService;
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

        List<Long> emojiIds = event.getGuild().getEmojis().stream().map(ISnowflake::getIdLong).toList();
        Long emojiId = emojiIds.get(new Random().nextInt(emojiIds.size()));

        RichCustomEmoji emoji = event.getGuild().getEmojiById(emojiId);
        MessageEmbed embedMessage = createEmbedPost(partialPost.getRaidName(), partialPost.getOrganiser(), partialPost.getMinRaiders(), dateTime, message, emoji);
        TextChannel textChannel = event.getGuild().getChannelById(TextChannel.class, partialPost.getPostChannel());
        textChannel.sendMessage(partialPost.getRole().getAsMention()).queue();
        textChannel.sendMessageEmbeds(embedMessage) .queue((messageObject -> {
            messageObject.addReaction(emoji).queue();
            RaidInfo raidInfo = new RaidInfo(partialPost.getRaidName(), messageObject.getIdLong(), partialPost.getPostChannel(), partialPost.getReminderChannel(), partialPost.getMinRaiders(), emojiId, dateTime.getTime());
            raidInfoService.save(raidInfo);
            reminderSchedulerService.scheduleCloseRaidPost(dateTime.getTime(), raidInfo);
            logger.info("Standard Raid Post Created");
        }));

        event.reply("Raid Post has been queued").queue();
    }

    private Date getDateTime(String date, String time) throws ParseException {
        SimpleDateFormat inputFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK);
        String dateTimeString = date + " " + time;
        return inputFormatter.parse(dateTimeString);
    }

    private MessageEmbed createEmbedPost(String raidName, String organiser, int minRaiders, Date dateTime, String message, RichCustomEmoji emoji){
        EmbedBuilder embedBuilder = createEmbed(raidName, "<t:" + dateTime.toInstant().getEpochSecond() + ":R>", organiser, minRaiders, formatDateTime(dateTime), message);
        embedBuilder.addField("","React: " + emoji.getFormatted(),false);
        return  embedBuilder.build();
    }

    private MessageEmbed createEmbedPost(String raidName, String organiser, int minRaiders, String message, Map<RichCustomEmoji, Date> emojiDateMap){
        EmbedBuilder embedBuilder = createEmbed(raidName, "TBD", organiser, minRaiders, "TBD", message);
        embedBuilder.addField("Vote on Date and Time:", getVoteField(emojiDateMap),false);
        return  embedBuilder.build();
    }

    private EmbedBuilder createEmbed(String raidName, String description, String organiser, int minRaiders, String dateTime, String message) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Raid Incoming!");
        embedBuilder.setDescription(description);
        embedBuilder.setColor(Color.orange);
        embedBuilder.setThumbnail("https://i.insider.com/5800ec6c52dd73d0018b4e21?width=750&format=jpeg&auto=webp");
        embedBuilder.addField("Raid", raidName, false);
        embedBuilder.addField("Organiser", organiser, true);
        embedBuilder.addField("Min Raiders", minRaiders + " peeps", true);
        embedBuilder.addField("Date and Time", dateTime, false);
        embedBuilder.addField("Call to Arms", message, false);
        return  embedBuilder;
    }

    private String getVoteField(Map<RichCustomEmoji, Date> emojiDateMap) {
        ArrayList<String> lines = new ArrayList<>();
        emojiDateMap.forEach((emoji, date) -> lines.add(emoji.getFormatted() + " - " + formatDateTime(date)));
        return String.join("\n", lines);
    }

    private String formatDateTime(Date dateTime) {
        SimpleDateFormat outputFormatterDate = new SimpleDateFormat("EEEE, dd MMM", Locale.UK);
        String date = outputFormatterDate.format(dateTime);
        SimpleDateFormat outputFormatterTime = new SimpleDateFormat("h:mma", Locale.UK);
        String time = outputFormatterTime.format(dateTime);
        return date + " at " + time;
    }
}
