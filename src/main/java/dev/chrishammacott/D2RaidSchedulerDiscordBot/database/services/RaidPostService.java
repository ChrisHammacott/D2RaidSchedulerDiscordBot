package dev.chrishammacott.D2RaidSchedulerDiscordBot.database.services;

import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.model.RaidPostRecord;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.database.mongoRepositories.RaidPostRepository;
import dev.chrishammacott.D2RaidSchedulerDiscordBot.discordListeners.model.RaidPost;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RaidPostService {

    private final RaidPostRepository raidPostRepository;
    private final JDA jda;

    public RaidPostService(RaidPostRepository raidPostRepository, JDA jda) {
        this.raidPostRepository = raidPostRepository;
        this.jda = jda;
    }

    public void save(RaidPost raidPost) {
        raidPostRepository.insert(new RaidPostRecord(raidPost));
    }

    public void delete(String objectId) {
        raidPostRepository.deleteById(objectId);
    }

    public void deleteByPostId(Long postId) {
        raidPostRepository.deleteRaidPostByDiscordPostId(postId);
    }

    public List<RaidPost> getAllPosts() {
        List<RaidPostRecord> raidPostRecords = raidPostRepository.findAll();
        List<RaidPost> raidPosts = new ArrayList<>();
        for (RaidPostRecord raidPostRecord : raidPostRecords) {
            Role role = raidPostRecord.getAssignedRole() == null ? null : getRole(raidPostRecord.getAssignedRole());
            Map<RichCustomEmoji, Date> emojiDateMap = getEmojiDateMap(raidPostRecord.getEmojiIdDateMap());
            raidPosts.add(new RaidPost(raidPostRecord, role, emojiDateMap));
        }
        return raidPosts;
    }

    public Optional<RaidPost> getRaidPost(Long discordPostId){
        Optional<RaidPostRecord> raidPostRecordOptional = raidPostRepository.findRaidPostRecordByDiscordPostId(discordPostId);
        if (raidPostRecordOptional.isPresent()) {
            RaidPostRecord raidPostRecord = raidPostRecordOptional.get();
            Role role = raidPostRecord.getAssignedRole() == null ? null : getRole(raidPostRecord.getAssignedRole());
            Map<RichCustomEmoji, Date> emojiDateMap = getEmojiDateMap(raidPostRecord.getEmojiIdDateMap());
            return Optional.of(new RaidPost(raidPostRecord, role, emojiDateMap));
        }
        return Optional.empty();
    }

    private Map<RichCustomEmoji, Date> getEmojiDateMap(Map<Long, Long> emojiIdDateMap) {
        Map<RichCustomEmoji, Date> emojiDateMap = new HashMap<>();
        emojiIdDateMap.forEach((id, date) -> emojiDateMap.put(jda.getEmojiById(id), new Date(date)));
        return emojiDateMap;
    }

    private Role getRole(Long roleId) {
        return jda.getRoleById(roleId);
    }

}
