package io.terminus.doctor.common.utils;

import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/3
 */

public class CountUtil {

    public static <T> long sumInt(List<T> datas, ToIntFunction<T> function) {
        return datas.stream().collect(Collectors.summarizingInt(function)).getSum();
    }

    public static <T> long sumLong(List<T> datas, ToLongFunction<T> function) {
        return datas.stream().mapToLong(function).sum();
    }

    public static <T> double sumDouble(List<T> datas, ToDoubleFunction<T> function) {
        return datas.stream().mapToDouble(function).sum();
    }

    public static <T> IntStream intStream(List<T> datas, ToIntFunction<T> function) {
        return datas.stream().mapToInt(function);
    }

    public static <T> LongStream longStream(List<T> datas, ToLongFunction<T> function) {
        return datas.stream().mapToLong(function);
    }

    public static <T> DoubleStream doubleStream(List<T> datas, ToDoubleFunction<T> function) {
        return datas.stream().mapToDouble(function);
    }
}
