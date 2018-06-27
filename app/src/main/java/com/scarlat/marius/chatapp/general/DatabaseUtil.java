package com.scarlat.marius.chatapp.general;


import com.google.firebase.database.FirebaseDatabase;

public class DatabaseUtil {

    private static boolean firstContact = false;

    public static void setupDatabase() {

        if (! firstContact) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            firstContact = true;
        }

    }



}
