package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class ReactionPostService {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JDA jda;
    public ReactionPostService(JDA jda) {
        this.jda = jda;
    }

    public String getJoinPost(User user, Role role) {
        return user.getAsMention() + " has joined the fray! cc" + role.getAsMention();
    }

    public String getReservesPost(User user, String raidName) {
        return user.getAsMention() + " is a reserve for " + raidName;
    }

    public String getDropoutPost(User user, Role role) {
        return user.getAsMention() + " has dropped out of the raid. cc" + role.getAsMention();
    }

    public String getFillingInPost(User user, Role role) {
        return user.getAsMention() + " is now part of the raid Team due to a drop out. cc" + role.getAsMention();
    }

    public String getTeamPost(Role role, List<Long> userList) {
        if (userList.size() == 6) {
            return  getTeamPost(role, userList, "has a full raid team!");
        }
        return getTeamPost(role, userList, "has met the minimum raiders!");
    }

    private String getTeamPost(Role raidTeamRole, List<Long> userList, String topLine){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(raidTeamRole.getAsMention()).append("  ").append(topLine).append("\n");
        stringBuilder.append("Raid Team:").append("\n");
        for (Long userId : userList) {
            stringBuilder.append(jda.getUserById(userId).getAsMention()).append("\n");
        }
        return stringBuilder.toString();
    }

    public String getRaidCancelledPost(RaidInfo raidInfo, long eventEmojiId) {
        long dateTime = raidInfo.getDateTime(eventEmojiId);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Raid ").append(raidInfo.getRaidName()).append(", on ").append(getDateIdentifier(dateTime)).append(" is bellow minimum raiders. Raid cancelled").append("\n");
        for (Long userId : raidInfo.getUserIdList(eventEmojiId)){
            stringBuilder.append(jda.getUserById(userId).getAsMention()).append(", ");
        }
        logger.info(stringBuilder.toString());
        stringBuilder.replace(stringBuilder.length()-2, stringBuilder.length(), ".");
        return stringBuilder.toString();
    }

    public static String getDateIdentifier(long dateTime) {
        SimpleDateFormat outputFormatterTime = new SimpleDateFormat("dd/MM/yy@HH:mm", Locale.UK);
        return outputFormatterTime.format(new Date(dateTime));
    }
}
