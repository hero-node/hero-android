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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hero.chat.ChatEmojiAdapter;
import com.hero.chat.ChatEmojiUtils;
import com.hero.chat.ChatMessageAdapter;
import com.hero.chat.ChatMsgEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xincai on 16-6-15.
 */
public class HeroChatView extends FrameLayout implements View.OnClickListener, HeroFragment.ContextMenuItemListener, IHero {

    public static final int MENU_ID_COPY = 1;
    private Context context;
    private ListView chatListView;
    private EditText chatEditText;
    private View buttonSend;
    private LinearLayout layoutEmojiPager;
    private View layoutTools;
    private View layoutMore;

    private ViewPager emojiViewPager;
    private InputMethodManager inputMethodManager;
    private ClipboardManager clipBoard;
    private List<String> emojiResList;
    private ChatMessageAdapter adapter;

    private TextView txtTitle;
    private ImageView imgTitleRight;
    private ImageView imgEmojiNormal, imgEmojiChecked;
    private View editLayout;
    private ViewGroup optionButtonsLayout;
    private ProgressBar progressBar;
    private CheckBox checkButtonToggle;
    private Button btnMore;

    public HeroChatView(Context context) {
        super(context);
        initView(context, null);
    }

    public HeroChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, null);
    }

    public HeroChatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeroChatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, null);
    }

    public HeroChatView(Context context, List data) {
        super(context);
        initView(context, data);
    }

    private void initView(Context context, List data) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.chat_view, this, true);
        initSubViews(context);
        initChatList(context, data);
        initEmojiContainer();
        registerContextMenu();
    }

    protected void initSubViews(Context context) {
        txtTitle = (TextView) findViewById(R.id.txtChatTitle);
        imgTitleRight = (ImageView) findViewById(R.id.imgTitleRight);
        chatListView = (ListView) findViewById(R.id.chat_list);
        chatEditText = (EditText) findViewById(R.id.chatEditText);
        editLayout = findViewById(R.id.editTextLayout);
        optionButtonsLayout = (ViewGroup) findViewById(R.id.optionButtonsLayout);
        checkButtonToggle = (CheckBox) findViewById(R.id.btnToggleText);
        buttonSend = findViewById(R.id.btnSend);
        emojiViewPager = (ViewPager) findViewById(R.id.emojiPager);
        layoutEmojiPager = (LinearLayout) findViewById(R.id.layoutEmoji);
        imgEmojiNormal = (ImageView) findViewById(R.id.imgEmojiNormal);
        imgEmojiChecked = (ImageView) findViewById(R.id.imgEmojiChecked);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        btnMore = (Button) findViewById(R.id.btnMore);
        imgEmojiNormal.setVisibility(View.VISIBLE);
        imgEmojiChecked.setVisibility(View.INVISIBLE);
        layoutTools = findViewById(R.id.layoutTools);
        layoutMore = findViewById(R.id.layoutMore);
        editLayout.setBackgroundResource(R.drawable.chat_input_bg_normal);
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        clipBoard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        findViewById(R.id.chat_album).setOnClickListener(this);
        findViewById(R.id.chat_location).setOnClickListener(this);
        findViewById(R.id.chat_video).setOnClickListener(this);
        findViewById(R.id.chat_location).setOnClickListener(this);
        findViewById(R.id.imgTitleBack).setOnClickListener(this);

        imgTitleRight.setOnClickListener(this);
        imgEmojiNormal.setOnClickListener(this);
        imgEmojiChecked.setOnClickListener(this);

        buttonSend.setOnClickListener(this);
        chatEditText.setOnClickListener(this);
        btnMore.setOnClickListener(this);
        checkButtonToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hideKeyboard();
                    resetToolsContainer();
                    toggleInputContainer(false);
                } else {
                    toggleInputContainer(true);
                }
            }
        });

        imgTitleRight.setImageResource(R.drawable.chat_avatar_default);

        editLayout.requestFocus();
        chatEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editLayout.setBackgroundResource(R.drawable.chat_input_bg_focused);
                } else {
                    editLayout.setBackgroundResource(R.drawable.chat_input_bg_normal);
                }

            }
        });

        chatEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    btnMore.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                } else {
                    btnMore.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    public void initChatList(Context context, List data) {
        adapter = new ChatMessageAdapter(context, data);
        chatListView.setAdapter(adapter);
        int count = chatListView.getCount();
        if (count > 0) {
            rollListToBottom();
        }

        chatListView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                resetToolsContainer();
                return false;
            }
        });

        chatListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int itemType = adapter.getItemViewType(position);
                if (itemType != ChatMsgEntity.MESSAGE_TYPE_RECEIVED_TEXT && itemType != ChatMsgEntity.MESSAGE_TYPE_SENT_TEXT) {
                    return true;
                }
                return false;
            }
        });
    }

    private void initEmojiContainer() {
        if (emojiResList == null) {
            emojiResList = ChatEmojiUtils.getEmojiList(ChatEmojiUtils.EMOJI_COUNT);
        }
        List<View> pages = new ArrayList<View>();

        int pageCount = ChatEmojiUtils.EMOJI_COUNT / ChatEmojiUtils.EMOJI_COUNT_PER_PAGE;
        if (pageCount * ChatEmojiUtils.EMOJI_COUNT_PER_PAGE < ChatEmojiUtils.EMOJI_COUNT) {
            pageCount++;
        }
        for (int i = 0; i < pageCount; i++) {
            View page = setupEmojiGridChildView(i, pageCount);
            pages.add(page);
        }
        emojiViewPager.setAdapter(new ExpressionPagerAdapter(pages));
    }

    private void registerContextMenu() {
        int[] menuId = new int[] {MENU_ID_COPY};
        String[] menuTitle = new String[] {context.getString(R.string.chat_menu_copy)};
        HeroFragment fragment = ((HeroFragmentActivity) getContext()).getParentFragment(this);
        if (fragment != null) {
            fragment.registerContextMenuHandler(chatListView, menuId, menuTitle, this);
        }
    }

    private View setupEmojiGridChildView(int i, int pageCount) {
        View view = inflate(context, R.layout.chat_emoji_gridview, null);
        GridView gridView = (GridView) view.findViewById(R.id.gridView);
        List<String> list = new ArrayList<String>();
        if (i == pageCount - 1) {
            list.addAll(emojiResList.subList(i * ChatEmojiUtils.EMOJI_COUNT_PER_PAGE, emojiResList.size() - 1));
        } else {
            list.addAll(emojiResList.subList(i * ChatEmojiUtils.EMOJI_COUNT_PER_PAGE, ((i + 1) * ChatEmojiUtils.EMOJI_COUNT_PER_PAGE)));
        }
        list.add(ChatEmojiUtils.DELETE_ICON_NAME);
        final ChatEmojiAdapter expressionAdapter = new ChatEmojiAdapter(context, R.layout.chat_emoji_item, list);
        gridView.setAdapter(expressionAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filename = expressionAdapter.getItem(position);

                if (!filename.equals(ChatEmojiUtils.DELETE_ICON_NAME)) {
                    String emojiExpression = ChatEmojiUtils.getEmojiExpression(filename);
                    if (emojiExpression != null) {
                        int cursorPosition = chatEditText.getSelectionStart();
                        if (cursorPosition > 0) {
                            chatEditText.getText().insert(cursorPosition, emojiExpression);
                        } else {
                            chatEditText.append(emojiExpression);
                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(chatEditText.getText())) {
                        int selectionStart = chatEditText.getSelectionStart();
                        if (selectionStart > 0) {
                            String text = chatEditText.getText().toString();
                            String tempStr = text.substring(0, selectionStart);
                            int i = tempStr.lastIndexOf("[");
                            if (i != -1) {
                                String expression = tempStr.substring(i, selectionStart);
                                if (ChatEmojiUtils.isEmojiExists(expression)) {
                                    chatEditText.getEditableText().delete(i, selectionStart);
                                } else {
                                    chatEditText.getEditableText().delete(selectionStart - 1, selectionStart);
                                }
                            } else {
                                chatEditText.getEditableText().delete(selectionStart - 1, selectionStart);
                            }
                        }
                    }
                }
            }

        });
        return view;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id != R.id.chatEditText) {
            hideKeyboard();
        }
        if (id == R.id.imgEmojiNormal) {
            layoutTools.setVisibility(View.VISIBLE);
            toggleEmojiContainer(true);
            resetMoreContainer();
            rollListToBottom();
        } else if (id == R.id.imgEmojiChecked) {
            resetToolsContainer();
        } else if (id == R.id.btnSend) {
            String s = chatEditText.getText().toString();
            sendTextMessage(s);
        } else if (id == R.id.chatEditText) {
            rollListToBottom();
            editLayout.setBackgroundResource(R.drawable.chat_input_bg_focused);
            resetToolsContainer();
        } else if (id == R.id.btnToggleText) {
        } else if (id == R.id.btnMore) {
            toggleEmojiContainer(false);
            if (layoutMore.getVisibility() == VISIBLE) {
                resetToolsContainer();
            } else {
                toggleInputContainer(true);
                layoutTools.setVisibility(VISIBLE);
                layoutMore.setVisibility(VISIBLE);
                rollListToBottom();
            }
        } else if (id == R.id.chat_album){
            // 激活系统图库，选择一张图片
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            // 开启一个带有返回值的Activity
            ((HeroActivity)context).startActivityForResult(intent, HeroActivity.LOCAL_CROP);
        } else if (id == R.id.chat_location) {



        } else if (id == R.id.chat_video) {
            try {
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("command", "gotoWithLoading:http://localhost:3000/hero-home/videoChat.html");
//                HeroView.sendActionToContext(getContext(), jsonObject);

                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("click", "clickVideo");
                ((HeroFragmentActivity)context).on(jsonObject2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.chat_trade) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("click", "clickTX");
                ((HeroFragmentActivity)context).on(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (layoutTools.getVisibility() == View.VISIBLE) {
                resetToolsContainer();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void hideKeyboard() {
        if (((Activity) context).getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (((Activity) context).getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void rollListToBottom() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                chatListView.setSelection(chatListView.getBottom());
            }
        }, 10);
    }

    private void toggleEmojiContainer(boolean isShown) {
        if (isShown) {
            imgEmojiNormal.setVisibility(View.INVISIBLE);
            imgEmojiChecked.setVisibility(View.VISIBLE);
            layoutEmojiPager.setVisibility(View.VISIBLE);
            emojiViewPager.setCurrentItem(0, true);
        } else {
            imgEmojiNormal.setVisibility(View.VISIBLE);
            imgEmojiChecked.setVisibility(View.INVISIBLE);
            layoutEmojiPager.setVisibility(View.GONE);
        }
    }

    private void resetMoreContainer() {
        layoutMore.setVisibility(View.GONE);
    }

    private void toggleInputContainer(boolean isInput) {
        if (isInput) {
            checkButtonToggle.setChecked(false);
            optionButtonsLayout.setVisibility(GONE);
            editLayout.setVisibility(VISIBLE);
        } else {
            checkButtonToggle.setChecked(true);
            optionButtonsLayout.setVisibility(VISIBLE);
            editLayout.setVisibility(GONE);
        }
    }

    // reset emoji and more area
    private void resetToolsContainer() {
        layoutTools.setVisibility(View.GONE);
        toggleEmojiContainer(false);
        resetMoreContainer();
    }

    private void setupOptionsContainer(JSONObject content) {
        if (optionButtonsLayout != null && content != null) {
            // add views to options layout
            optionButtonsLayout.removeAllViews();
            // set the layout width for the children to measure themselves
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) optionButtonsLayout.getLayoutParams();
            params.width = editLayout.getWidth() + checkButtonToggle.getWidth() + ((LinearLayout.LayoutParams) checkButtonToggle.getLayoutParams()).leftMargin + ((LinearLayout.LayoutParams) editLayout.getLayoutParams()).leftMargin;
            optionButtonsLayout.setLayoutParams(params);
            FrameLayout.LayoutParams newParam = new FrameLayout.LayoutParams(0, 0);
            newParam.leftMargin = 0;
            newParam.rightMargin = 0;
            HeroChatMsgView.createUiView(getContext(), content, optionButtonsLayout, newParam);
        }
    }

    private void sendTextMessage(String content) {
        if (content.length() > 0) {
            try {
                JSONObject msg = ChatMsgEntity.composeOutgoingMessage(content);
                HeroView.sendActionToContext(getContext(), msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            chatEditText.setText("");
        }
    }

    public void copyToClipBoard(String text) {
        if (clipBoard != null) {
            clipBoard.setPrimaryClip(ClipData.newPlainText(null, text));
        }
    }

    @Override
    public void onMenuSelected(MenuItem item) {
        if (item.getItemId() == MENU_ID_COPY) {
            ContextMenu.ContextMenuInfo info = (ContextMenu.ContextMenuInfo) item.getMenuInfo();
            if (info != null) {
                if (info instanceof AdapterView.AdapterContextMenuInfo) {
                    AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) info;
                    if (menuInfo.targetView instanceof HeroChatMsgView) {
                        View v = menuInfo.targetView.findViewById(R.id.txtChatContent);
                        if (v != null && v instanceof TextView) {
                            copyToClipBoard(((TextView) v).getText().toString());
                            HeroToast.show(getContext(), getContext().getString(R.string.chat_copied), -1, null);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void on(final JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        if (jsonObject.has("customInputView")) {
            checkButtonToggle.setVisibility(VISIBLE);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setupOptionsContainer(jsonObject.optJSONObject("customInputView"));
                }
            }, 50);
        }

        if (jsonObject.has("newMsg")) {
            JSONObject message = jsonObject.optJSONObject("newMsg");
            if (message != null) {
                adapter.addItem(new ChatMsgEntity(message));
            }
        }

        if (jsonObject.has("data")) {
            Object data = jsonObject.opt("data");
            if (data instanceof JSONArray) {
                addHistoryData((JSONArray) data);
            }
        }
    }

    private void addHistoryData(JSONArray data) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject msg = data.optJSONObject(i);
            if (msg != null) {
                adapter.addItem(new ChatMsgEntity(msg));
            }
        }
    }

    public class ExpressionPagerAdapter extends PagerAdapter {

        private List<View> views;

        public ExpressionPagerAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

}
