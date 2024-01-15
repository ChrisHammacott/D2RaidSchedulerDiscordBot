package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class PostService {

    private final JDA jda;
    public PostService(JDA jda) {
        this.jda = jda;
    }

    public String getJoinPost(User user, Role role) {
        return user.getAsMention() + " has joined the fray! cc" + role.getAsMention();
    }

    public String getReservesPost(User user, Role role) {
        return user.getAsMention() + " is a reserve. cc" + role.getAsMention();
    }

    public String getDropoutPost(User user, Role role) {
        return user.getAsMention() + " has dropped out of the raid. cc" + role.getAsMention();
    }

    public String getFillingInPost(User user, Role role) {
        return user.getAsMention() + " is now part of the raid Team due to a drop out. cc" + role.getAsMention();
    }

    public String getTeamPost(Role role, List<User> userList) {
        if (userList.size() == 6) {
            return  getTeamPost(role, userList, "has a full raid team!");
        }
        return getTeamPost(role, userList, "has met the minimum raiders!");
    }

    private String getTeamPost(Role raidTeamRole, List<User> userList, String topLine){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(raidTeamRole.getAsMention()).append("  ").append(topLine).append("\n");
        stringBuilder.append("Raid Team:").append("\n");
        for (User user : userList) {
            stringBuilder.append(user.getAsMention()).append("\n");
        }
        return stringBuilder.toString();
    }

    public String getRaidCancelledPost(RaidInfo raidInfo, long dateTime) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Raid ").append(raidInfo.getRaidName()).append(", on ").append(getDateIdentifier(dateTime)).append(" is bellow minimum raiders. Raid cancelled").append("\n");
        for (Long userId : raidInfo.getUserIdList()){
            stringBuilder.append(jda.getUserById(userId).getAsMention()).append(", ");
        }
        stringBuilder.replace(stringBuilder.length()-3, stringBuilder.length()-1, ".");
        return stringBuilder.toString();
    }

    public static String getDateIdentifier(long dateTime) {
        SimpleDateFormat outputFormatterTime = new SimpleDateFormat("dd/MM/yy@HH:mm", Locale.UK);
        return outputFormatterTime.format(new Date(dateTime));
    }
}
