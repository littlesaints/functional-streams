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

package com.littlesaints.protean.functions.maths;

import com.littlesaints.protean.functions.ToIntIntBiFunction;

import java.util.function.IntPredicate;

/**
 * Factory of functions performing Math operations.
 *
 * @author Varun Anand
 * @since 1.0
 */
public interface Mathematician {

	/**
	 * <pre>
	 * A function to calculate much faster modulo operation with any number that is a power of 2, using a '&' instead of the '%' operator.
	 * It's useful when doing any conditional routing for data processing, whether relative order needs to be maintained.
	 *
	 * The method would be inaccurate, if the given number is not a power of 2.
	 * {@link #isPowerOfTwo} should be used to ascertain whether it is or not.
	 * </pre>
	 * @param x the divisor
	 * @param powerOf2Y the dividend
	 * @return the result of 'x mod powerOf2Y'
	 */
	ToIntIntBiFunction moduloForPowerOfTwo = (x, powerOf2Y) -> x & (powerOf2Y - 1);

	/**
	 * checks, if an number is a power of 2.
	 *
	 * @param n the number to validate.
	 * @return true, if the number is a power of 2 or false otherwise.
	 */
	IntPredicate isPowerOfTwo = n -> (n & (n - 1)) == 0;

}
