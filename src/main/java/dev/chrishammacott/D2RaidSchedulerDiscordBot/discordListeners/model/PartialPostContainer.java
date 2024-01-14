package dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model;

import org.springframework.stereotype.Component;

@Component
public class PartialPostContainer {

    private PartialPost partialPost;

    public PartialPostContainer() {
    }

    public PartialPost getPartialPost() {
        return partialPost;
    }

    public void setPartialPost(PartialPost partialPost) {
        this.partialPost = partialPost;
    }
}
