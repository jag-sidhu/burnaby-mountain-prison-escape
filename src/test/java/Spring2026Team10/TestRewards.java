package Spring2026Team10;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestRewards {
    private Player testplayer;

    @Test
    public void testRewardInit() {
        Rewards laptop = new Rewards(10.0f, 20.0f, RewardType.LAPTOP);
        assertEquals(10.0f, laptop.getX(), "X Coordinate does not match expected value");
        assertEquals(20.0f, laptop.getY(), "Y Coordinate does not match expected value");
        assertEquals(RewardType.LAPTOP, laptop.getRewardType(), "Reward type does not match expected value");
        assertTrue(laptop.isActive(), "Reward should be active when initialized");
    }

    @Test
    public void testApplyLaptop() {
        testplayer = new Player(0, 0, new PrisonMap());
        int startingScore = testplayer.getScore();
        int endScore = startingScore + 50;
        Rewards laptop = new Rewards(10.0f, 20.0f, RewardType.LAPTOP);

        laptop.applyTo(testplayer);
        assertEquals(endScore, testplayer.getScore(), "Score does not match expected value after laptop is applied");
        assertFalse(laptop.isActive(), "Laptop should not be active after applying laptop");
        assertEquals(1, testplayer.getReward(), "Player reward count should be incremented after reward is applied");
    }

    @Test
    public void testApplyID() {
        testplayer = new Player(0, 0, new PrisonMap());
        int startingScore = testplayer.getScore();
        int endScore = startingScore + 75;
        Rewards studentID = new Rewards(10.0f, 20.0f, RewardType.STUDENT_ID);

        studentID.applyTo(testplayer);
        assertEquals(endScore, testplayer.getScore(), "Score does not match expected value after Student_ID is applied");
        assertFalse(studentID.isActive(), "Student_ID should not be active after applying Student_ID");
        assertEquals(1, testplayer.getReward(), "Player reward count should be incremented after reward is applied");
    }

    @Test
    public void testApplyRaccoon() {
        testplayer = new Player(0, 0, new PrisonMap());
        int startingScore = testplayer.getScore();
        int endScore = startingScore + 100;
        Rewards raccoon = new Rewards(10.0f, 20.0f, RewardType.RACCOON);

        raccoon.applyTo(testplayer);
        assertEquals(endScore, testplayer.getScore(), "Score does not match expected value after raccoon is applied");
        assertFalse(raccoon.isActive(), "raccoon should not be active after applying raccoon");
        assertEquals(1, testplayer.getReward(), "Player reward count should be incremented after reward is applied");
    }

    @Test
    public void testHandsTiedApply() {
        testplayer = new Player(0, 0, new PrisonMap());
        Rewards raccoon = new Rewards(10.0f, 20.0f, RewardType.RACCOON);
        int initialScore = testplayer.getScore();
        testplayer.tieHands(15);
        raccoon.applyTo(testplayer);
        assertEquals(initialScore, testplayer.getScore(), "Score does not match expected value after hands is applied");
        assertTrue(raccoon.isActive(), "Reward should remain active when applying after hands is applied");
    }

    @Test
    public void testNonActiveReward() {
        testplayer = new Player(0, 0, new PrisonMap());
        Rewards raccoon = new Rewards(10.0f, 20.0f, RewardType.RACCOON);
        raccoon.applyTo(testplayer);
        int scoreAfterReward = testplayer.getScore();
        raccoon.applyTo(testplayer);
        assertEquals(scoreAfterReward, testplayer.getScore(), "Player should not get points from an inactive reward");
    }

}
