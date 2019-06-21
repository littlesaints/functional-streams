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

public class StrategyTest {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMaxTriesWithYield() {
        Strategy.builder().maxTriesWithYield(-1).build().validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMaxTriesWithDelay() {
        Strategy.builder().maxTriesWithDelay(0).build().validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDelayBetweenTriesInMillis() {
        Strategy.builder().delayBetweenTriesInMillis(-1).build().validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDelayThresholdInMillis() {
        Strategy.builder().delayThresholdInMillis(-2).build().validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLessDelayThresholdInMillis() {
        Strategy.builder().delayBetweenTriesInMillis(1).delayThresholdInMillis(0).build().validate();
    }

    @Test
    public void testNoWaitDelay() {
        Strategy.builder().delayBetweenTriesInMillis(0).delayThresholdInMillis(0).build().validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDelayThresholdInMillis_DelayBetweenTriesInMillis() {
        Strategy.builder().delayBetweenTriesInMillis(1000).delayThresholdInMillis(999).build().validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTriesUntilDelayIncrease() {
        Strategy.builder().triesUntilDelayIncrease(0).build().validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDelayIncreaseMultiplier() {
        Strategy.builder().delayIncreaseMultiplier(0).build().validate();
    }

}
