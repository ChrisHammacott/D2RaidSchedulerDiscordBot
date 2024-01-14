package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidPostService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.PartialPost;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.PartialPostContainer;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.RaidPost;
import net.dv8tion.jda.api.EmbedBuilder;
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
    private final RaidPostService raidPostService;
    private final PartialPostContainer partialPostContainer;

    public ModalService(RaidPostService raidPostService, PartialPostContainer partialPostContainer) {
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

        List<RichCustomEmoji> emojiList = event.getGuild().getEmojis();
        RichCustomEmoji emoji = emojiList.get(new Random().nextInt(emojiList.size()));
        HashMap<RichCustomEmoji, Date> emojiDateHashMap = new HashMap<>();
        emojiDateHashMap.put(emoji, dateTime);

        MessageEmbed embedMessage = createEmbedPost(partialPost.getRaidName(), partialPost.getOrganiser(), partialPost.getMinRaiders(), dateTime, message, emoji);
        TextChannel textChannel = event.getGuild().getChannelById(TextChannel.class, partialPost.getPostChannel());
        textChannel.sendMessage(partialPost.getRole().getAsMention()).queue();
        textChannel.sendMessageEmbeds(embedMessage) .queue((messageObject -> {
            messageObject.addReaction(emoji).queue();

            RaidPost raidPost = new RaidPost(messageObject.getIdLong(), partialPost.getReminderChannel(), partialPost.getMinRaiders(), emojiDateHashMap);
            raidPostService.save(raidPost);
            logger.info("Standard Raid Post Created");
        }));

        event.reply("Raid Post has been queued").queue();
    }

    public void postVoteModal(ModalInteractionEvent event) {
        PartialPost partialPost = partialPostContainer.getPartialPost();
        String rawDateTimes = event.getValue("date_time").getAsString();
        String[] splitDateTimes = rawDateTimes.split("\n");
        String message = event.getValue("message").getAsString();

        if (splitDateTimes.length == 0){
            event.reply("No date times given").queue();
            return;
        }

        List<RichCustomEmoji> emojiList = new ArrayList<>(event.getGuild().getEmojis());
        Map<RichCustomEmoji, Date> emojiDateMap;
        try {
            emojiDateMap = getEmojiDateMap(emojiList, splitDateTimes);
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).queue();
            return;
        }

        MessageEmbed embedMessage = createEmbedPost(partialPost.getRaidName(), partialPost.getOrganiser(), partialPost.getMinRaiders(), message, emojiDateMap);
        TextChannel textChannel = event.getGuild().getChannelById(TextChannel.class, partialPost.getPostChannel());
        textChannel.sendMessage(partialPost.getRole().getAsMention()).queue();
        textChannel.sendMessageEmbeds(embedMessage).queue((messageObject -> {
            emojiDateMap.forEach((emoji, date) -> messageObject.addReaction(emoji).queue());

            RaidPost raidPost = new RaidPost(messageObject.getIdLong(), partialPost.getReminderChannel(), partialPost.getMinRaiders(), emojiDateMap);
            raidPostService.save(raidPost);
            logger.info("Vote Raid Post Created");
        }));
        event.reply("Raid has been queued").queue();
    }

    private Map<RichCustomEmoji, Date> getEmojiDateMap(List<RichCustomEmoji> emojiList, String[] splitDateTimes){
        Map<RichCustomEmoji, Date> emojiDateMap = new HashMap<>();
        for (String rawDateTime : splitDateTimes) {
            String[] dateAndTime = rawDateTime.split("@");
            if (dateAndTime.length != 2) {
                throw new IllegalArgumentException(String.format("Unable to process given date time [%s] value", rawDateTime));
            }
            Date dateTime;
            try {
                dateTime = getDateTime(dateAndTime[0],dateAndTime[1]);
            } catch (ParseException e) {
                throw new IllegalArgumentException(String.format("Unable to process date [%s] and time [%s] values", dateAndTime[0], dateAndTime[1]));
            }
            int randomNum = new Random().nextInt(emojiList.size());
            emojiDateMap.put(emojiList.remove(randomNum), dateTime);
        }
        return emojiDateMap;
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
