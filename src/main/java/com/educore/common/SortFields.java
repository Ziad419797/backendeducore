package com.educore.common;

import java.util.List;

public class SortFields {

    public static final List<String> WEEK =
            List.of("id", "title", "orderNumber", "createdAt");
    public static final List<String> SESSION =
            List.of("id", "title", "orderNumber", "createdAt");
    public static final List<String> COURSE =
            List.of("id", "title", "orderNumber");
    public static final List<String> CATEGORY =
            List.of("id", "title", "orderNumber", "createdAt");

    public static final List<String> LEVEL =
            List.of("id", "title");
    public static final List<String> LESSONMaterial =
            List.of("id", "title", "orderNumber", "createdAt");

}
