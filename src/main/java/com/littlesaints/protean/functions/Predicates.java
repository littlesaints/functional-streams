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

package com.littlesaints.protean.functions;

import java.util.function.Predicate;

/**
 * This is a utility class for lambdas as predicates.
 * <p/>
 * This enables us to execute {@link Predicate} functions on lambdas, without explicit casting.
 * In the absence of this class, one needs to either cast the lambda to an Predicate OR write additional lines of *non-functional* code.
 * <p/>
 * Usage:
 * {@code
 *     // Print null OR multiples of 3
 *     Stream.of(1, 2, 3, null)
 *                 .filter(  Predicates.<Integer>of(Objects::isNull).or( i -> (i % 3) == 0)  )
 *                 .forEach(System.out::println);
 * }
 */
public final class Predicates {

    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }

    public static <T> Predicate<T> of(Predicate<T> predicate) {
        return predicate;
    }

}
