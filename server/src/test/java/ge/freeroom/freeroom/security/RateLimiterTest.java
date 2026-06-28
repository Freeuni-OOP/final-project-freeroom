package ge.freeroom.freeroom.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    @Test
    void allowsRequestsUnderTheLimit() {
        RateLimiter limiter = new RateLimiter();
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allow("user1", 5, 60000));
        }
    }

    @Test
    void blocksRequestsOverTheLimit() {
        RateLimiter limiter = new RateLimiter();
        for (int i = 0; i < 5; i++) {
            limiter.allow("user1", 5, 60000);
        }
        assertFalse(limiter.allow("user1", 5, 60000));
    }

    @Test
    void separateKeysHaveSeparateLimits() {
        RateLimiter limiter = new RateLimiter();
        for (int i = 0; i < 5; i++) {
            limiter.allow("user1", 5, 60000);
        }
        assertTrue(limiter.allow("user2", 5, 60000));
    }

    @Test
    void allowsAgainAfterWindowExpires() throws InterruptedException {
        RateLimiter limiter = new RateLimiter();
        limiter.allow("user1", 2, 100);
        limiter.allow("user1", 2, 100);
        assertFalse(limiter.allow("user1", 2, 100));
        Thread.sleep(150);
        assertTrue(limiter.allow("user1", 2, 100));
    }
}
