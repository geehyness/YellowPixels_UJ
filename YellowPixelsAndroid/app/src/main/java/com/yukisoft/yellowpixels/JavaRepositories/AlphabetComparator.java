package com.yukisoft.yellowpixels.JavaRepositories;

import java.util.Comparator;

public class AlphabetComparator implements Comparator<String> {
    public int compare(String a, String b) {
        int dateComparison = String.valueOf(b).compareTo(a);
        return dateComparison == 0 ? String.valueOf(b).compareTo(a) : dateComparison;
    }
}
