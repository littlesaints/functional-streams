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

import com.littlesaints.protean.functions.streams.If;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class TrialTest {

    private final static int TEST_DEFAULT_DELAY_IN_MILLIS = 1;

    private final static int TEST_DEFAULT_DELAY_INCREASE_RETRIES = 100;

    @Test
    public void test_exit_after_n_retries_for_unbounded_counter() {
        final int simulatedTries = 20;
        final Strategy strategy = Strategy.builder()
                .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
                .build();
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
    public void test_exit_after_n_retries_setters() {
        final int simulatedTries = 5;
        final Strategy strategy = new Strategy();
        strategy.setMaxTriesWithYield(5);
        strategy.setMaxTriesWithDelay(5);
        strategy.setTriesUntilDelayIncrease(5);
        strategy.setDelayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS);
        strategy.setDelayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS);

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

    @Test
    public void testNullable() {
        final Strategy strategy = Strategy.builder().maxTriesWithDelay(5).build();
        final AtomicInteger counter = new AtomicInteger(0);
        Trial<Integer> trial = Trial.ofNullable(strategy, () -> counter.incrementAndGet() == 2 ? -1 : null);
        assertEquals(Integer.valueOf(-1), trial.get().orElse(-2));
    }

    @Test
    public void testOptional() {
        final Strategy strategy = Strategy.builder().triesUntilDelayIncrease(5).maxTriesWithDelay(5).build();
        final AtomicInteger counter = new AtomicInteger(0);
        Trial<Optional<Integer>> trial = Trial.ofOptional(strategy, () -> counter.incrementAndGet() == 3 ? -1 : null);
        assertEquals(Integer.valueOf(-1), trial.getSupplier().get().orElse(Optional.empty()).orElse(-2));
        assertEquals(3, trial.getRemainingTriesUntilDelayIncrease());
    }

    private void testRetryDelay(int simulatedTries, long expectedDelayInMillis, Strategy strategy) {
        testRetryDelay(simulatedTries, expectedDelayInMillis, strategy, 1);
    }

    private void testRetryDelay(int simulatedInvocations, long expectedDelayInMillis, Strategy strategy, int runs) {
        final AtomicInteger counter = new AtomicInteger(0);
        Trial<Integer> trial = Trial.of(strategy, counter::incrementAndGet, i -> i == simulatedInvocations);
        for (int i = 0; i < runs; i++) {
            counter.set(0);
            trial.get();
            assertEquals(expectedDelayInMillis, trial.getCurrentDelayBetweenTriesInMillis());
        }
    }

    private void testRetries(final int simulatedTries, Strategy strategy) {
        testRetries(simulatedTries, strategy, 1);
    }

    private void testRetries(final int simulatedInvocations, Strategy strategy, int runs) {
        final AtomicInteger counter = new AtomicInteger(0);
        Trial<Integer> trial = Trial.of(strategy, counter::incrementAndGet, i -> i == simulatedInvocations);

        Optional<Integer> result;
        for (int i = 0; i < runs; i++) {
            counter.set(0);
            result = trial.get();

            long maxAttemptedTries = trial.getAttemptedTriesWithYield() + trial.getAttemptedTriesWithDelay();
            if (strategy.getMaxTriesWithDelay() == Constants.UNBOUNDED_TRIES) {
                assertEquals(maxAttemptedTries, 0);
            } else {
                //Unbounded attempts cause counter to NOT increment, to avoid overflow.
                assertEquals(simulatedInvocations, maxAttemptedTries + 1);
                assertEquals(counter.get(), maxAttemptedTries + 1);
            }
            assertEquals(simulatedInvocations, counter.get());
            assertEquals(result.get().intValue(), counter.get());
        }
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