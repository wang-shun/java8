/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.common.banner;

import org.springframework.boot.Banner;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2015-11-22 10:43 PM  <br>
 * Author: xiao
 */
public class DoctorBanner implements Banner {
    private static final String[] BANNER = { "",
            " ________   ___   ________                   ________   ________   ________  _________   ________   ________     \n" +
            "|\\   __  \\ |\\  \\ |\\   ____\\                 |\\   ___ \\ |\\   __  \\ |\\   ____\\|\\___   ___\\|\\   __  \\ |\\   __  \\    \n" +
            "\\ \\  \\|\\  \\\\ \\  \\\\ \\  \\___|    ____________ \\ \\  \\_|\\ \\\\ \\  \\|\\  \\\\ \\  \\___|\\|___ \\  \\_|\\ \\  \\|\\  \\\\ \\  \\|\\  \\   \n" +
            " \\ \\   ____\\\\ \\  \\\\ \\  \\  ___ |\\____________\\\\ \\  \\ \\\\ \\\\ \\  \\\\\\  \\\\ \\  \\        \\ \\  \\  \\ \\  \\\\\\  \\\\ \\   _  _\\  \n" +
            "  \\ \\  \\___| \\ \\  \\\\ \\  \\|\\  \\\\|____________| \\ \\  \\_\\\\ \\\\ \\  \\\\\\  \\\\ \\  \\____    \\ \\  \\  \\ \\  \\\\\\  \\\\ \\  \\\\  \\| \n" +
            "   \\ \\__\\     \\ \\__\\\\ \\_______\\                \\ \\_______\\\\ \\_______\\\\ \\_______\\   \\ \\__\\  \\ \\_______\\\\ \\__\\\\ _\\ \n" +
            "    \\|__|      \\|__| \\|_______|                 \\|_______| \\|_______| \\|_______|    \\|__|   \\|_______| \\|__|\\|__|"
    };

    private static final String SPRING_BOOT = " :: Powered by Terminus.inc :: ";

    private static final int STRAP_LINE_SIZE = 42;

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass,
                            PrintStream printStream) {


        for (String line : BANNER) {
            printStream.println(line);
        }
        String version = "(v1.0)";
        String padding = "";
        while (padding.length() < STRAP_LINE_SIZE
                - (version.length() + SPRING_BOOT.length())) {
            padding += " ";
        }

        printStream.println(AnsiOutput.toString(AnsiColor.GREEN, SPRING_BOOT,
                AnsiColor.DEFAULT, padding, AnsiStyle.FAINT, version));
        printStream.println();
    }


}
