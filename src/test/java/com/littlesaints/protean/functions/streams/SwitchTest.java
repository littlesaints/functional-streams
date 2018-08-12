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
public class SwitchTest {

    private final int input;

    private final String expected;

    private static final Switch <Integer, Integer, String> SWITCH = Switch.<Integer, Integer, String>evaluate(Math::abs)
        .when(i -> i >= 0 && i < 10 , (ii, ll) -> "units")
        .when(i -> i >= 10 && i < 100 , (ii, ll) -> "tens")
        .orDefault((i, l) -> "hundreds or more");

    public SwitchTest(int input, String expected) {
        this.input = input;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "{index}: input={0} | expected={1}")
    public static Collection<Object[]> data() {
        final List<Object[]> data = new ArrayList<>();
        IntStream.range(-20, -10).forEach(i -> data.add(new Object[]{i, "tens"}));
        IntStream.range(-9, 10).forEach(i -> data.add(new Object[]{i, "units"}));
        IntStream.range(10, 100).forEach(i -> data.add(new Object[]{i, "tens"}));
        IntStream.range(100, 110).forEach(i -> data.add(new Object[]{i, "hundreds or more"}));
        IntStream.range(1000, 1010).forEach(i -> data.add(new Object[]{i, "hundreds or more"}));
        Collections.shuffle(data);
        return data.stream().map(d -> new Object[]{d[0], d[1]}).collect(Collectors.toList());
    }

    @Test
    public void test() {
        Assert.assertEquals(expected,
            Stream.of(input)
                .map(SWITCH)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny()
                .orElse("unknown"));
    }
}
