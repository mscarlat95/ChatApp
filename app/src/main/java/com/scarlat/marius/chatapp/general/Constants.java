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
    public static final int REQUEST_CODE_ENABLE_GPS = 14;

    /* User Info */
    public static final String TOKEN_ID = "tokenId";
    public static final String USER_ID = "userId";
    public static final String EMAIL = "email";
    public static final String FULLNAME = "fullname";
    public static final String STATUS = "status";
    public static final String NUMBER_OF_FRIENDS = "numberOfFriends";
    public static final String PROFILE_IMAGE = "profileImage";
    public static final String ONLINE = "online";
    public static final String LAST_SEEN = "lastSeen";

    /* Message Info */
    public static final String SEEN = "seen";
    public static final String TIMESTAMP = "timestamp";
    public static final String MESSAGE_CONTENT = "message";
    public static final String MESSAGE_TYPE = "type";
    public static final String MESSAGE_TYPE_TEXT = "text";
    public static final String MESSAGE_TYPE_IMAGE= "image";

    public static final int MAX_LOAD_MESSAGES = 10;

    /* Profile picture resize */
    public static final int MAX_WIDTH = 600;
    public static final int MAX_HEIGHT = 600;

    /* Friend request */
    public static final String REQUEST_TYPE = "request_type";
    public static final String REQUEST_TYPE_SENT = "sent";
    public static final String REQUEST_TYPE_RECEIVED = "received";
    public static final String FRIENDS_SINCE = "friendshipDate";

    /* Database Tables */
    public static final String USERS_TABLE = "Users";
    public static final String FRIEND_REQUESTS_TABLE = "FriendRequests";
    public static final String NOTIFICATIONS_TABLE = "Notifications";
    public static final String FRIENDS_TABLE = "Friends";
    public static final String CHAT_TABLE = "Chat";
    public static final String MESSAGES_TABLE = "Messages";

    /* Server Storage */
    public static final String STORAGE_PROFILE_IMAGES = "profile_images";
    public static final String STORAGE_MESSAGE_IMAGES = "message_images";

    /* Notifications channel for OREO android version */
    public static final String CHANNEL_ID = "notify_001";
    public static final String CHANNEL_NAME = "ChatApp Notification Channel";
    public static final String CHANNEL_DESCRIPTION = "Notification Channel used for FirebaseMessaging";

    /* Notification info */
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

    /* Offline Intent filters */
    public static final String ACTION_PEER_CONNECTED = "com.action.onPeerConnected";
    public static final String ACTION_PEER_DISCONNECTED = "com.action.onPeerDisconnected";
    public static final String ACTION_MESSAGE_RECEIVED = "com.action.onMessageReceived";

    public static final String FINISH_MESSAGE =     "\n-- The conversation has been interrupted. --\n" +
                                                    "-- There is no guarantee that your next messages will arrive to the destination. --\n";

    /* Geolocation */
    public static final String ACTION_LOCATION_UPDATE = "com.action.userLocation";
    public static final String USER_LONGITUDE = "longitude";
    public static final String USER_LATITUDE = "latitude";
    public static final String USER_LOCATION = "location";

    /* Hyccups Install */
    public static final String URL_HYCCUPS_HOME = "http://www.smartrdi.net/2017/11/08/getting-started";
    public static final String URL_HYCCUPS_DOWNLOAD = "http://cipsm.hpc.pub.ro/hyccups-javadoc/downloads/hyccups-tracer-5.1-vc14.apk";
    public static final String HYCCUPS_PREFERENCES = "hyccups_preferences";
    public static final String HYCCUPS_PACKAGE_NAME = "ro.pub.acs.hyccups";

    public static final String HIDE_INSTALL = "hide_install";

}
