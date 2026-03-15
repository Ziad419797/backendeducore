package com.educore.student;

import java.util.Random;

public class StudentCodeGenerator {

    public static String generate() {
        int number = 1000 + new Random().nextInt(9000);
        return "G" + number;
    }
}
