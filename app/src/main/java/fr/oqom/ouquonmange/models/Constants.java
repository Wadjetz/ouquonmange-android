package fr.oqom.ouquonmange.models;

import java.text.SimpleDateFormat;

public class Constants {
    public static final String OQOM_BASE_URL = "http://ouquonmange.berezovskiy.fr";
    public static final String REGEX_PASSWORD = "^[A-Za-z0-9]{6,20}$";
    public static final String REGEX_NAME_COMMUNITY = "^[A-Za-z]{4,20}$";
    public static final int MIN_LENGTH_NAME_COMMUNITY = 2;
    public static final int MAX_LENGTH_NAME_COMMUNITY = 30;

    public static final String COMMUNITY_UUID = "COMMUNITY_UUID";
    public static final String EVENT_UUID = "EVENT_UUID";

    public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

    public static final String INTEREST_POINT_ID = "INTEREST_POINT_ID";
    public static final String EVENT_DATE = "EVENT_DATE";
    public static final String INTEREST_POINT = "INTEREST_POINT";
    public static final String COMMUNITIES_FRAGMENT = "COMMUNITIES_FRAGMENT";
    public static final String CREATE_COMMUNITY_FRAGMENT = "CREATE_COMMUNITY_FRAGMENT";
}
