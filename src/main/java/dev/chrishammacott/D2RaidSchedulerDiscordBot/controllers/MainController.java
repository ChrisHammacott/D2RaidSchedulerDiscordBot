package dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services.RaidPostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private final RaidPostService raidPostService;

    public MainController(RaidPostService raidPostService) {
        this.raidPostService = raidPostService;
    }

    @GetMapping("/")
    public String viewMainPage(Model model) {
        model.addAttribute("raidPostList", raidPostService.getAllPosts());
        return "index";
    }
}
