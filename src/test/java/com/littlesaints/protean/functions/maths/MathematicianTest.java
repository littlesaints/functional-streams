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

import org.junit.Assert;
import org.junit.Test;

public class MathematicianTest {

    @Test
    public void moduloForPowerOfTwoTest() {
        Assert.assertEquals(0, Mathematician.moduloForPowerOfTwo.applyAsInt(4*4, 4));
        Assert.assertEquals(3, Mathematician.moduloForPowerOfTwo.applyAsInt(4*4+3, 4));
        Assert.assertEquals(120, Mathematician.moduloForPowerOfTwo.applyAsInt(128*64+120, 128));
    }

    @Test
    public void isPowerOf2Test() {
        Assert.assertTrue(Mathematician.isPowerOfTwo.test(0));
        Assert.assertTrue(Mathematician.isPowerOfTwo.test(2));
        Assert.assertTrue(Mathematician.isPowerOfTwo.test(128));
        Assert.assertTrue(Mathematician.isPowerOfTwo.test(128*128));
        Assert.assertFalse(Mathematician.isPowerOfTwo.test(127));
        Assert.assertFalse(Mathematician.isPowerOfTwo.test(-1));
        Assert.assertFalse(Mathematician.isPowerOfTwo.test(-2));
    }

}
