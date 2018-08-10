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

package com.littlesaints.protean.functions;

/**
 * <pre>
 * A functional interface that throws a throwable.
 * It's helpful to integrate api calls that throw exception and need exception handling to integrate for a streams pipeline processing.
 *
 * This is most useful when used as a lambda expression in a {@link com.littlesaints.protean.functions.streams.Try} function.
 * Which would not involve referencing this Interface directly in code.
 *
 * @see {@link com.littlesaints.protean.functions.streams.Try} for details on it's usage.
 * </pre>
 * @author Varun Anand
 * @since 0.1
 */
@FunctionalInterface
public interface XFunction<T, R> {

    R apply(T t) throws Throwable;

}
