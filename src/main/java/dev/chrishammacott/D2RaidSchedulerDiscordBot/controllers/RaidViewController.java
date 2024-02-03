package dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers.model.Defaults;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers.model.ValuePair;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.Config;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.ConfigService;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidInfoService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequestMapping("/views")
@Controller
public class RaidViewController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JDA jda;
    private final RaidInfoService raidInfoService;
    private final ConfigService configService;

    public RaidViewController(JDA jda, RaidInfoService raidInfoService, ConfigService configService) {
        this.jda = jda;
        this.raidInfoService = raidInfoService;
        this.configService = configService;
    }

    @HxRequest
    @GetMapping("/raidList")
    public String viewRaidList(Model model) {
        model.addAttribute("raidInfoList", raidInfoService.getAllFormattedPosts());
        return "components/raidList";
    }

    @HxRequest
    @PostMapping("/createRaidForm/DateTimeInput")
    public HtmxResponse addDateTimeInput(@RequestParam(value = "noDateTimes") Integer noDateTimes, Model model) {
        // num is index used in list, so num of inputs is n-1
        if (noDateTimes > 3) {
            noDateTimes = 3;
        }
        model.addAttribute("minDateTime", getCurrentDateTimeWithOffset());
        model.addAttribute("numOfDateTimes", noDateTimes + 1);
        return HtmxResponse.builder().view("components/dateTimesInput").build();
    }

    @HxRequest
    @GetMapping("/createRaidForm/initialDateTimeInput")
    public HtmxResponse getInitialDateTimeInput(Model model) {
        model.addAttribute("minDateTime", getCurrentDateTimeWithOffset());
        model.addAttribute("numOfDateTimes", 1);
        return HtmxResponse.builder().view("components/dateTimesInput").build();
    }

    @HxRequest
    @GetMapping("/createRaidForm")
    public HtmxResponse viewCreateRaid(Model model) {
        return createRaidForm(jda, model, configService.getConfig()).build();
    }

    public static HtmxResponse.Builder createRaidForm(JDA jda, Model model, Optional<Config> configOptional) {
        Defaults defaults = new Defaults(configOptional);
        model.addAttribute("defaults", defaults);

        List<ValuePair> userList = new ArrayList<>();
        jda.getUsers().forEach(user -> userList.add(new ValuePair(user.getName(), user.getIdLong())));
        model.addAttribute("userList", userList);

        List<ValuePair> channels = new ArrayList<>();
        jda.getGuilds().get(0).getChannels().forEach(channel ->  {
            if (channel.getType().equals(ChannelType.TEXT)) {
                channels.add(new ValuePair(channel.getName(), channel.getIdLong()));
            }
        });
        model.addAttribute("channels", channels);

        List<ValuePair> roles = new ArrayList<>();
        jda.getRoles().forEach(role -> roles.add(new ValuePair(role.getName(), role.getIdLong())));
        model.addAttribute("roles", roles);

        return HtmxResponse.builder().view("components/createRaidForm");
    }

    private String getCurrentDateTimeWithOffset() {
        return LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }
}
