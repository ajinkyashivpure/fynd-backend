package com.assess.backend;

import java.util.ArrayList;
import java.util.List;

public class sample {

    public static void main(String[] args) {
        List<String> categories = new ArrayList<>();
        categories.add("cat");
        categories.add("dog");
        categories.add("mouse");
        String text ="";
        for (String category : categories) {
            text += category + ".";
        }
        System.out.println(text);
    }

}
