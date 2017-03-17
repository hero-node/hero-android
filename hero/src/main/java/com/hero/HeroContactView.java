/**
 * BSD License
 * Copyright (c) Hero software.
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * Neither the name Facebook nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.hero;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.widget.TextView;

import com.hero.depandency.MPermissionUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rx.functions.Action1;

public class HeroContactView extends TextView implements IHero {

    private static final String TAG = "HeroContactView";
    public static final int SMS_POST_COUNT = 500;

    private JSONObject contactObject;
    private JSONObject filterObject;

    private BroadcastReceiver broadcastReceiver;

    private Handler postDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null && msg.obj instanceof JSONObject) {
                HeroView.sendActionToContext(getContext(), (JSONObject) msg.obj);
            }
        }
    };

    public HeroContactView(Context context) {
        super(context);
        this.setVisibility(INVISIBLE);
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        if (jsonObject.has("getContact")) {
            if (jsonObject.get("getContact") instanceof JSONObject) {
                contactObject = jsonObject.getJSONObject("getContact");
                MPermissionUtils.requestPermissionAndCall(getContext(), Manifest.permission.READ_CONTACTS, new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            postContactsData(contactObject);
                        } else {
                            postFailure(contactObject);
                        }
                    }
                });
            }

        }
        if (jsonObject.has("getRecent")) {
            if (jsonObject.get("getRecent") instanceof JSONObject) {
                final JSONObject callsObject = jsonObject.getJSONObject("getRecent");
                MPermissionUtils.requestPermissionAndCall(getContext(), Manifest.permission.READ_CALL_LOG, new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            postCallLogs(callsObject);
                        } else {
                            postFailure(callsObject);
                        }
                    }
                });
            }

        }
        if (jsonObject.has("getSms")) {
            if (jsonObject.get("getSms") instanceof JSONObject) {

                final JSONObject smsObject = jsonObject.getJSONObject("getSms");
                MPermissionUtils.requestPermissionAndCall(getContext(), Manifest.permission.READ_SMS, new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            postSms(smsObject);
                        } else {
                            postFailure(smsObject);
                        }
                    }
                });
            }
        }

        if (jsonObject.has("filterReceivedSms")) {
            if (jsonObject.get("filterReceivedSms") instanceof JSONObject) {
                filterObject = jsonObject.getJSONObject("filterReceivedSms");

                MPermissionUtils.requestPermissionAndCall(getContext(), Manifest.permission.READ_SMS, new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            postFilterSms(filterObject);
                        } else {
                            postFailure(filterObject);
                        }
                    }
                });
            }
        }
        if (jsonObject.has("contactName") || jsonObject.has("contactNumber")) {
            if (contactObject != null) {
                if (jsonObject.has("error")) { // got fail
                    JSONObject value = new JSONObject();
                    try {
                        value.put("error", jsonObject.getString("error"));
                        contactObject.put("value", value);
                        ((IHeroContext) getContext()).on(contactObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    contactObject.put("name", jsonObject.getString("contactName"));
                    contactObject.put("phone", jsonObject.getString("contactNumber"));
                    HeroView.sendActionToContext(getContext(), contactObject);
                }
            }
        }
    }

    private void postContactsData(final JSONObject contactObject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONArray jsonArray = getAllContacts();
                if (jsonArray != null) {
                    if (jsonArray.length() == 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        postFailure(contactObject);
                        return;
                    }
                    JSONObject value = new JSONObject();
                    try {
                        value.put("contacts", jsonArray);
                        contactObject.put("value", value);
                        postDataHandler.sendMessage(generateMessage(contactObject));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    postFailure(contactObject);
                }
            }
        }).start();
    }

    private void postCallLogs(final JSONObject callsObject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONArray jsonArray = getAllCallLogs();
                if (jsonArray != null) {
                    JSONObject value = new JSONObject();
                    try {
                        value.put("callHistories", jsonArray);
                        callsObject.put("value", value);
                        postDataHandler.sendMessage(generateMessage(callsObject));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    postFailure(callsObject);
                }
            }
        }).start();
    }

    private void postFilterSms(final JSONObject object) {
        String phone="";
        String contain="";
        if (object.has("phone"))
        {
            phone=object.optString("phone");
        }
        if (object.has("contain"))
        {
            contain=object.optString("contain");
        }
        broadcastReceiver = new SmsReceiveBroadcastReceiver(getContext(), postDataHandler,phone,contain);
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        getContext().registerReceiver(broadcastReceiver, intentFilter);
    }


    private void postSms(final JSONObject jsonObject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = getAllSms();
                    if (jsonArray != null) {
                        if (jsonArray.length() == 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            postFailure(jsonObject);
                            return;
                        }
                        if (SMS_POST_COUNT < jsonArray.length()) {
                            // if sms count is too much, send them every SMS_POST_COUNT items
                            JSONArray array = new JSONArray();
                            for (int count = 0; count < jsonArray.length(); count++) {
                                if (array.length() == SMS_POST_COUNT) {
                                    array = new JSONArray();
                                }
                                array.put(jsonArray.get(count));
                                if (array.length() == SMS_POST_COUNT || count == jsonArray.length() - 1) {
                                    JSONObject copyOfObject = new JSONObject(jsonObject.toString());
                                    postSmsDataOnce(copyOfObject, array);
                                    Thread.sleep(500);
                                }
                            }
                        } else {
                            postSmsDataOnce(jsonObject, jsonArray);
                        }
                    } else {
                        postFailure(jsonObject);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    private void postSmsDataOnce(JSONObject jsonObject, JSONArray array) {
        JSONObject value = new JSONObject();
        try {
            value.put("smsList", array);
            value.put("system", "ANDROID");
            HeroView.putValueToJson(jsonObject, value);
            postDataHandler.sendMessage(generateMessage(jsonObject));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void postFailure(JSONObject content) {
        JSONObject value = new JSONObject();
        try {
            value.put("error", "fail");
            HeroView.putValueToJson(content, value);
            postDataHandler.sendMessage(generateMessage(content));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getAllContacts() {
        JSONArray array = new JSONArray();

        if (!requestContactPermission()) {
            return null;
        }
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String phoneNumber = "";

                Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                if (phones != null) {
                    while (phones.moveToNext()) {
                        phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    phones.close();
                }
                JSONObject item = new JSONObject();
                try {
                    item.put("phone", phoneNumber);
                    item.put("name", name);
                    array.put(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
            cursor.close();
        }

        return array;
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        if (getContext() instanceof HeroActivity) {
            ((HeroActivity) (getContext())).startActivityByView(intent, HeroActivity.REQUEST_CODE_PICK_CONTACT, this);
        }
    }


    private JSONArray getAllSms() {
        final String SORT_ORDER = "date DESC";
        final String SMS_URI = "content://sms";
        final String SMS_COL_BODY = "body";
        final String SMS_COL_ADDRESS = "address";
        final String SMS_COL_PERSON = "person";
        final String SMS_COL_DATE = "date";
        final String SMS_COL_TYPE = "type";

        JSONArray jsonArray = new JSONArray();
        if (!requestSmsPermission()) {
            return jsonArray;
        }

        final ContentResolver resolver = getContext().getContentResolver();
        Uri uri = Uri.parse(SMS_URI);

        String[] projection = new String[]{SMS_COL_ADDRESS, SMS_COL_DATE, SMS_COL_BODY, SMS_COL_TYPE, SMS_COL_PERSON};
        String selection = "type < 3";
        String sortOrder = SORT_ORDER;

        Cursor cursor = resolver.query(uri, projection, null, null, sortOrder);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                JSONObject item = new JSONObject();
                try {
                    String address = cursor.getString(0);
                    long date = cursor.getLong(1);
                    String body = cursor.getString(2);
                    int type = cursor.getInt(3);
                    String name = cursor.getString(4);
                    item.put("address", address);
                    item.put("date", date);
                    item.put("body", body);
                    item.put("name", name);
                    // 1 : inbox; 2 : outbox
                    item.put("type", type == 1 ? "INBOX" : "OUTBOX");
                    jsonArray.put(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return jsonArray;
    }

    private JSONArray getAllCallLogs() {
        JSONArray array = new JSONArray();

        if (!requestCalllogPermission()) {
            return null;
        }
        final ContentResolver resolver = getContext().getContentResolver();
        Uri uri;
        uri = CallLog.Calls.CONTENT_URI;
        //        uri = uri.buildUpon().appendQueryParameter("address_book_index_extras", "true").build();

        String[] projection = new String[]{CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.TYPE};
        String[] selectionArgs = null;
        String sortOrder = CallLog.Calls.DATE + " desc";

        try {
            Cursor callCursor = resolver.query(uri, projection, null, selectionArgs, sortOrder);
            if (callCursor != null && callCursor.moveToFirst()) {
                do {
                    if (callCursor.getInt(4) != CallLog.Calls.MISSED_TYPE) {
                        JSONObject item = new JSONObject();
                        try {
                            item.put("phone", callCursor.getString(0));
                            item.put("name", callCursor.getString(1));
                            item.put("callTime", callCursor.getString(2));
                            item.put("duration", callCursor.getString(3));
                            item.put("callType", callCursor.getInt(4) == CallLog.Calls.INCOMING_TYPE ? "CALLIN" : "CALLOUT");
                            array.put(item);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } while (callCursor.moveToNext());
                callCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }

    private boolean requestSmsPermission() {
        return MPermissionUtils.checkAndRequestPermission(getContext(), Manifest.permission.READ_SMS, MPermissionUtils.HERO_PERMISSION_SMS);
    }

    private boolean requestContactPermission() {
        return MPermissionUtils.checkAndRequestPermission(getContext(), Manifest.permission.READ_CONTACTS, MPermissionUtils.HERO_PERMISSION_CONTACTS);
    }

    private boolean requestCalllogPermission() {
        return MPermissionUtils.checkAndRequestPermission(getContext(), Manifest.permission.READ_CALL_LOG, MPermissionUtils.HERO_PERMISSION_CALLLOG);
    }

    private Message generateMessage(JSONObject object) {
        Message message = postDataHandler.obtainMessage();
        message.obj = object;
        return message;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (broadcastReceiver!=null)
        {
            getContext().unregisterReceiver(broadcastReceiver);
        }
    }

    public class SmsReceiveBroadcastReceiver extends BroadcastReceiver {

        private Context context;
        private Handler handler;
        private String phoneValue;
        private String containValue;

        public SmsReceiveBroadcastReceiver(Context context, Handler handler, String phone,String contain) {
            // TODO Auto-generated constructor stub
            this.context = context;
            this.handler = handler;
            this.phoneValue = phone;
            this.containValue=contain;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Bundle bundle = intent.getExtras();
            Object[] objects = (Object[]) bundle.get("pdus");
            if (objects != null) {
                String phone = null;
                StringBuffer content = new StringBuffer();
                for (int i = 0; i < objects.length; i++) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) objects[i]);
                    phone = sms.getDisplayOriginatingAddress();
                    content.append(sms.getDisplayMessageBody());
                }
                if (checkPhoneOrContent(phone,content.toString()))
                {
                    JSONObject value = new JSONObject();
                    try {
                        value.put("phone", phone);
                        value.put("content", content);
                        HeroView.putValueToJson(filterObject, value);
                        handler.sendMessage(generateMessage(filterObject));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        private boolean checkPhoneOrContent(String phone,String content) {
            if (TextUtils.isEmpty(phone)||TextUtils.isEmpty(content))
            {
                return false;
            }

            if (phone.contains(phoneValue)&&content.contains(containValue))
            {
                return true;
            }   
            return false;
        }

    }


}
