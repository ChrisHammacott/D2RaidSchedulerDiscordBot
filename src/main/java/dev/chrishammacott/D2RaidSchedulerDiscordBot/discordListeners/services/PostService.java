package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class PostService {

    public PostService() {
    }

    public String getJoinPost(User user, Role role) {
        return user.getAsMention() + " has joined the fray! cc" + role.getAsMention();
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

    public String getRaidCancelledPost(List<User> userList, Date date) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Raid on ").append(getDateIdentifier(date)).append(" is bellow minimum raiders. Raid currently Cancelled cc");
        for (User user : userList){
            stringBuilder.append(user.getAsMention());
        }
        return stringBuilder.toString();
    }

    public String getDateIdentifier(Date dateTime) {
        SimpleDateFormat outputFormatterTime = new SimpleDateFormat("dd/MM/yy@HH:mm", Locale.UK);
        return outputFormatterTime.format(dateTime);
    }
}
