# Destiny 2 Raid Scheduler Discord Bot

## TLDR: This project allows you to schedule and auto manage raid posts.

Allows user to post a raid that people can react to. When min raiders has been met, a raid team will form and a unique role will be created to tag the raid team. This bot will then handle additional signups and drop outs, notifiying the raid team. It will create a reminder 30 min before the raid start.

### Included functionality:
- Posts a formatted embeded post to a channel of your choosing
- Bot reacts to post to make it easier to react with specified emoji. Make sure to have at least one custom emoji in your server.
- Manges scheduled raids posts with reactions (which indicates a sign up)
- Posts updates on scheduled (meeting min raiders and when a full raid team is met)
- Creates a unique role for members of the raid team to be notifed of updates on the raid post and for reminder message
- Manges dropouts (reaction removal) and autosubs in people when num of reactions is greater than 6
- Creates a reminder post that is sent out 30 min before raid start.

### How to build

First you will have to create a `.env` file and add your discord bot TOKEN to that file, see `.env.example`. Then add your bot Id to the .properties file. This will be used to remove the initial reaction the bot does on the raid post. Make sure to have an instance of MongoDB ready and update the url properties in the `.properties` file. The bot is now ready to run. You can create a dockerised version of the bot using the inbuilt `create-container.ps1` script.