package com.hero.depandency;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import rx.functions.Action1;

/**
 * Created by XinXia on 17-4-12.
 */

public class ReminderDelegate {

    private static final String[] EVENT_PROJECTION = new String[]{
            Calendars._ID,                           // 0
            Calendars.ACCOUNT_NAME,                  // 1
            Calendars.CALENDAR_DISPLAY_NAME,         // 2
            Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private static final String TAG = "ReminderDelegate";

    private static final int CALENDAR_TOKEN = 1;

    private static final int EVENT_TOKEN = 2;

    private static final int REMINDER_TOKEN = 3;

    private final List<Long> calendarIDs = new ArrayList<>();

    private long eventID;

    private final WeakReference<Context> contextWeakReference;

    private CalendarQueryHandler queryHandler;

    public ReminderDelegate(Context context){
        contextWeakReference = new WeakReference<>(context);
    }

    public static class ReminderInfo{
        long startMillis;
        long endMillis;
        String title;
        String description;

        public ReminderInfo(long startMillis,long endMillis,String title,String description){
            this.startMillis = startMillis;
            this.endMillis = endMillis;
            this.title = title;
            this.description = description;
        }
    }

    interface Action{
        void run();
    }

    public void setReminder(final ReminderInfo info){
        final Context context = contextWeakReference.get() == null?null: contextWeakReference.get();
        if (context == null){
            Log.e(TAG,"Activity been destroyed, can not set reminder");
            return;
        }
        if (calendarIDs.isEmpty()){
            queryCalendarList(context, new Action() {
                @Override
                public void run() {
                    insertEvent(context, calendarIDs.get(0), info, new Action() {
                        @Override
                        public void run() {
                            insertReminder(context);
                        }
                    });
                }
            });
        }
        else {
            insertEvent(context, calendarIDs.get(0), info, new Action() {
                @Override
                public void run() {
                    insertReminder(context);
                }
            });
        }
    }

    public void destroy(){
        if (queryHandler != null){
            queryHandler.removeCallbacksAndMessages(0);
            queryHandler = null;
        }
    }

    private void queryCalendarList(Context context,Action action){
        if (queryHandler == null){
            queryHandler = new CalendarQueryHandler(context.getContentResolver());
        }
        queryHandler.queryAllCalendar(context,action);
    }

    private void insertEvent(Context context,long calID,ReminderInfo info,Action action){
        if (queryHandler == null){
            queryHandler = new CalendarQueryHandler(context.getContentResolver());
        }
       queryHandler.insertEventInCalendar(context,info,calID,action);
    }

    private void insertReminder(Context context){
        if (queryHandler == null){
            queryHandler = new CalendarQueryHandler(context.getContentResolver());
        }
       queryHandler.insertReminder(context,null);
    }


    private class CalendarQueryHandler extends AsyncQueryHandler {

         CalendarQueryHandler(ContentResolver cr) {
            super(cr);
        }

        void queryAllCalendar(Context context,final Action action) {
                MPermissionUtils.requestPermissionsAndCall(context, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            Uri uri = Calendars.CONTENT_URI;
                            String selection = "(" + Calendars.ACCOUNT_TYPE + " = ?)";
                            String[] selectionArgs = new String[]{CalendarContract.ACCOUNT_TYPE_LOCAL};
                            startQuery(CALENDAR_TOKEN, action, uri, EVENT_PROJECTION, selection, selectionArgs, null);
                        }
                        else {
                            Log.e(TAG,"Permission not granted!");
                        }
                    }
                });
        }
        void insertReminder(Context context,final Action action){
            MPermissionUtils.requestPermissionsAndCall(context, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, new Action1<Boolean>() {
                @Override
                public void call(Boolean aBoolean) {
                    if (aBoolean){
                        ContentValues values = new ContentValues();
                        values.put(Reminders.MINUTES, 15);
                        values.put(Reminders.EVENT_ID, eventID);
                        values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
                        queryHandler.startInsert(REMINDER_TOKEN,null,Reminders.CONTENT_URI,values);
                    }
                    else {
                        Log.e(TAG,"Permission not granted!");
                    }
                }
            });

        }
        void insertEventInCalendar(Context context,final ReminderInfo info,final long calID,final Action action){
            MPermissionUtils.requestPermissionsAndCall(context, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, new Action1<Boolean>() {
                @Override
                public void call(Boolean aBoolean) {
                    if (aBoolean){
                        ContentValues values = new ContentValues();
                        values.put(Events.DTSTART, info.startMillis);
                        values.put(Events.DTEND,info.endMillis);
                        values.put(Events.TITLE, info.title);
                        values.put(Events.DESCRIPTION, info.description);
                        values.put(Events.CALENDAR_ID, calID);
                        values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
                        queryHandler.startInsert(EVENT_TOKEN,action,Events.CONTENT_URI,values);
                    }
                    else {
                        Log.e(TAG,"Permission not granted!");
                    }
                }
            });

        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor == null){
                return;
            }
            if (token == CALENDAR_TOKEN){
                // Use the cursor to step through the returned records
                while (cursor.moveToNext()) {
                    long calID ;
                    String displayName,accountName,ownerName ;
                    // Get the field values
                    calID = cursor.getLong(PROJECTION_ID_INDEX);
                    displayName = cursor.getString(PROJECTION_DISPLAY_NAME_INDEX);
                    accountName = cursor.getString(PROJECTION_ACCOUNT_NAME_INDEX);
                    ownerName = cursor.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

                    Log.d(TAG,"calID="+calID+" displayName="+displayName+" accountName="+accountName+" ownerName="+ownerName);
                    calendarIDs.add(calID);
                    if (cookie instanceof Action){
                        Action action = (Action)cookie;
                        action.run();
                    }
                }

            }
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {

            if (EVENT_TOKEN == token) {
                Log.d(TAG,"Event set done");
                if (uri == null){
                    return;
                }
                eventID = Long.parseLong(uri.getLastPathSegment());
                if (cookie instanceof Action){
                    Action action = (Action)cookie;
                    action.run();
                }
            }
            else if (REMINDER_TOKEN == token){
                // Reminder set done
                Log.d(TAG,"Reminder set done");
            }



        }
    }

}
