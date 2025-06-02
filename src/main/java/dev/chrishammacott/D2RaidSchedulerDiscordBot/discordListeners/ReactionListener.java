package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidInfoService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services.ReactionPostService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.services.ReminderSchedulerService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReactionListener extends ListenerAdapter {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${discord.botId}")
    private Long BOT_ID;
    @Value("${d2scheduler.reminder.offset}")
    private Long REMINDER_OFFSET;
    private final JDA jda;
    private final ReactionPostService reactionPostService;
    private final RaidInfoService raidInfoService;
    private final ReminderSchedulerService reminderSchedulerService;

    public ReactionListener(JDA jda, ReactionPostService reactionPostService, RaidInfoService raidInfoService, ReminderSchedulerService reminderSchedulerService) {
        jda.addEventListener(this);
        this.jda = jda;
        this.reactionPostService = reactionPostService;
        this.raidInfoService = raidInfoService;
        this.reminderSchedulerService = reminderSchedulerService;
    }

    @PostConstruct
    private void postConstructor() {
        for (RaidInfo raidInfo : raidInfoService.getAllPosts()) {
            //Clear any out-of-date raids.
            long inDate_DateTime = Instant.now().toEpochMilli()-REMINDER_OFFSET-100000;
            if (!raidInfo.isInDate(inDate_DateTime)) {
                raidInfoService.delete(raidInfo.getId());
                break;
            }

            jda.getChannelById(TextChannel.class, raidInfo.getPostChannelId())
                    .retrieveMessageById(raidInfo.getPostId())
                    .queue((message -> {
                        List<MessageReaction> reactions = message.getReactions();
                        for (MessageReaction reaction : reactions) {
                            if (reaction.getEmoji().asCustom().getIdLong())
                            reaction.retrieveUsers().queue((users -> {

                            }));
                            // todo process through on message reaction add method (refactored)
                        }
                    }));

            //Check if db (raid info)s are out of sync with posts
                //process removed reactions through unreacted method
                //process new reactions through message reaction add method
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        // Check if reaction belongs to an active raid post
        // todo have local collection to reduce cost as this will call DB for every post for all channels the bot is in
        Optional<RaidInfo> raidInfoOptional = raidInfoService.getRaidPost(event.getMessageIdLong());
        if (raidInfoOptional.isEmpty()){
            return;
        }

        RaidInfo raidInfo = raidInfoOptional.get();
        logger.info("Picked up reaction to tracked post [{}]", raidInfo.getPostId());

        // Removing reaction if the emoji is not registered to the raid post.
        long eventEmojiId;
        try {
            eventEmojiId = event.getEmoji().asCustom().getIdLong(); // throws IllegalStateException if emoji is not custom (raid post emojis are always custom).
            if (!raidInfo.isRegisteredEmoji(eventEmojiId)){
                throw new IllegalArgumentException("Emoji not registered");
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.info("Reaction was wrong emoji, removing it from [{}]", raidInfo.getPostId());
            event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> message.removeReaction(event.getEmoji(), event.getUser()).queue());
            return;
        }

        List<User> userList = event.getReaction().retrieveUsers().complete();
        for (User user : userList) {
            if (user.getIdLong() == BOT_ID && userList.size() > 1) {
                logger.info("User has reacted, removing bot reaction from [{}]", raidInfo.getPostId());
                event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> message.removeReaction(event.getEmoji(), user).queue());
                userList.remove(user);
            }
        }

        // Add user to raid post
        raidInfo.addUser(eventEmojiId, event.getUser().getIdLong());
        logger.info("Adding user [{}], to [{}]", event.getUser().getName(), raidInfo.getPostId());
        raidInfoService.save(raidInfo);

        // get current number of raiders (includes this reaction)
        int raiders = raidInfo.getUserIdList(eventEmojiId).size();

        if (raiders == raidInfo.getMinRaiders()) {
            activateRaid(event.getGuild(), raidInfo, eventEmojiId);
            return;
        }

        if (raiders > raidInfo.getMinRaiders() && raiders <= 6) {
            Role role = jda.getRoleById(raidInfo.getRoleId());
            logger.info("Adding user [{}], to [{}]", event.getUser().getName(), raidInfo.getPostId());
            event.getGuild().addRoleToMember(event.getUser(), role).queue();

            String message = reactionPostService.getJoinPost(event.getUser(), role);
            if (raiders == 6){
                logger.info("Sending updated team post for [{}]", raidInfo.getPostId());
                message = reactionPostService.getTeamPost(role, raidInfo.getUserIdList(eventEmojiId));
            }
            event.getGuild().getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(message).queue();
            return;
        }

        if (raiders > 6) {
            logger.info("Adding user [{}], as a reserve for [{}]", event.getUser().getName(), raidInfo.getPostId());
            String message = reactionPostService.getReservesPost(event.getUser(), raidInfo.getRaidName());
            event.getGuild().getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(message).queue();
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        Optional<RaidInfo> raidInfoOptional = raidInfoService.getRaidPost(event.getMessageIdLong());
        if (raidInfoOptional.isEmpty()){
            return;
        }
        RaidInfo raidInfo = raidInfoOptional.get();
        logger.info("Picked up reaction removal on tracked post [{}]", raidInfo.getPostId());
        //Not accepted emoji
        long eventEmojiId;
        try {
            eventEmojiId = event.getEmoji().asCustom().getIdLong();
        } catch (IllegalStateException e) {
            return;
        }

        if (!raidInfo.isRegisteredEmoji(eventEmojiId)){
            logger.info("Wrong Emoji, not proceeding [{}]", raidInfo.getPostId());
            return;
        }

        logger.info("Removing user [{}], from [{}]", event.getUser().getName(),  raidInfo.getPostId());
        raidInfo.removeUser(eventEmojiId, event.getUser().getIdLong());
        raidInfoService.save(raidInfo);
        if (raidInfo.getUserIdList(eventEmojiId).size()+1 < raidInfo.getMinRaiders()){
            logger.info("Raid post, [{}], was bellow minRaiders not proceeding", raidInfo.getPostId());
            return;
        }

        Role role = jda.getRoleById(raidInfo.getRoleId());
        logger.info("Sending drop out message for [{}]", raidInfo.getPostId());
        String dropoutMessage = reactionPostService.getDropoutPost(event.getUser(), role);
        event.getGuild().getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(dropoutMessage).queue();

        if (raidInfo.getUserIdList(eventEmojiId).size() < raidInfo.getMinRaiders()){
            logger.info("Raid now bellow min raiders, sending cancelled message for [{}]", raidInfo.getPostId());
            String cancelledMessage = reactionPostService.getRaidCancelledPost(raidInfo, eventEmojiId);
            event.getGuild().getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(cancelledMessage).queue();

            logger.info("Deleting role for [{}]", raidInfo.getPostId());
            role.delete().queue();
            raidInfo.setRoleId(null);
            raidInfoService.save(raidInfo);
            logger.info("Cancelling reminder for [{}]", raidInfo.getPostId());
            reminderSchedulerService.cancelReminder(raidInfo.getPostId());

            // For raids with a vote, check if any other time has enough raiders
            for (var entry : raidInfo.getEmojiUserListMap().entrySet()) {
                if (entry.getValue().size() >= 6) {
                    activateRaid(event.getGuild(), raidInfo, eventEmojiId);
                    break;
                }
            }
        }
        if (raidInfo.getUserIdList(eventEmojiId).size() >= 6) {
            logger.info("Raid post has reserves [{}]", raidInfo.getPostId());
            User user = jda.getUserById(raidInfo.getUserIdList(eventEmojiId).get(5));
            logger.info("Adding role to user [{}] for [{}]", user.getName(), raidInfo.getPostId());
            event.getGuild().addRoleToMember(user, role).queue();

            logger.info("Sending filling in message for [{}]", raidInfo.getPostId());
            String message = reactionPostService.getFillingInPost(user, role);
            event.getGuild().getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(message).queue();
        }
    }

    private void activateRaid(Guild guild, RaidInfo raidInfo, long eventEmojiId) {
        guild.createRole()
            .setName("Raid Team")
            .setColor(Color.orange)
            .queue(role -> {
                logger.info("Created role [{}], for [{}]", role.getIdLong(), raidInfo.getPostId());
                raidInfo.setRoleId(role.getIdLong());
                for (long userId : raidInfo.getUserIdList(eventEmojiId)) {
                    logger.info("Adding user [{}], to [{}] role", userId, raidInfo.getPostId());
                    User user = jda.getUserById(userId);
                    guild.addRoleToMember(user, role).queue();
                }

                String message = reactionPostService.getTeamPost(role, raidInfo.getUserIdList(eventEmojiId));
                logger.info("Sending team post for [{}]", raidInfo.getPostId());
                guild.getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(message).queue();

                raidInfoService.save(raidInfo);
                logger.info("Scheduling reminder for [{}]", raidInfo.getPostId());
                reminderSchedulerService.scheduleReminder(raidInfo.getDateTime(eventEmojiId), raidInfo);
            });
    }
}
