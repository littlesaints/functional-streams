package com.littlesaints.protean.functions.streams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TryTest {

    private final String input;

    private final Boolean expected;

    private static final Try <String, Integer, Boolean> TRY = Try.<String, Integer, Boolean>of(Integer::parseInt)
        .onSuccess(i -> true)
        .onFailure((i, e) -> e == null)
        .onFinally(i -> System.out.println("Finally :: ".concat(i)));

    public TryTest(String input, Boolean expected) {
        this.input = input;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "{index}: input={0} | expected={1}")
    public static Collection<Object[]> data() {
        final List<Object[]> data = new ArrayList<>();
        IntStream.range(0, 100).boxed().map(Objects::toString).forEach(i -> data.add(new Object[]{i, true}));
        IntStream.range(0, 100).boxed().map(Objects::toString).map("XX"::concat).forEach(i -> data.add(new Object[]{i, false}));
        Collections.shuffle(data);
        return data.stream().map(d -> new Object[]{d[0], d[1]}).collect(Collectors.toList());
    }

    @Test
    public void test() {
        Assert.assertEquals(expected,
            Stream.of(input)
                .map(TRY)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny()
                .orElse(null));
    }
}
