package dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    Logger logger = LoggerFactory.getLogger(this.getClass());


    public MainController() {
    }

    @GetMapping("/")
    public String viewMainPage(Model model) {
        return "index";
    }
}
