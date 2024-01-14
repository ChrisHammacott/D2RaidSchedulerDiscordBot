package dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private final RaidInfoService raidInfoService;

    public MainController(RaidInfoService raidInfoService) {
        this.raidInfoService = raidInfoService;
    }

    @GetMapping("/")
    public String viewMainPage(Model model) {
        model.addAttribute("raidPostList", raidInfoService.getAllPosts());
        return "index";
    }
}
