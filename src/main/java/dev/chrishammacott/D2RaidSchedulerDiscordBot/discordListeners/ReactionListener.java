package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidInfoService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services.PostService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.services.ReminderSchedulerService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.Optional;

@Service
public class ReactionListener extends ListenerAdapter {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${bot.id}")
    private Long BOT_ID;
    private final JDA jda;
    private final PostService postService;
    private final RaidInfoService raidInfoService;
    private final ReminderSchedulerService reminderSchedulerService;

    public ReactionListener(JDA jda, PostService postService, RaidInfoService raidInfoService, ReminderSchedulerService reminderSchedulerService) {
        jda.addEventListener(this);
        this.jda = jda;
        this.postService = postService;
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
        //Not accepted emoji
        long eventEmojiId;
        try {
            eventEmojiId = event.getEmoji().asCustom().getIdLong();
        } catch (IllegalStateException e) {
            return;
        }
        if (raidInfo.getEmojiId() != eventEmojiId){
            event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> message.removeReaction(event.getEmoji(), event.getUser()).queue());
            return;
        }
        if (event.getUser().getIdLong() == BOT_ID) {
            return;
        }

        List<User> userList = event.getReaction().retrieveUsers().complete();
        for (User user : userList) {
            if (user.getIdLong() == BOT_ID && userList.size() > 1) {
                event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> message.removeReaction(event.getEmoji(), user).queue());
                userList.remove(user);
            }
        }

        raidInfo.addUser(event.getUser().getIdLong());
        raidInfoService.save(raidInfo);
        if (raidInfo.getUserIdList().size() == raidInfo.getMinRaiders()) {
            event.getGuild().createRole()
                    .setName("RaidTeam-" + postService.getDateIdentifier(raidInfo.getDateTime()))
                    .setColor(Color.orange)
                    .queue(role -> {
                        raidInfo.setRoleId(role.getIdLong());
                        for (long userId : raidInfo.getUserIdList()) {
                            event.getGuild().addRoleToMember(jda.getUserById(userId), role);
                        }

                        String message = postService.getTeamPost(role, userList);

                        event.getGuild().getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(message).queue();

                        raidInfoService.save(raidInfo);
                        reminderSchedulerService.scheduleReminder(raidInfo.getDateTime(), raidInfo);
                    });
        }
        if (raidInfo.getUserIdList().size() > raidInfo.getMinRaiders() && raidInfo.getUserIdList().size() <= 6) {
            Role role = jda.getRoleById(raidInfo.getRoleId());
            event.getGuild().addRoleToMember(event.getUser(), role).queue();

            String message = postService.getJoinPost(event.getUser(), role);
            if (userList.size() == 6){
                message = postService.getTeamPost(role, userList);
            }
            event.getGuild().getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(message).queue();
        }
        if (raidInfo.getUserIdList().size() > 6) {
            Role role = jda.getRoleById(raidInfo.getRoleId());
            String message = postService.getReservesPost(event.getUser(), role);
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
        //Not accepted emoji
        long eventEmojiId;
        try {
            eventEmojiId = event.getEmoji().asCustom().getIdLong();
        } catch (IllegalStateException e) {
            return;
        }

        raidInfo.removeUser(event.getUser().getIdLong());
        raidInfoService.save(raidInfo);
        if (raidInfo.getEmojiId() != eventEmojiId || raidInfo.getUserIdList().size()+1 < raidInfo.getMinRaiders()){
            return;
        }

        Role role = jda.getRoleById(raidInfo.getRoleId());
        String dropoutMessage = postService.getDropoutPost(event.getUser(), role);
        event.getGuild().getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(dropoutMessage).queue();

        if (raidInfo.getUserIdList().size() < raidInfo.getMinRaiders()){
            String cancelledMessage = postService.getRaidCancelledPost(raidInfo, raidInfo.getDateTime());
            event.getGuild().getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(cancelledMessage).queue();

            role.delete().queue();
            raidInfo.setRoleId(null);
            raidInfoService.save(raidInfo);
            reminderSchedulerService.cancelReminder(raidInfo.getPostId());
        }
        if (raidInfo.getUserIdList().size() >= 6) {
            User user = jda.getUserById(raidInfo.getUserIdList().get(5));
            event.getGuild().addRoleToMember(user, role).queue();

            String message = postService.getFillingInPost(user, role);
            event.getGuild().getChannelById(TextChannel.class, raidInfo.getReminderChannelId()).sendMessage(message).queue();
        }
    }
}
