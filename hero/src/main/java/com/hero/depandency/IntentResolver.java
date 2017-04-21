package com.hero.depandency;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by drjr on 17-4-21.
 */

public class IntentResolver {

    public static final int REQUEST_CODE_CAMERA = 3001;
    public static final int REQUEST_CODE_GALLERY = 3002;
    public static final int REQUEST_CODE_PHOTO_CROP = 3003;
    public static final int REQUEST_CODE_PICK_CONTACT = 3100;
    public static final int REQUEST_IMAGE = 2000;

    public static JSONObject resolveContact(Context context, Intent data){
        String phoneNumber = null, name = null;
        JSONObject item = new JSONObject();
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                Cursor c = null;
                Cursor phone = null;
                try {
                    c = context.getContentResolver().query(uri, new String[] {BaseColumns._ID, ContactsContract.Contacts.DISPLAY_NAME,}, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        int id = c.getInt(0);
                        name = c.getString(1);
                        phone = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
                        if (phone != null) {
                            while (phone.moveToNext()) {
                                String num = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                phoneNumber = num;
                            }
                            phone.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
            if (phoneNumber != null) {
                try {
                    item.put("contactName", name == null ? "" : name);
                    item.put("contactNumber", phoneNumber);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    item.put("contactName", name == null ? "" : name);
                    item.put("contactNumber", "");
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            if (name == null && phoneNumber == null) {
                                item.put("error", "empty");
                            }
                        }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    return item;
    }
}
