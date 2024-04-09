package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidInfoService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services.ReactionPostService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.services.ReminderSchedulerService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReactionListener extends ListenerAdapter {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${bot.id}")
    private Long BOT_ID;
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

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        Optional<RaidInfo> raidInfoOptional = raidInfoService.getRaidPost(event.getMessageIdLong());
        if (raidInfoOptional.isEmpty()){
            return;
        }
        RaidInfo raidInfo = raidInfoOptional.get();
        logger.info("Picked up reaction to tracked post [{}]", raidInfo.getPostId());
        //Not accepted emoji
        long eventEmojiId;
        try {
            eventEmojiId = event.getEmoji().asCustom().getIdLong();
            if (!raidInfo.isRegisteredEmoji(eventEmojiId)){
                throw new IllegalArgumentException("Emoji not registered");
            }
        } catch (Exception e) {
            logger.info("Reaction was wrong emoji, removing it from [{}]", raidInfo.getPostId());
            event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> message.removeReaction(event.getEmoji(), event.getUser()).queue());
            return;
        }
        if (event.getUser().getIdLong() == BOT_ID) {
            logger.info("Bot reacted, not proceeding to [{}]", raidInfo.getPostId());
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

        raidInfo.addUser(eventEmojiId, event.getUser().getIdLong());
        logger.info("Adding user [{}], to [{}]", event.getUser().getName(), raidInfo.getPostId());
        raidInfoService.save(raidInfo);
        if (raidInfo.getUserIdList(eventEmojiId).size() == raidInfo.getMinRaiders()) {
            if (raidInfo.getRoleId() == null) {
                activateRaid(event.getGuild(), raidInfo, eventEmojiId);
            }
        }
        if (raidInfo.getUserIdList(eventEmojiId).size() > raidInfo.getMinRaiders() && raidInfo.getUserIdList(eventEmojiId).size() <= 6) {
            Role role = jda.getRoleById(raidInfo.getRoleId());
            logger.info("Adding user [{}], to [{}]", event.getUser().getName(), raidInfo.getPostId());
            event.getGuild().addRoleToMember(event.getUser(), role).queue();

            String message = reactionPostService.getJoinPost(event.getUser(), role);
            if (userList.size() == 6){
                logger.info("Sending team post for [{}]", raidInfo.getPostId());
                message = reactionPostService.getTeamPost(role, userList);
            }
            event.getGuild().getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(message).queue();
        }
        if (raidInfo.getUserIdList(eventEmojiId).size() > 6) {
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

            //todo switch to keeping role even if raid is bellow min
            logger.info("Deleting role for [{}]", raidInfo.getPostId());
            role.delete().queue();
            raidInfo.setRoleId(null);
            raidInfoService.save(raidInfo);
            logger.info("Cancelling reminder for [{}]", raidInfo.getPostId());
            reminderSchedulerService.cancelReminder(raidInfo.getPostId());
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
            .setName("RaidTeam-" + ReactionPostService.getDateIdentifier(raidInfo.getDateTime(eventEmojiId)))
            .setColor(Color.orange)
            .queue(role -> {
                logger.info("Created role [{}], for [{}]", role.getIdLong(), raidInfo.getPostId());
                raidInfo.setRoleId(role.getIdLong());
                List<User> userList = new ArrayList<>();
                for (long userId : raidInfo.getUserIdList(eventEmojiId)) {
                    logger.info("Adding user [{}], to [{}] role", userId, raidInfo.getPostId());
                    User user = jda.getUserById(userId);
                    userList.add(user);
                    guild.addRoleToMember(user, role).queue();
                }

                String message = reactionPostService.getTeamPost(role, userList);
                logger.info("Sending team post for [{}]", raidInfo.getPostId());
                guild.getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(message).queue();

                raidInfoService.save(raidInfo);
                logger.info("Scheduling reminder for [{}]", raidInfo.getPostId());
                reminderSchedulerService.scheduleReminder(raidInfo.getDateTime(eventEmojiId), raidInfo);
            });
    }
}
