/**
 * BSD License
 * Copyright (c) Hero software.
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.

 * Neither the name Facebook nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.

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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.widget.TextView;

import com.hero.depandency.MPermissionUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rx.functions.Action1;

public class HeroContactView extends TextView implements IHero {
    private static final String TAG = "HeroContactView";
    private JSONObject contactObject;
    private JSONObject callsObject;

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
            }
            //            pickContact();
            MPermissionUtils.requestPermissionAndCall(getContext(), Manifest.permission.READ_CONTACTS, new Action1<Boolean>() {
                @Override
                public void call(Boolean aBoolean) {
                    if (aBoolean) {
                        postContactsData();
                    } else {
                        postFailure(contactObject);
                    }
                }
            });
        }
        if (jsonObject.has("getRecent")) {
            if (jsonObject.get("getRecent") instanceof JSONObject) {
                callsObject = jsonObject.getJSONObject("getRecent");
            }
            MPermissionUtils.requestPermissionAndCall(getContext(), Manifest.permission.READ_CALL_LOG, new Action1<Boolean>() {
                @Override
                public void call(Boolean aBoolean) {
                    if (aBoolean) {
                        postCallLogs();
                    } else {
                        postFailure(callsObject);
                    }
                }
            });
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
                    ((IHeroContext) this.getContext()).on(contactObject);
                }
            }
        }
    }

    private void postContactsData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONArray jsonArray = getAllContacts();
                if (jsonArray != null) {
                    if (jsonArray.length()==0&& Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                    {
                        postFailure(contactObject);
                        return;
                    }
                    JSONObject value = new JSONObject();
                    try {
                        value.put("contacts", jsonArray);
                        contactObject.put("value", value);
                        ((IHeroContext) getContext()).on(contactObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    postFailure(contactObject);
                }
            }
        }).start();
    }

    private void postCallLogs() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONArray jsonArray = getAllCallLogs();
                if (jsonArray != null) {
                    JSONObject value = new JSONObject();
                    try {
                        value.put("callHistories", jsonArray);
                        callsObject.put("value", value);
                        ((IHeroContext) getContext()).on(callsObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    postFailure(callsObject);
                }
            }
        }).start();
    }

    private void postFailure(JSONObject content) {
        JSONObject value = new JSONObject();
        try {
            value.put("error", "fail");
            content.put("value", value);
            ((IHeroContext) getContext()).on(content);
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

    private JSONArray getAllCallLogs() {
        JSONArray array = new JSONArray();

        if (!requestCalllogPermission()) {
            return null;
        }
        final ContentResolver resolver = getContext().getContentResolver();
        Uri uri;
        uri = CallLog.Calls.CONTENT_URI;
        //        uri = uri.buildUpon().appendQueryParameter("address_book_index_extras", "true").build();

        String[] projection = new String[] {CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.TYPE};
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

    private boolean requestContactPermission() {
        return MPermissionUtils.checkAndRequestPermission(getContext(), Manifest.permission.READ_CONTACTS, MPermissionUtils.HERO_PERMISSION_CONTACTS);
    }

    private boolean requestCalllogPermission() {
        return MPermissionUtils.checkAndRequestPermission(getContext(), Manifest.permission.READ_CALL_LOG, MPermissionUtils.HERO_PERMISSION_CALLLOG);
    }
}
