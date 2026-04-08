package net.azisaba.azisabamod.fabric.util;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

public class Optionals {
    public static <T> @NotNull OptionalInt mapToInt(@NotNull Optional<T> optional, @NotNull Function<? super T, Integer> function) {
        Integer integer = optional.map(function).orElse(null);
        return integer == null ? OptionalInt.empty() : OptionalInt.of(integer);
    }
}
