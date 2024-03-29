package fr.oqom.ouquonmange.models;

import java.text.SimpleDateFormat;

public class Constants {
    public static final String OQOM_BASE_URL = "http://ouquonmange.berezovskiy.fr";
    public static final String _OQOM_BASE_URL = "http://berezovskiy.fr:9007";
    public static final String REGEX_PASSWORD = "^[A-Za-z0-9]{6,30}$";
    public static final String REGEX_NAME_COMMUNITY = "^[A-Za-z]{4,20}$";
    public static final int MIN_LENGTH_NAME_COMMUNITY = 2;
    public static final int MAX_LENGTH_NAME_COMMUNITY = 30;

    public static final String COMMUNITY_UUID = "COMMUNITY_UUID";
    public static final String COMMUNITY = "COMMUNITY";
    public static final String EVENT_UUID = "EVENT_UUID";

    public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static final String INTEREST_POINT_ID = "INTEREST_POINT_ID";
    public static final String EVENT_DATE = "EVENT_DATE";
    public static final String INTEREST_POINT = "INTEREST_POINT";
    public static final String COMMUNITIES_LIST = "COMMUNITIES_LIST";
    public static final String EVENTS_LIST = "EVENTS_LIST";
    public static final String INTEREST_POINTS_LIST = "INTEREST_POINTS_LIST";
    public static final String FROM_MENU = "FROM_MENU";
    public static final String NAME_MEMBERS = "Members" ;
    public static final String DEFAULT_NUMBERS_OF_MEMBERS = "0";
    public static final String MEMBERS_LIST = "MEMBERS_LIST";
    public static final String CREATED_COMMUNITY = "CREATED_COMMUNITY";
    public static final String MEMBERS = "member";
    public static final String MEMBER_STATUS_ACCEPTED = "accepted";
    public static final String MEMBER_STATUS_PENDING = "pending";
    public static final String MEMBER_STATUS_REJECTED = "rejected";
    public static final String INTEREST_POINT_DETAILS = "INTEREST_POINT_DETAILS";
    public static final String EVENT_DATE_START = "EVENT_DATE_START";
    public static final String EVENT_DATE_END = "EVENT_DATE_END";
    public static int NUMBER_OF_HOURS_BETWEEN_TWO_DATE = 12;
    public static long DIFFERENT_HOURS = 12 * 60 * 60 ; // difference entre le date de debut et le fin (event)
    public static int NUMBER_OF_HOURS_IN_THE_PAST_FOR_EVENT = 1;
}
