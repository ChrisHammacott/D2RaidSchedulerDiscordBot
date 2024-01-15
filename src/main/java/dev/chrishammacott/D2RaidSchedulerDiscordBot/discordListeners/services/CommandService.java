package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.Config;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.ConfigService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.ModalListener;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.PartialPost;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.PartialPostContainer;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import net.dv8tion.jda.api.JDA;

@Service
public class CommandService {

    Logger logger = LoggerFactory.getLogger(CommandService.class);
    private final JDA jda;
    private final ConfigService configService;
    private final PartialPostContainer partialPostContainer;

    public CommandService(JDA jda, ConfigService configService, PartialPostContainer partialPostContainer) {
        this.jda = jda;
        this.configService = configService;
        this.partialPostContainer = partialPostContainer;
    }

    public void setup(SlashCommandInteractionEvent event) {
        GuildChannelUnion defaultPostChannel = event.getOption("raid_channel").getAsChannel();
        GuildChannelUnion defaultReminderChannel = event.getOption("reminder_channel").getAsChannel();
        Role defaultRole = event.getOption("default_role").getAsRole();

        configService.upsertConfig(new Config(defaultPostChannel.getIdLong(), defaultReminderChannel.getIdLong(), defaultRole.getIdLong()));

        String reply = String.format("Default Post Channel: `[%s]`. Default Reminder Channel `[%s]`. Default Role `[%s]`.", defaultPostChannel.getName(), defaultReminderChannel.getName(), defaultRole.getName());
        logger.info(reply);
        event.reply(reply).queue();
    }

    public void post(SlashCommandInteractionEvent event) {
        OptionMapping minRaidersOption = event.getOption("min_raiders");
        int minRaiders = 6;
        if (minRaidersOption != null){
            if (minRaidersOption.getAsInt() > 6 || minRaidersOption.getAsInt() < 1) {
                event.reply("min_raiders must be 2-6").queue();
                logger.info("Min Raiders was not 2-6");
                return;
            }
            minRaiders = minRaidersOption.getAsInt();
        }
        savePostData(event, minRaiders);
        logger.info("Partial Data saved");
        event.replyModal(ModalListener.getPostModal()).queue();
        logger.info("Modal response sent");
    }

    public void votePost(SlashCommandInteractionEvent event) {
        savePostData(event, 6);
        logger.info("Partial Data saved");
        event.replyModal(ModalListener.getPostVoteModal()).queue();
        logger.info("Modal response sent");
    }

    private void savePostData(SlashCommandInteractionEvent event, int minRaiders) {
        OptionMapping organiserMapping = event.getOption("organiser");
        OptionMapping postChannelOption = event.getOption("post_channel");
        OptionMapping reminderChannelOption = event.getOption("reminder_channel");
        OptionMapping roleMentionOption = event.getOption("role_mention");

        Config config = configService.getConfig().get();

        String raidName = event.getOption("raid_name").getAsString();
        String organiser = event.getMember().getAsMention();
        long postChannel = config.defaultPostChannel();
        long reminderChannel = config.defaultReminderChannel();
        Role role = jda.getRoleById(config.defaultRole());

        if (organiserMapping != null){
            organiser = organiserMapping.getAsMember().getAsMention();
        }
        if (postChannelOption != null){
            postChannel = postChannelOption.getAsChannel().getIdLong();
        }
        if (reminderChannelOption != null){
            reminderChannel = reminderChannelOption.getAsChannel().getIdLong();
        }
        if (roleMentionOption != null){
            role = roleMentionOption.getAsRole();
        }
        partialPostContainer.setPartialPost(new PartialPost(raidName, postChannel, reminderChannel, role, organiser, minRaiders));
    }
}
