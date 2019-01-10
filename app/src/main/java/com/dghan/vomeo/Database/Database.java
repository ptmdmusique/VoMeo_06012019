package com.dghan.vomeo.Database;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Database {
    /*public static String username;
    public static String alarm;//sua thanh type gi do khac
    public static boolean darkMode;

    // Words list
    public ArrayList<Integer> levelLength;
    public ArrayList<String> terms, definitions, notes;
    public ArrayList<Integer> level, records;
    public ArrayList<Boolean> favorite;*/
    public String username;
    public boolean darkMode;
    public static String alarm;//sua thanh type gi do khac
    public ArrayList<Word> words;
}
