package com.littlesaints.protean.functions.streams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class IfTest {

    private final int input;

    private final String expected;

    private static final If <Integer, String> IF = If. <Integer, String>test(i -> i >= 10)
        .then(i -> "i >= 10")
        .elseIf(i -> i >= 5 && i < 10, i -> "5 <= i < 10")
        .elseIf(i -> i >= 0 && i < 5, i -> "0 <= i < 5")
        .orElse(i -> "i < 0");

    public IfTest(int input, String expected) {
        this.input = input;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "{index}: input={0} | expected={1}")
    public static Collection <Object[]> data() {
        final List <Object[]> data = new ArrayList <>();
        IntStream.range(-10, 0).forEach(i -> data.add(new Object[]{i, "i < 0"}));
        IntStream.range(0, 5).forEach(i -> data.add(new Object[]{i, "0 <= i < 5"}));
        IntStream.range(5, 10).forEach(i -> data.add(new Object[]{i, "5 <= i < 10"}));
        IntStream.range(10, 15).forEach(i -> data.add(new Object[]{i, "i >= 10"}));
        Collections.shuffle(data);
        return data.stream().map(d -> new Object[]{d[0], d[1]}).collect(Collectors.toList());
    }

    @Test
    public void test() {
        Assert.assertEquals(expected,
            Stream.of(input)
                .map(IF)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny()
                .orElse("unknown"));
    }

}
