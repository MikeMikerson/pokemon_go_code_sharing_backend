package com.devs.simplicity.poke_go_friends.task;

import com.devs.simplicity.poke_go_friends.service.FriendCodeService;
import com.devs.simplicity.poke_go_friends.service.RedditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that scrapes Reddit for Pokemon Go friend codes and saves them to the database.
 * Runs at the top of every hour.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedditScraperTask {

    private final RedditService redditService;
    private final FriendCodeService friendCodeService;

    /**
     * Scheduled method to fetch and save friend codes from Reddit.
     * Runs every hour at minute 0.
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void scrapeAndSaveFriendCodes() {
        log.info("Starting scheduled Reddit friend code scraping task");
        try {
            var friendCodes = redditService.fetchFriendCodes();
            int newCodes = friendCodeService.addFriendCodesFromScraper(friendCodes);
            log.info("Reddit scraping complete: {} new friend codes added", newCodes);
        } catch (Exception e) {
            log.error("Error during Reddit scraping task: {}", e.getMessage(), e);
        }
    }
}
