package com.hero;

import android.content.Context;
import android.view.View;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Yuri on 2019/1/9.
 */
public class HeroDB extends View implements IHero {

    private Context context;

    private static DB snappyDB;

    public HeroDB(Context c) {
        super(c);
        this.context = c;
        initOrOpenDb();
    }

    private void initOrOpenDb() {
        try {
            if (snappyDB == null || !snappyDB.isOpen()) {
                snappyDB = DBFactory.open(context, "hero");
            }
        } catch (SnappydbException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        if (jsonObject.has("key")) {
            try {
                initOrOpenDb();
                if (jsonObject.has("value")) {
                    setValue(jsonObject.get("value"), jsonObject.getString("key"));
                } else {
                    Object value = getValue(jsonObject.getString("key"));
                    if (jsonObject.has("isNpc")) {
                        ((HeroFragmentActivity)context).getCurrentFragment().mWebview.evaluateJavascript("window['HeroSignature"+"callback']("+ value.toString() +")",null);
                    } else {
                        JSONObject o = new JSONObject();
                        o.put("result", value);
                        ((HeroFragmentActivity)context).on(o);
                    }
                }
                snappyDB.close();
            } catch (SnappydbException e) {
                e.printStackTrace();
            }
        }

        if (jsonObject.has("arrayKey")) {
            try {
                initOrOpenDb();
                if (jsonObject.has("values")) {
                    addValue(jsonObject.get("value"), jsonObject.getString("key"));
                } else if (jsonObject.has("start") && jsonObject.has("count")) {
                    Object[] value = valueForArrayKey(jsonObject.getString("key"), jsonObject.getInt("start"),
                            jsonObject.getInt("count"));
                    if (jsonObject.has("isNpc")) {
                        ((HeroFragmentActivity)context).getCurrentFragment().mWebview.evaluateJavascript("window['HeroSignature"+"callback']("+ value.toString() +")",null);
                    } else {
                        JSONObject o = new JSONObject();
                        o.put("result", value);
                        ((HeroFragmentActivity)context).on(o);
                    }
                }
                snappyDB.close();
            } catch (SnappydbException e) {
                e.printStackTrace();
            }
        }
    }


    private void setValue(Object value, String key) throws SnappydbException {
        snappyDB.put(key,value);
    }

    private Object getValue(String key) throws SnappydbException {
        return snappyDB.getObject(key,Object.class);
    }

    private void addValue(Object value, String key) throws SnappydbException {
        Object[] arraysOld = snappyDB.getObjectArray(key, Object.class);
        Object[] arraysNew = null;
        if (arraysOld != null) {
            List<Object> listOld = Arrays.asList(arraysOld);
            List<Object> listNew;
            if (value instanceof Object[]) {
                listNew = Arrays.asList(value);
                listOld.add(listNew);
            } else {
                listNew = listOld;
                listNew.add(value);
            }
            arraysNew = listNew.toArray();
        } else {
            snappyDB.put(key, value);
        }
        snappyDB.put(key, arraysNew);
    }

    private Object[] valueForArrayKey (String key, int start, int count) throws SnappydbException {
        Object[] arraysOld = snappyDB.getObjectArray(key, Object.class);
        if (start + count <= arraysOld.length) {
            List<Object> listOld = Arrays.asList(arraysOld);
            List<Object> listNew = listOld.subList(listOld.size() - start - count -1 , listOld.size() - start - 1);
            return listNew.toArray();
        } else {
            return null;
        }
    }

}
