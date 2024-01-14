package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidPostService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.ReminderService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.RaidPost;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services.PostService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.services.ReminderSchedulerService;
import net.dv8tion.jda.api.JDA;
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
    private final PostService postService;
    private final RaidPostService raidPostService;
    private final ReminderService reminderService;
    private final ReminderSchedulerService reminderSchedulerService;

    public ReactionListener(JDA jda, PostService postService, RaidPostService raidPostService, ReminderService reminderService, ReminderSchedulerService reminderSchedulerService) {
        jda.addEventListener(this);
        this.postService = postService;
        this.raidPostService = raidPostService;
        this.reminderService = reminderService;
        this.reminderSchedulerService = reminderSchedulerService;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        Optional<RaidPost> raidPostOptional = raidPostService.getRaidPost(event.getMessageIdLong());
        if (raidPostOptional.isEmpty()){
            return;
        }
        RaidPost raidPost = raidPostOptional.get();
        //Not accepted emoji
        if (!raidPost.isRegisteredEmoji(event.getEmoji())){
            event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> message.removeReaction(event.getEmoji(), event.getUser()).queue());
            return;
        }

        List<User> userList = event.getReaction().retrieveUsers().complete();
//        for (User user : userList) {
//            if (user.getIdLong() == BOT_ID && userList.size() > 1) {
//                event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> message.removeReaction(event.getEmoji(), user).queue());
//            }
//        }

        if (raidPost.isRaidActive()) {
            event.getGuild().addRoleToMember(event.getUser(), raidPost.getAssignedRaidRole()).queue();
            String message = postService.getJoinPost(event.getUser(), raidPost.getAssignedRaidRole());
            if (userList.size() == 6){
                //todo switch to track even after post has reached 6 peeps
                raidPostService.deleteByPostId(raidPost.getDiscordPostId());
                message = postService.getTeamPost(raidPost.getAssignedRaidRole(), userList);
            }
            event.getGuild().getChannelById(TextChannel.class, raidPost.getDiscordReminderChannel()).sendMessage(message).queue();
        }

        //Raid needs to be activated
        if(userList.size() == raidPost.getMinimumRaiders()){
            event.getGuild().createRole()
                    .setName("RaidTeam-" + postService.getDateIdentifier(raidPost.getDate(event.getEmoji())))
                    .setColor(Color.orange)
                    .queue(role -> {
                        for (User user : userList){
                            event.getGuild().addRoleToMember(user, role).queue();
                        }
                        raidPost.setAssignedRaidRole(role);

                        String message = postService.getTeamPost(role, userList);

                        event.getGuild().getChannelById(TextChannel.class, raidPost.getDiscordReminderChannel()).sendMessage(message).queue();

                        if (userList.size() == 6) {
                            //todo switch to track even after post has reached 6 peeps
                            raidPostService.deleteByPostId(raidPost.getDiscordPostId());
                        } else {
                            raidPostService.save(raidPost);
                        }
                        reminderSchedulerService.scheduleReminder(raidPost.getDate(event.getEmoji()).getTime(), raidPost);
                        reminderSchedulerService.scheduleCloseRaidPost(raidPost.getDate(event.getEmoji()).getTime(), raidPost);
                    });
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        Optional<RaidPost> raidPostOptional = raidPostService.getRaidPost(event.getMessageIdLong());
        if (raidPostOptional.isEmpty()){
            return;
        }
        RaidPost raidPost = raidPostOptional.get();
        if (!raidPost.isRaidActive()) {
            return;
        }
        List<User> userList = event.getReaction().retrieveUsers().complete();
        if (userList.size() < raidPost.getMinimumRaiders()){
            String cancelledMessage = postService.getRaidCancelledPost(userList, raidPost.getDate(event.getEmoji()));
            event.getGuild().getChannelById(TextChannel.class, raidPost.getDiscordReminderChannel()).sendMessage(cancelledMessage).queue();

            raidPost.setAssignedRaidRole(null);
            raidPostService.save(raidPost);

            reminderSchedulerService.cancelReminder(raidPost.getDiscordPostId());
            reminderService.deleteByPostId(raidPost.getDiscordPostId());
        }
    }
}
