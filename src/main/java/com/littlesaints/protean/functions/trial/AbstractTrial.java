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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.littlesaints.protean.functions.trial.Constants.UNBOUNDED_TRIES;

/**
 * <pre>
 * Base class for concrete implementation of 'trials'.
 *
 * Trials are a Functional way to re-attempt failures. They supports configuring a re-attempt strategy and can perform a phased-backoff on continuous failures.
 *
 * For more details, please see subclasses.
 *
 * @author Varun Anand
 * @since 1.0
 *
 * @see Strategy
 * @see com.littlesaints.protean.functions.streams.Try
 */
@Slf4j
public abstract class AbstractTrial<T> {

    private final Strategy strategy;

    private final Predicate<T> successfulOpTest;

    private final UnaryOperator<T> onTrialsExhaustion;

    @Getter
    private long currentDelayBetweenTriesInMillis;

    @Getter
    private int attemptedTriesWithDelay;

    @Getter
    private int attemptedTriesWithYield;

    @Getter
    private long remainingTriesUntilDelayIncrease;

    AbstractTrial(Strategy strategy, Predicate<T> successfulOpTest, UnaryOperator<T> onTrialsExhaustion) {
        this.strategy = strategy.toBuilder().build();
        this.successfulOpTest = successfulOpTest;
        this.onTrialsExhaustion = onTrialsExhaustion;
    }

    protected T get(Supplier<T> op) {
        reset();
        T result;
        do {
            if (successfulOpTest.test(result = op.get())){
                return result;
            }
        } while (test());
        return onTrialsExhaustion.apply(result);
    }

    private void reset() {
        attemptedTriesWithYield = 0;
        attemptedTriesWithDelay = 0;
        currentDelayBetweenTriesInMillis = strategy.getDelayBetweenTriesInMillis();
        remainingTriesUntilDelayIncrease = strategy.getTriesUntilDelayIncrease();
    }

    private boolean test() {

        if (attemptedTriesWithYield < strategy.getMaxTriesWithYield()) {
            Thread.yield();
            ++attemptedTriesWithYield;
            return true;
        }

        boolean retry = false;
        if (strategy.getMaxTriesWithDelay() == UNBOUNDED_TRIES) {
            //If unbounded retries, then don't increment attemptedTriesWithDelay or it'll eventually overflow.
            retry = true;
        }
        else if (attemptedTriesWithDelay < strategy.getMaxTriesWithDelay()) {
            ++attemptedTriesWithDelay;
            retry = true;
        }

        if (retry) {
            //can't do better atm.
            try {
                Thread.sleep(currentDelayBetweenTriesInMillis);
            } catch (InterruptedException e) {
                log.warn("Error during Thread.sleep.", e);
            }

            // iff delayThresholdInMillis isn't reached.
            if (currentDelayBetweenTriesInMillis < strategy.getDelayThresholdInMillis()) {
                if (remainingTriesUntilDelayIncrease > 0) {
                    --remainingTriesUntilDelayIncrease;
                    if (remainingTriesUntilDelayIncrease == 0) {
                        // reset
                        remainingTriesUntilDelayIncrease = strategy.getTriesUntilDelayIncrease();
                        // double the wait time bounded by the threshold
                        currentDelayBetweenTriesInMillis = Math.min(currentDelayBetweenTriesInMillis * 2, strategy.getDelayThresholdInMillis());
                    }
                }
            }
        }

        return retry;
    }

}
