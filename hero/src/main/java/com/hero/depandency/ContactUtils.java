package com.hero.depandency;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by xincai on 17-3-28.
 */

public class ContactUtils {

    public static Map<Integer, Contact> getContacts(Context context) {
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, null, null, null, ContactsContract.Data.RAW_CONTACT_ID);
        if (cursor == null) {
            return null;
        } else {
            HashMap map = new HashMap();
            while (cursor.moveToNext()) {
                try {
                    int raw_id = cursor.getInt(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
                    // find an item with same raw id in list
                    Contact matchedContact = (Contact) map.get(raw_id);
                    if (matchedContact == null) {
                        // not found, create one
                        matchedContact = new Contact();
                        map.put(raw_id, matchedContact);
                    }

                    String mimeType = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
                    if (TextUtils.equals(mimeType, StructuredName.CONTENT_ITEM_TYPE)) {
                        // it's a name type
                        String familyName = cursor.getString(cursor.getColumnIndex(StructuredName.FAMILY_NAME));
                        String givenName = cursor.getString(cursor.getColumnIndex(StructuredName.GIVEN_NAME));

                        StringBuffer stringBuffer = new StringBuffer();
                        if (!TextUtils.isEmpty(familyName)) {
                            stringBuffer.append(familyName);
                        }

                        if (!TextUtils.isEmpty(givenName)) {
                            stringBuffer.append(givenName);
                        }
                        matchedContact.name = stringBuffer.toString();
                    } else if (TextUtils.equals(mimeType, Phone.CONTENT_ITEM_TYPE)) {
                        // it's a number type
                        String phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                        if (!TextUtils.isEmpty(phone)) {
                            if (matchedContact != null && matchedContact.phone == null) {
                                matchedContact.phone = phone;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            return map;
        }
    }

    public static JSONArray getContactsArray(Context context) {
        Map map = ContactUtils.getContacts(context);
        if (map == null) {
            return null;
        }
        JSONArray array = new JSONArray();

        Iterator i = map.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            Contact contact = (Contact) entry.getValue();
            if (contact != null && !TextUtils.isEmpty(contact.phone)) {
                JSONObject validItem = new JSONObject();
                try {
                    validItem.put("phone", contact.phone);
                    validItem.put("name", contact.name == null ? contact.phone : contact.name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                array.put(validItem);
            }
        }
        return array;
    }

    static class Contact {
        public String name;
        public String phone;
    }
}
