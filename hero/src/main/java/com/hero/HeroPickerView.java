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

 * Neither the name Hero nor the names of its contributors may be used to
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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HeroPickerView extends View implements IHero, View.OnClickListener, NumberPicker.OnValueChangeListener {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_DATE = 1;
    private String subDateMode;
    private String[] sectionData;
    private Map<String, JSONArray> rowDataMap;
    private List<NumberPicker> pickerList;
    private Dialog pickerDialog;

    // date and time
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    private int currSection;
    private int currRow;

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int theYear, int monthOfYear, int dayOfMonth) {
            year = theYear;
            month = monthOfYear;
            day = dayOfMonth;
            //            setText(formatDate(year, month, day));
            Object click = getTag(R.id.kAction);
            if (click != null) {
                try {
                    JSONObject action = (JSONObject) click;
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    c.set(Calendar.MONTH, monthOfYear);
                    c.set(Calendar.YEAR, theYear);
                    HeroView.putValueToJson(action, c.getTimeInMillis() / 1000);
                    ((IHeroContext) getContext()).on(action);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int h, int m) {
            hour = h;
            minute = m;
            Object click = getTag(R.id.kAction);
            if (click != null) {
                try {
                    JSONObject action = (JSONObject) click;
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, hour);
                    c.set(Calendar.MINUTE, minute);
                    HeroView.putValueToJson(action, c.getTimeInMillis() / 1000);
                    ((IHeroContext) getContext()).on(action);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (pickerList != null && pickerList.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < pickerList.size(); j++) {
                    int selectId = pickerList.get(j).getValue();
                    if (pickerList.get(j).getDisplayedValues() != null) {
                        sb.append(pickerList.get(j).getDisplayedValues()[selectId]);
                        if (j < pickerList.size() - 1) {
                            sb.append(" ");
                        }
                    }
                }
                if (pickerDialog != null && pickerDialog.isShowing()) {
                    pickerDialog.dismiss();
                }
                //                HeroPickerView.this.setText(sb.toString());
                Object click = getTag(R.id.kAction);
                if (click != null && pickerList != null) {
                    try {
                        JSONObject action = (JSONObject) click;
                        JSONObject value = new JSONObject();
                        if (pickerList.size() > 1) {
                            value.put("section", pickerList.get(0).getValue());
                            value.put("row", pickerList.get(1).getValue());
                        } else {
                            value.put("row", pickerList.get(0).getValue());
                        }
                        HeroView.putValueToJson(action, value);
                        ((IHeroContext) getContext()).on(action);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public HeroPickerView(Context context) {
        super(context);
        this.setVisibility(GONE);
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);

        int type = TYPE_NORMAL;
        currSection = 0;
        currRow = 0;

        if (!jsonObject.has("backgroundColor")) {
            this.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }

        if (jsonObject.has("type")) {
            if ("date".equals(jsonObject.getString("type"))) {
                if (jsonObject.has("dateMode")) {
                    subDateMode = jsonObject.getString("dateMode");
                }
                type = TYPE_DATE;
            }
        }

        if (type == TYPE_DATE) {
            if (jsonObject.has("date")) {
                //                this.setText(jsonObject.getString("date"));
            } else {
                //                this.setText(formatDate(year, month, day));
            }
            createDialog(getContext(), jsonObject, type);
        }

        if (jsonObject.has("selectAction")) {
            this.setTag(R.id.kAction, jsonObject.get("selectAction"));
        }

        if (jsonObject.has("selectIndex")) {
            Object obj = jsonObject.get("selectIndex");
            if (obj instanceof JSONObject) {
                JSONObject selectObj = (JSONObject) jsonObject.get("selectIndex");
                if (selectObj.has("section")) {
                    currSection = selectObj.getInt("section");
                }
                if (selectObj.has("row")) {
                    currRow = selectObj.getInt("row");
                }
            }
        }

        if (jsonObject.has("datas") && type != TYPE_DATE) {
            Object dataObj = jsonObject.get("datas");
            boolean isDataValid = false;

            if (dataObj instanceof JSONArray) {
                JSONArray dataArray = (JSONArray) dataObj;
                if (dataArray.length() > 0 && dataArray.get(0) instanceof JSONObject) {
                    // it's a json array
                    if (dataArray.length() > 0) {
                        isDataValid = true;
                        String column = null, row = null;
                        createMapIfNeeded();
                        sectionData = new String[dataArray.length()];
                        int counter = 0;
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject item = dataArray.getJSONObject(i);
                            JSONArray childArray = item.getJSONArray("rows");
                            sectionData[i] = item.getString("title");
                            rowDataMap.put(sectionData[i], childArray);
                            if (counter == currSection) {
                                column = sectionData[i];
                                if (currRow < childArray.length()) {
                                    row = childArray.getString(currRow);
                                }
                            }
                            counter++;
                        }
                        // set the displayed text
                        if (column != null && row != null) {
                            //                            this.setText(column + " " + row);
                        }
                    }
                } else {
                    sectionData = jsonToArray(jsonObject.getJSONArray("datas"));
                    if (sectionData.length > 0) {
                        isDataValid = true;
                    }
                    if (sectionData != null && currSection < sectionData.length) {
                        //                        this.setText(sectionData[currSection]);
                    }
                }
            } else if (dataObj instanceof JSONObject) {
                JSONObject obj = (JSONObject) dataObj;
                if (obj.length() > 0) {
                    isDataValid = true;
                    String column = null, row = null;
                    Iterator it = obj.keys();
                    createMapIfNeeded();
                    sectionData = new String[obj.length()];
                    int counter = 0;
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        JSONArray array = obj.getJSONArray(key);
                        sectionData[counter] = key;
                        rowDataMap.put(key, array);
                        if (counter == currSection) {
                            column = key;
                            if (currRow < array.length()) {
                                row = array.getString(currRow);
                            }
                        }
                        counter++;
                    }
                    // set the displayed text
                    if (column != null && row != null) {
                        //                        this.setText(column + " " + row);
                    }
                }
            }
            if (isDataValid) {
                pickerDialog=null;
                createDialog(getContext(), jsonObject, type);
            }
        }

        if (jsonObject.has("method")) {
            if (jsonObject.getString("method").equals("show")) {
                showPickDialog(true);
            } else if (jsonObject.getString("method").equals("hide")) {
                showPickDialog(false);
            }
        }

        //        setOnClickListener(this);
    }

    private void createMapIfNeeded() {
        if (rowDataMap == null) {
            rowDataMap = new HashMap<String, JSONArray>();
        }
    }

    @Override
    public void onClick(View view) {
        showPickDialog(true);
    }

    private void showPickDialog(boolean show) {
        if (pickerDialog != null) {
            if (show) {
                pickerDialog.show();
            } else {
                pickerDialog.dismiss();
            }
        }
    }

    private void createDialog(Context context, JSONObject json, int pickerType) {
        if (pickerDialog != null) return;
        if (pickerType == TYPE_DATE) {
            if ("UIDatePickerModeTime".equals(subDateMode)) {
                final Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
                pickerDialog = new TimePickerDialog(context, timeSetListener, hour, minute, true);
            } else if ("UIDatePickerModeDateAndTime".equals(subDateMode)) {
                // ??
            } else {
                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
                pickerDialog = new DatePickerDialog(context, dateSetListener, year, month, day);
            }
        } else {
            ViewGroup dialogView = createDialogView(context);
            ViewGroup pickLayout = (ViewGroup) dialogView.findViewById(R.id.pickerLayout);
            pickerList = new ArrayList<NumberPicker>();

            if (rowDataMap == null) { // only section
                addPicker(context, pickLayout, sectionData, currSection, false);
            } else { // section and row
                if (rowDataMap.size() > 0) {
                    if (currSection >= sectionData.length) currSection = 0;
                    // add section picker
                    addPicker(context, pickLayout, sectionData, currSection, true);

                    // add row picker
                    JSONArray array = rowDataMap.get(sectionData[currSection]);
                    if (currRow >= array.length()) currRow = 0;
                    addPicker(context, pickLayout, jsonToArray(array), currRow, false);
                }
            }
            pickerDialog = new Dialog(context, R.style.PickerDialog);
            pickerDialog.setContentView(dialogView);
            Button button = (Button) dialogView.findViewById(R.id.btnConfirm);
            button.setOnClickListener(listener);
            //            pickerDialog.setPositiveButton(android.R.string.ok, listener).create();
            //            WindowManager.LayoutParams localLayoutParams = pickerDialog.getWindow().getAttributes();
            //            localLayoutParams.gravity = Gravity.BOTTOM;
            //            pickerDialog.onWindowAttributesChanged(localLayoutParams);

        }
    }

    private ViewGroup createDialogView(Context context) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.picker_dialog, null);
        return (ViewGroup) dialogView;
    }

    private void addPicker(Context context, ViewGroup rootView, String[] data, int defaultValue, boolean setListener) {
        NumberPicker picker = new NumberPicker(context);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = 30;
        params.rightMargin = 30;
        rootView.addView(picker, params);
        pickerList.add(picker);
        if (data != null) {
            picker.setMaxValue(data.length - 1);
            picker.setDisplayedValues(data);
        }
        picker.setValue(defaultValue);
        if (setListener) {
            picker.setOnValueChangedListener(this);
        }
    }

    private void setDividerColor(NumberPicker picker, int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public String[] jsonToArray(JSONArray array) {
        String strings[] = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            try {
                strings[i] = array.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return strings;
    }


    public String formatDate(int y, int m, int d) {
        m++;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        return y + "-" + m + "-" + d;
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
        if (rowDataMap != null && pickerList != null && pickerList.size() > 1) {
            NumberPicker nextPicker = null;
            for (int i = 0; i < pickerList.size() - 1; i++) {
                NumberPicker np = pickerList.get(i);
                if (numberPicker.equals(np)) {
                    nextPicker = pickerList.get(i + 1);
                    break;
                }
            }
            String key = numberPicker.getDisplayedValues()[newValue];
            if (newValue < rowDataMap.size()) {
                if (nextPicker != null) {
                    nextPicker.setDisplayedValues(null);
                    nextPicker.setMaxValue(rowDataMap.get(key).length() - 1);
                    nextPicker.setDisplayedValues(jsonToArray(rowDataMap.get(key)));
                    nextPicker.setValue(0);
                }
            }
        }
    }
}
