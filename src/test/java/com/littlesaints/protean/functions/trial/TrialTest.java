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

import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class TrialTest extends AbstractTrialTest {

    @Test
    public void testNullable() {
        final Strategy strategy = Strategy.builder().maxTriesWithDelay(5).build();
        final AtomicInteger counter = new AtomicInteger(0);
        Trial<Integer> trial = Trial.ofNullable(strategy, () -> counter.incrementAndGet() == 2 ? -1 : null);
        assertEquals(Integer.valueOf(-1), trial.get());
    }

    @Test
    public void testOptional() {
        final Strategy strategy = Strategy.builder().triesUntilDelayIncrease(5).maxTriesWithDelay(5).build();
        final AtomicInteger counter = new AtomicInteger(0);
        Trial<Optional<Integer>> trial = Trial.ofOptional(strategy, () -> counter.incrementAndGet() == 3 ? -1 : null);
        assertEquals(Integer.valueOf(-1), trial.get().orElse(-2));
        assertEquals(3, trial.getState().getRemainingTriesUntilDelayIncrease());
    }

    @Override
    protected void testRetryDelay(int simulatedInvocations, long expectedDelayInMillis, Strategy strategy, int runs) {
        final AtomicInteger counter = new AtomicInteger(0);
        Trial<Integer> trial = Trial.of(strategy, counter::incrementAndGet, i -> i == simulatedInvocations, r -> r);
        for (int i = 0; i < runs; i++) {
            counter.set(0);
            trial.get();
            assertEquals(expectedDelayInMillis, trial.getState().getCurrentDelayBetweenTriesInMillis());
        }
    }

    @Override
    protected void testRetries(final int simulatedInvocations, Strategy strategy, int runs) {
        final AtomicInteger counter = new AtomicInteger(0);
        Trial<Integer> trial = Trial.of(strategy, counter::incrementAndGet, i -> i == simulatedInvocations, r -> Integer.MIN_VALUE);

        Integer result;
        for (int i = 0; i < runs; i++) {
            counter.set(0);
            result = trial.get();

            long maxAttemptedTries = trial.getState().getAttemptedTriesWithYield() + trial.getState().getAttemptedTriesWithDelay();
            if (strategy.getMaxTriesWithDelay() == Constants.UNBOUNDED_TRIES) {
                int expectedTries = simulatedInvocations - 1 <= strategy.getMaxTriesWithYield() ? simulatedInvocations : strategy.getMaxTriesWithYield();
                assertEquals(expectedTries, maxAttemptedTries);
            } else {
                //Unbounded attempts cause counter to NOT increment, to avoid overflow.
                assertEquals(simulatedInvocations, maxAttemptedTries + 1);
                assertEquals(counter.get(), maxAttemptedTries + 1);
            }
            assertEquals(simulatedInvocations, counter.get());
            assertEquals(result.intValue(), counter.get());
        }
    }

    @Override
    protected void testTrialFailure(final int simulatedInvocations, Strategy strategy, int runs) {
        final AtomicInteger counter = new AtomicInteger(0);
        Trial<Integer> trial = Trial.of(strategy, counter::incrementAndGet, i -> i == simulatedInvocations, r -> Integer.MIN_VALUE);

        Integer result;
        for (int i = 0; i < runs; i++) {
            counter.set(0);
            result = trial.get();

            long maxAttemptedTries = trial.getState().getAttemptedTriesWithYield() + trial.getState().getAttemptedTriesWithDelay();
            assertEquals(strategy.getMaxTriesWithYield() + strategy.getMaxTriesWithDelay(), maxAttemptedTries);
            assertEquals(counter.get(), maxAttemptedTries + 1);
            assertEquals(result.intValue(), Integer.MIN_VALUE);
        }
    }

}