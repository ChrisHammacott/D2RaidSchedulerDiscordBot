package dev.chrishammacott.D2RaidSchedulerDiscordBot.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.PartialPost;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

@Service
public class RaidPostService {

    private final JDA jda;

    public RaidPostService(JDA jda) {
        this.jda = jda;
    }

    public void postRaidListing(PartialPost partialPost, Consumer<Long> messageIdConsumer) {
        TextChannel textChannel = jda.getChannelById(TextChannel.class, partialPost.getPostChannel());
        MessageEmbed embedMessage;
        if (partialPost.getEmojiDateHashMap().size() == 1) {
            RichCustomEmoji emoji = partialPost.getEmojiDateHashMap().keySet().stream().findFirst().get();
            embedMessage = createEmbedPost(partialPost, partialPost.getEmojiDateHashMap().get(emoji), emoji);
        } else {
            embedMessage = createEmbedPost(partialPost);
        }

        textChannel.sendMessage(partialPost.getMentionRole().getAsMention()).queue();
        textChannel.sendMessageEmbeds(embedMessage).queue((messageObject -> {
            for (var entry : partialPost.getEmojiDateHashMap().entrySet()){
                messageObject.addReaction(entry.getKey()).queue();
            }
            messageIdConsumer.accept(messageObject.getIdLong());
        }));
    }

    private MessageEmbed createEmbedPost(PartialPost partialPost, Date date, RichCustomEmoji emoji) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Raid Incoming!");
        embedBuilder.setDescription("<t:" + date.toInstant().getEpochSecond() + ":R>");
        embedBuilder.setColor(Color.orange);
        embedBuilder.setThumbnail("https://i.insider.com/5800ec6c52dd73d0018b4e21?width=750&format=jpeg&auto=webp");
        embedBuilder.addField("Raid", partialPost.getRaidName(), false);
        embedBuilder.addField("Organiser", partialPost.getOrganiser(), true);
        embedBuilder.addField("Min Raiders", partialPost.getMinRaiders() + " peeps", true);
        embedBuilder.addField("Date and Time", formatDateTime(date), false);
        embedBuilder.addField("Call to Arms", partialPost.getMessage() + "\n" + "**React " + emoji.getFormatted() + " to join**", false);
        return embedBuilder.build();
    }

    private MessageEmbed createEmbedPost(PartialPost partialPost) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Raid Incoming!");
        embedBuilder.setDescription("Vote for Day");
        embedBuilder.setColor(Color.orange);
        embedBuilder.setThumbnail("https://i.insider.com/5800ec6c52dd73d0018b4e21?width=750&format=jpeg&auto=webp");
        embedBuilder.addField("Raid", partialPost.getRaidName(), false);
        embedBuilder.addField("Organiser", partialPost.getOrganiser(), true);
        embedBuilder.addField("Min Raiders", partialPost.getMinRaiders() + " peeps", true);
        embedBuilder.addField("Date and Time", "TBD by Vote", false);
        embedBuilder.addField("Call to Arms", partialPost.getMessage(), false);
        embedBuilder.addField("Vote by Reacting:", getVoteField(partialPost.getEmojiDateHashMap()),false);
        return embedBuilder.build();
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
