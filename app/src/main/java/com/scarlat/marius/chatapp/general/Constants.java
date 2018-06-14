package com.scarlat.marius.chatapp.general;

public class Constants {

    /* Default Element values */
    public static final String UNSET = "Unset";
    public static final String DEFAULT_STATUS_VAL = "Available";
    public static final String RESULT_OK = "SUCCESS";
    public static final String RESULT_ERROR = "ERROR";

    /* Main tabs fragments */
    public static final String COLOR_ACTIVE_TAB = "#00bfff";
    public static final String COLOR_INACTIVE_TAB = "#4d4d4d";

    /* Requests Codes */
    public static final int REQUEST_CODE_READ_EXT = 8;
    public static final int REQUEST_CODE_CAMERA = 9;
    public static final int REQUEST_CODE_WRITE_EXT = 10;
    public static final int REQUEST_CODE_SIGN_IN = 21;

    /* User Info */
    public static final String TOKEN_ID = "tokenId";
    public static final String USER_ID = "userId";
    public static final String EMAIL = "email";
    public static final String FULLNAME = "fullname";
    public static final String STATUS = "status";
    public static final String NUMBER_OF_FRIENDS = "numberOfFriends";
    public static final String PROFILE_IMAGE = "profileImage";

    /* Profile picture resize */
    public static final int MAX_WIDTH = 600;
    public static final int MAX_HEIGHT = 600;

    /* Friend request */
    public static final String REQUEST_TYPE = "request_type";
    public static final String REQUEST_TYPE_SENT = "sent";
    public static final String REQUEST_TYPE_RECEIVED = "received";


    /* Notifications */
    public static final String CHANNEL_ID = "notify_001";
    public static final String SOURCE = "from";
    public static final String DESTINATION = "to";
    public static final String NOTIFICATION_TYPE = "type";
    public static final String NOTIFICATION_FRIEND_REQUEST = "request";

    public static final String STATE_FRIENDS = "friends";

    public static final String[] REQUEST_STATES = {
            UNSET,                      /* 0:   User can perform Send request */
            REQUEST_TYPE_SENT,          /* 1:   User can perform Cancel request */
            REQUEST_TYPE_RECEIVED,      /* 2:   User can accept or decline the request */
            STATE_FRIENDS               /* 3:   Users are friends */
    };

    /* Storage */
    public static final String USER_PREFERENCES = "user_info_shared_pref";
    public static final String CREDENTIALS_CHECKBOX = "remember_credentials_checkbox";
    public static final String PASSWORD = "password";
    // Also EMAIL, FULLNAME

    /* Services Status */
    public static final boolean ACTIVE = true;
    public static final boolean INACTIVE = false;

}
