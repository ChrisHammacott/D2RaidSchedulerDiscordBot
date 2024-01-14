package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.ConfigService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services.CommandService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommandListener extends ListenerAdapter {

    private final ConfigService configService;
    private final CommandService commandService;
    Logger logger = LoggerFactory.getLogger(CommandListener.class);

    public CommandListener(JDA jda, ConfigService configService, CommandService commandService) {
        jda.addEventListener(this);

        this.configService = configService;
        this.commandService = commandService;
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandList = new ArrayList<>();

        // setup command
        OptionData raidChannel = new OptionData(OptionType.CHANNEL, "raid_channel", "set channel to post raid schedule posts")
                .setChannelTypes(ChannelType.TEXT)
                .setRequired(true);
        OptionData reminderChannel = new OptionData(OptionType.CHANNEL, "reminder_channel", "set channel for reminders to be sent to")
                .setChannelTypes(ChannelType.TEXT)
                .setRequired(true);
        OptionData defaultRole = new OptionData(OptionType.ROLE, "default_role", "set default role to be pinged when a raid posting goes up")
                .setRequired(true);
        commandList.add(Commands.slash("setup", "setup raid schedule channel and channel for reminder to be sent")
                .addOptions(raidChannel)
                .addOptions(reminderChannel)
                .addOptions(defaultRole));

        // post command
        OptionData raidName = new OptionData(OptionType.STRING, "raid_name", "set raid name").setMinLength(3).setRequired(true);
        OptionData organiser = new OptionData(OptionType.USER, "organiser", "set raid organiser for post");
        OptionData minRaiders = new OptionData(OptionType.INTEGER, "min_raiders", "set min raider amount for post").setMinValue(3).setMaxValue(6);
        OptionData postChannel = new OptionData(OptionType.CHANNEL, "post_channel", "set post channel for post").setChannelTypes(ChannelType.TEXT);
        OptionData reminderChannel2 = new OptionData(OptionType.CHANNEL, "reminder_channel", "set reminder channel for post").setChannelTypes(ChannelType.TEXT);
        OptionData roleMention = new OptionData(OptionType.ROLE, "role_mention", "set role mention");
        commandList.add(Commands.slash("post", "schedule a raid")
                .addOptions(raidName)
                .addOptions(organiser)
                .addOptions(minRaiders)
                .addOptions(postChannel)
                .addOptions(reminderChannel2)
                .addOptions(roleMention));

        // post vote time command
        commandList.add(Commands.slash("post_vote", "schedule a raid with voting")
                .addOptions(raidName)
                .addOptions(organiser)
                .addOptions(postChannel)
                .addOptions(reminderChannel2)
                .addOptions(roleMention));

        // remove post from active posts

        event.getGuild().updateCommands().addCommands(commandList).queue();
        logger.info("Discord Commands set.");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("setup")){
            logger.info("Setup Command Run.");
            commandService.setup(event);
            return;
        }
        boolean configEmpty = configService.getConfig().isEmpty();
        if (configEmpty){
            //todo update so that other commands have not been added till setup has been run?
            logger.info("User ran command without setup");
            event.reply("setup command not run").queue();
            return;
        }
        if (command.equals("post")){
            logger.info("Post Command Run.");
            commandService.post(event);
        }
        if (command.equals("post_vote")){
            logger.info("Post Vote Command Run.");
            commandService.votePost(event);
        }
    }
}
