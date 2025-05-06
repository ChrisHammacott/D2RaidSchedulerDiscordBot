# Destiny 2 Raid Scheduler Discord Bot

## TLDR: This project allows you to schedule and auto manage raid posts.

Allows user to post a raid that people can react to.
When min raiders has been met, a raid team will form and a unique role will be created to tag the raid team.
This bot will then handle additional signups and drop-outs, notifying the raid team.
It will create a reminder 30 min before the raid start.

This project uses Java Spring with the JDA dependency to connect to discord. The web console is built using Thymleaf and HTMX.


### Included functionality:
- Posts a formatted embedded post to a channel of your choosing.
- Bot reacts to post to make it easier to react with specified emoji. Make sure to have as many custom emojis as date-times in your server.
- Manges scheduled raids posts with reactions (which indicates a sign-up).
- Posts updates on scheduled (meeting min raiders and when a full raid team is met).
- Creates a unique role for members of the raid team to be notified of updates on the raid post and for reminder message.
- Manges dropouts (reaction removal) and autosubs in people when num of reactions is greater than 6.
- Creates a reminder post that is sent out (default: 30 min) before raid start.

### How to build

First you will want to create the docker image, this can be done by using one of the included create-containers scripts (.sh or .ps1).
You will likely have to modify these scripts as they point to a private docker registry, but it is very easy to set up your own locally to push to.
Once you have created the image, you can use the included docker compose file to launch a mongodb instance and the built docker image.
If you haven't already, you will need to create a discord application from the developer portal, and make note of the secret token and application ID (Bot ID).
You can then add your discord bot token and bot ID to the docker compose file and start the service.
Depending on the mongodb instance you plan to use, you will likely have to add env variables to config the connection properties, these variables can be found in the properties file.

### Known Issues

 - On all reaction events, a call to the DB is made to see if it is a tracked post. 
   - Fix: Have a local collection of post IDs to make initial filtering less expensive.
 - Reminders will not be re-scheduled if the service restarts.
   - Fix: Have a collection that stores reminders and Post Constructor to set on startup.
 - If any (un)reactions occur when the service is offline, they will not be counted.
   - Fix: Have a Post Constructor method to check status of tracked posts and clean collections of out of date posts.
 - If a user spams react and un-react, events do not come in order, causing state in DB and the reactions on the post to be out of sync for that user.
   - Fix: Unsure at this time.