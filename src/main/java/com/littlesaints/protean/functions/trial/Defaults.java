/*
 *                     functional-streams
 *               Copyright (C) 2018  Varun Anand
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Varun Anand
 * @since 1.0
 */
package com.littlesaints.protean.functions.trial;

import static com.littlesaints.protean.functions.trial.Constants.UNBOUNDED_TRIES;

public interface Defaults {

    int TRIES_WITH_DELAY = UNBOUNDED_TRIES;

    int TRIES_UNTIL_DELAY_INCREASE = 5;

    int DELAY_THRESHOLD_IN_MILLIS = 30 * 60 * 1000;

    int DELAY_BETWEEN_TRIES_IN_MILLIS = 1000;

    int TRIES_WITH_YIELD = 0;
}
