package io.terminus.doctor.common.utils;

import java.util.Random;

/**
 * Desc: 随机数工具类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public class RandomUtil {

    private static final Random random = new Random();

    /**
     * 生成a到b之间的随机数
     * @param a 最小值
     * @param b 最大值
     * @return 随机数
     */
    public static int random(int a, int b) {
        int min = getMin(a, b);
        int max = getMax(a, b);
        return random.nextInt(max - min + 1) + min;
    }

    private static int getMin(int a, int b) {
        return Math.min(a, b);
    }

    private static int getMax(int a, int b) {
        return Math.max(a, b);
    }
}
