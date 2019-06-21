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
import lombok.extern.log4j.Log4j2;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.littlesaints.protean.functions.trial.Constants.NO_DELAY_INCREASE;
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
@Log4j2
public abstract class AbstractTrial<T> {

    @Getter
    public static class State {

        private long currentDelayBetweenTriesInMillis;

        private int attemptedTriesWithDelay;

        private int attemptedTriesWithYield;

        private long remainingTriesUntilDelayIncrease;

        private void reset(Strategy strategy) {
            attemptedTriesWithYield = 0;
            attemptedTriesWithDelay = 0;
            currentDelayBetweenTriesInMillis = strategy.getDelayBetweenTriesInMillis();
            remainingTriesUntilDelayIncrease = strategy.getTriesUntilDelayIncrease();
        }
    }

    private final Strategy strategy;

    private final Predicate<T> successfulOpTest;

    private final UnaryOperator<T> onTrialsExhaustion;

    @Getter
    private final State state = new State();

    private final Predicate<?> trialValidator;

    private final Runnable delayAdjuster;

    AbstractTrial(Strategy strategy, Predicate<T> successfulOpTest, UnaryOperator<T> onTrialsExhaustion) {
        strategy.validate();
        this.strategy = strategy.toBuilder().build();
        this.successfulOpTest = successfulOpTest;
        this.onTrialsExhaustion = onTrialsExhaustion;
        trialValidator = getTrialValidator(this.strategy);
        delayAdjuster = getDelayAdjuster(this.strategy);
    }

    protected T get(Supplier<T> op) {
        state.reset(strategy);
        T result;
        do {
            if (successfulOpTest.test(result = op.get())){
                return result;
            }
        } while (test());
        return onTrialsExhaustion.apply(result);
    }

    private boolean test() {
        if (trialValidator.test(null)) {
            if (state.currentDelayBetweenTriesInMillis > 0) {
                //can't do better atm.
                try {
                    Thread.sleep(state.currentDelayBetweenTriesInMillis);
                } catch (InterruptedException e) {
                    log.warn("Error during Thread.sleep.", e);
                }
            }
            delayAdjuster.run();
            return true;
        }
        return false;
    }

    private Predicate<?> getTrialValidator(Strategy strategy) {

        final int strategyMaxTriesWithDelay = strategy.getMaxTriesWithDelay();

        final Predicate<?> trialValidator;
        if (strategy.getMaxTriesWithYield() > 0) {
            final int strategyMaxTriesWithYield = this.strategy.getMaxTriesWithYield();
            if (strategyMaxTriesWithDelay == UNBOUNDED_TRIES) {
                trialValidator = x -> {
                    if (state.attemptedTriesWithYield < strategyMaxTriesWithYield) {
                        Thread.yield();
                        ++state.attemptedTriesWithYield;
                    }
                    return true;
                };
            } else {
                trialValidator = x -> {
                    if (state.attemptedTriesWithYield < strategyMaxTriesWithYield) {
                        Thread.yield();
                        ++state.attemptedTriesWithYield;
                        return true;
                    } else if (state.attemptedTriesWithDelay < strategyMaxTriesWithDelay) {
                        ++state.attemptedTriesWithDelay;
                        return true;
                    }
                    return false;
                };
            }
        } else {
            if (strategyMaxTriesWithDelay == UNBOUNDED_TRIES) {
                trialValidator = x -> true;
            } else {
                trialValidator = x -> {
                    if (state.attemptedTriesWithDelay < strategyMaxTriesWithDelay) {
                        ++state.attemptedTriesWithDelay;
                        return true;
                    }
                    return false;
                };
            }
        }
        return trialValidator;
    }

    private Runnable getDelayAdjuster(Strategy strategy) {

        final long strategyDelayThresholdInMillis = strategy.getDelayThresholdInMillis();
        final int strategyTriesUntilDelayIncrease = strategy.getTriesUntilDelayIncrease();
        final int strategyDelayIncreaseMultiplier = strategy.getDelayIncreaseMultiplier();

        final Runnable delayAdjuster;
        if (strategyTriesUntilDelayIncrease <= NO_DELAY_INCREASE
                || strategy.getDelayBetweenTriesInMillis() == strategyDelayThresholdInMillis
                || strategyDelayIncreaseMultiplier == 1) {
            delayAdjuster = () -> {};
        } else {
            delayAdjuster = () -> {
                // iff delayThresholdInMillis isn't reached.
                if (state.currentDelayBetweenTriesInMillis < strategyDelayThresholdInMillis) {
                    --state.remainingTriesUntilDelayIncrease;
                    if (state.remainingTriesUntilDelayIncrease == 0) {
                        // reset
                        state.remainingTriesUntilDelayIncrease = strategyTriesUntilDelayIncrease;
                        // double the wait time bounded by the threshold
                        state.currentDelayBetweenTriesInMillis =
                                Math.min(state.currentDelayBetweenTriesInMillis * strategyDelayIncreaseMultiplier,
                                        strategyDelayThresholdInMillis);
                    }
                }
            };
        }
        return delayAdjuster;
    }

}
