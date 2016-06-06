package io.terminus.doctor.common.utils;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

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
        return datas.stream().collect(Collectors.summarizingLong(function)).getSum();
    }

    public static <T> double sumDouble(List<T> datas, ToDoubleFunction<T> function) {
        return datas.stream().collect(Collectors.summarizingDouble(function)).getSum();
    }

    public static <T> IntSummaryStatistics intStatistics(List<T> datas, ToIntFunction<T> function) {
        return datas.stream().collect(Collectors.summarizingInt(function));
    }

    public static <T> LongSummaryStatistics longStatistics(List<T> datas, ToLongFunction<T> function) {
        return datas.stream().collect(Collectors.summarizingLong(function));
    }

    public static <T> DoubleSummaryStatistics doubleStatistics(List<T> datas, ToDoubleFunction<T> function) {
        return datas.stream().collect(Collectors.summarizingDouble(function));
    }
}
