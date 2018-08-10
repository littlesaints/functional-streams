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

package com.littlesaints.protean.functions.maths;

import java.util.function.IntPredicate;

import com.littlesaints.protean.functions.ToIntIntBiFunction;

/**
 * Factory of functions performing Math operations.
 *
 * @author Varun Anand
 * @since 0.1
 */
public interface Mathematician {

	/**
	 * <pre>
	 * calculate the modulo of a number where the number is a power of two.
	 *
	 * The method would be inaccurate if the given number is not a power of 2.
	 * {@link #isPowerOf2} should be used to ascertain whether it is or not.
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
	IntPredicate isPowerOf2 = n -> (n & (n - 1)) == 0;

}
