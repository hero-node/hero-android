package com.hero;

import android.content.Context;
import android.view.View;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
                    setValue(jsonObject.getJSONObject("value"), jsonObject.getString("key"));
                } else {
                    Object value = getValue(jsonObject.getString("key"));
                    if (jsonObject.has("isNpc")) {
                        ((HeroFragmentActivity)context).getCurrentFragment().mWebview.evaluateJavascript("window['HeroDB"+"callback']("+ value.toString() +")",null);
                    } else {
                        JSONObject o = new JSONObject();
                        o.put("result", value);
                        o.put("key", jsonObject.getString("key"));
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
                if (jsonObject.has("value")) {
                    addValue(jsonObject.getJSONObject("value"), jsonObject.getString("arrayKey"));
                } else if (jsonObject.has("start") && jsonObject.has("count")) {
                    Object[] value = valueForArrayKey(jsonObject.getString("arrayKey"), jsonObject.getInt("start"),
                            jsonObject.getInt("count"));
                    if (jsonObject.has("isNpc")) {
                        ((HeroFragmentActivity)context).getCurrentFragment().mWebview.evaluateJavascript("window['HeroDB"+"callback']("+ value.toString() +")",null);
                    } else {
                        JSONObject o = new JSONObject();
                        JSONArray jsonArray = new JSONArray();
                        for (Object object : value) {
                            jsonArray.put(object);
                        }
                        o.put("result", jsonArray);
                        o.put("arrayKey", jsonObject.getString("arrayKey"));
                        ((HeroFragmentActivity)context).on(o);
                    }
                }
                snappyDB.close();
            } catch (SnappydbException e) {
                e.printStackTrace();
            }
        }
    }


    private void setValue(JSONObject value, String key) throws SnappydbException, JSONException{
        snappyDB.put(key, value.get("value"));
    }

    private Object getValue(String key) throws SnappydbException {
        if (snappyDB.findKeys(key).length == 0) {
            return snappyDB.getObject(key,Object.class);
        } else {
            return new Object();
        }
    }

    private void addValue(JSONObject value, String key) throws SnappydbException, JSONException{
        Object[] arraysOld ;
        if (snappyDB.findKeys(key).length == 0) {
            arraysOld = null;
        } else {
            arraysOld = snappyDB.getObjectArray(key, Object.class);
        }

        Object[] arraysNew = null;
        if (arraysOld != null) {
            List<Object> listOld = new ArrayList<Object>();
            for (Object o : arraysOld) {
                listOld.add(o);
            }
            List<Object> listNew;
            Object o = value;
            if (o instanceof Object[]) {
                listNew = Arrays.asList(o);
                listOld.add(o);
            } else {
                listNew = listOld;
                listNew.add(o);
            }
            arraysNew = listNew.toArray();
            snappyDB.put(key, arraysNew);
        } else {
            Object o = value;
            List<Object> list = new ArrayList<>();
            list.add(o);
            snappyDB.put(key, list.toArray());
        }
    }

    private Object[] valueForArrayKey (String key, int start, int count) throws SnappydbException {
        if (snappyDB.findKeys(key).length == 0) {
            return new Object[10];
        } else {
            Object[] arraysOld = snappyDB.getObjectArray(key, Object.class);
            if (start + count <= arraysOld.length) {
                List<Object> listOld = Arrays.asList(arraysOld);
                List<Object> listNew = listOld.subList(listOld.size() - start - count -1 , listOld.size() - start - 1);
                return listNew.toArray();
            } else {
                return arraysOld;
            }
        }
    }

}
