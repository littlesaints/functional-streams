/*
 *                     functional-streams
 *              Copyright (C) 2018 Varun Anand
 *
 * This file is part of functional-streams.
 *
 * functional-streams is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * functional-streams is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.littlesaints.protean.functions.trial;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class AbstractTrialTest {

    private final static int TEST_DEFAULT_DELAY_IN_MILLIS = 1;

    @Test
    public void test_exit_after_n_retries_for_unbounded_counter() {
        final int simulatedTries = 20;
        final Strategy strategy = Strategy.CONSTANT_DELAY_UNBOUNDED_TRIES.apply(TEST_DEFAULT_DELAY_IN_MILLIS);
        testRetries(simulatedTries, strategy);
    }

    @Test
    public void test_unbounded_attempts_constant() {
        assertEquals(Constants.UNBOUNDED_TRIES, -1);
    }

    @Test
    public void test_exit_after_n_retries() {
        final int simulatedTries = 5;
        final Strategy strategy = Strategy.builder()
                .maxTriesWithYield(5)
                .maxTriesWithDelay(5)
                .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS).build();

        testRetries(simulatedTries, strategy);
    }

    @Test
    public void test_exit_after_n_delay_retries() {
        final int simulatedTries = 5;
        final Strategy strategy = Strategy.builder()
                .maxTriesWithYield(2)
                .maxTriesWithDelay(5)
                .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS).build();

        testRetries(simulatedTries, strategy);
    }

    @Test
    public void test_exit_after_n_no_wait_retries() {
        final int simulatedTries = 5;
        final Strategy strategy = Strategy.builder()
                .maxTriesWithYield(2)
                .maxTriesWithDelay(5)
                .delayBetweenTriesInMillis(0)
                .delayThresholdInMillis(0).build();

        testRetries(simulatedTries, strategy);
    }

    @Test
    public void test_exit_after_n_delay_retries_unbounded() {
        final int simulatedTries = 5;
        final Strategy strategy = Strategy.builder()
                .maxTriesWithYield(2)
                .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS).build();

        testRetries(simulatedTries, strategy);
    }

    @Test
    public void test_exit_after_n_retries_setters() {
        final int simulatedTries = 5;
        final Strategy strategy = Strategy.builder()
                .maxTriesWithYield(5)
                .maxTriesWithDelay(5)
                .triesUntilDelayIncrease(5)
                .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS).build();

        Assert.assertNotNull(strategy.toString());
        Assert.assertNotNull(Strategy.builder().toString());
        testRetries(simulatedTries, strategy);
    }

    @Test
    public void test_exit_after_n_retries_no_increase() {
        final int simulatedTries = 5;
        final Strategy strategy = Strategy.builder()
                .maxTriesWithYield(5)
                .maxTriesWithDelay(5)
                .triesUntilDelayIncrease(Constants.NO_DELAY_INCREASE)
                .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS).build();

        Assert.assertNotNull(strategy.toString());
        Assert.assertNotNull(Strategy.builder().toString());
        testRetries(simulatedTries, strategy);
    }

    @Test
    public void test_exit_after_max_retries() {
        final int maxTries = 5;
        final Strategy strategy = Strategy.builder()
                .maxTriesWithDelay(maxTries)
                .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .build();

        testRetries(maxTries, strategy);
    }

    @Test
    public void test_exit_after_max_retries_yield() {
        final int maxTries = 10;
        final Strategy strategy = Strategy.builder()
                .maxTriesWithYield(2)
                .maxTriesWithDelay(5)
                .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .build();

        testTrialFailure(maxTries, strategy, 1);
    }

    @Test
    public void test_no_increase_in_delay_after_threshold() {
        int delayIncreaseRetries = 2;
        int maxDelayInMillis = 4;
        final int maxTries = 10;

        final Strategy strategy = Strategy.builder()
                .maxTriesWithDelay(maxTries)
                .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .triesUntilDelayIncrease(delayIncreaseRetries)
                .delayThresholdInMillis(maxDelayInMillis).build();

        testRetryDelay(maxTries + 10, maxDelayInMillis, strategy);
    }

    @Test
    public void test_increase_in_delay_after_increase_threshold() {
        int delayIncreaseRetries = 2;

        final Strategy strategy = Strategy.builder()
                .maxTriesWithDelay(10)
                .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .triesUntilDelayIncrease(delayIncreaseRetries)
                .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS * 3)
                .build();

        testRetryDelay(delayIncreaseRetries * 2, strategy.getDelayBetweenTriesInMillis() * 2, strategy);
    }

    @Test
    public void testResetAfterMaxRetries() {
        final int maxRetries = 5;
        final Strategy strategy = Strategy.builder()
                .maxTriesWithDelay(maxRetries)
                .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS).build();

        testRetries(maxRetries, strategy, 2);
    }

    @Test
    public void testResetAfterThresholdDelay() {
        int delayIncreaseRetries = 2;
        int maxDelayInMillis = 4;

        final Strategy strategy = Strategy.builder()
                .maxTriesWithDelay(10)
                .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .triesUntilDelayIncrease(delayIncreaseRetries)
                .delayThresholdInMillis(maxDelayInMillis)
                .build();

        testRetryDelay(delayIncreaseRetries * 2, strategy.getDelayBetweenTriesInMillis() * 2, strategy, 2);
    }

    protected abstract void testRetries(final int simulatedInvocations, Strategy strategy, int runs);

    protected abstract void testRetryDelay(int simulatedInvocations, long expectedDelayInMillis, Strategy strategy, int runs);

    protected abstract void testTrialFailure(final int simulatedInvocations, Strategy strategy, int runs);

    private void testRetryDelay(int simulatedTries, long expectedDelayInMillis, Strategy strategy) {
        testRetryDelay(simulatedTries, expectedDelayInMillis, strategy, 1);
    }

    private void testRetries(final int simulatedTries, Strategy strategy) {
        testRetries(simulatedTries, strategy, 1);
    }

    //	creating this method because of findbugs "ICAST_INTEGER_MULTIPLY_CAST_TO_LONG"
    private long power(int num, long pow) {
        long result = num;
        for (int i = 2; i <= pow; i++) {
            result *= num;
        }
        return result;
    }

}
