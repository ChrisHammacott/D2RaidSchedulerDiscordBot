package dev.chrishammacott.D2RaidSchedulerDiscordBot.controllers.model;

public class ValuePair {

    private final String name;
    private final long id;

    public ValuePair(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }
}
