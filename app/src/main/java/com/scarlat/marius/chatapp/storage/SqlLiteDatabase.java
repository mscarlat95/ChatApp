package com.scarlat.marius.chatapp.storage;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.Message;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SqlLiteDatabase {
    private static final String TAG = "SqlLiteDatabase";

    public static void retrieveLastMessages(Context context, final String tableName, final List<Message> messages) {
        Log.d(TAG, "retrieveLastMessages: Method was invoked!");

        try {
            /* Obtain database */
            SQLiteDatabase db = context.openOrCreateDatabase(Constants.USERS_TABLE, MODE_PRIVATE, null);
            Cursor c = db.rawQuery("SELECT * FROM " + tableName, null);

            int idIndex = c.getColumnIndex("id");
            int messageIndex = c.getColumnIndex("message");
            int timestampIndex = c.getColumnIndex("timestamp");

            /* Move cursor to the first result */
            c.moveToFirst();

            /* Add all previous messages stored in database */
            while (!c.isAfterLast()) {
                final String id = c.getString(idIndex);
                final String content = c.getString(messageIndex);
                final long timestamp = c.getLong(timestampIndex);

                Log.d(TAG, "retrieveLastMessages: "  + id + "; " + content + "; " + timestamp);

                Message message = new Message();
                message.setFrom(id);
                message.setMessage(content);
                message.setTimestamp(timestamp);

                messages.add(message);

                c.moveToNext();
            }

            c.close();

        } catch (Exception e) {
            Log.d(TAG, "retrieveLastMessages: [Exception]" + e.getMessage());
        }
    }


    public static void saveMessages(Context context, final String tableName, final List<Message> messages) {
        Log.d(TAG, "saveMessages: Method was invoked!");

        try {
            /* Save data in database */
            SQLiteDatabase db = context.openOrCreateDatabase(Constants.USERS_TABLE, MODE_PRIVATE, null);

            /* Save table in the database */
            db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (id VARCHAR, message VARCHAR, timestamp INT(30))");
            db.execSQL("DELETE FROM "  + tableName);

            /* Insert data */
            for (Message message : messages) {
                final String id = message.getFrom();
                final String content = message.getMessage();
                final long timestamp = message.getTimestamp();

                final String pushedValue = "('" + id + "', " + "'" + content + "', " + timestamp + ")";
                db.execSQL("INSERT INTO " + tableName + " (id,message,timestamp) VALUES " + pushedValue);

                Log.d(TAG, "saveMessages: " + id + "; " + content + "; " + timestamp);
            }
        } catch (Exception e) {
            Log.d(TAG, "saveMessages: [Exception]"  + e.getMessage());
        }
    }

    public static void deleteAllMessages(Context context, final String tableName) {

        /* Delete records from database */
        SQLiteDatabase db = context.openOrCreateDatabase(Constants.USERS_TABLE, MODE_PRIVATE, null);
        db.execSQL("DELETE FROM "  + tableName);

    }
}
