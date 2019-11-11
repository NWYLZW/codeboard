package com.gazlaws.codeboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.media.MediaPlayer; // for keypress sound

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;


import static android.view.KeyEvent.KEYCODE_CTRL_LEFT;
import static android.view.KeyEvent.KEYCODE_SHIFT_LEFT;
import static android.view.KeyEvent.META_CTRL_ON;
import static android.view.KeyEvent.META_SHIFT_ON;

/*Created by Ruby(aka gazlaws) on 13/02/2016.
 */

public class CodeBoardIME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {
    private KeyboardView kv;
    private Keyboard keyboard;

    EditorInfo sEditorInfo;
    private boolean vibratorOn;
    private boolean soundOn;
    private boolean shiftLock = false;
    private boolean shift = false;
    private boolean ctrl = false;
    private int mKeyboardState = R.integer.keyboard_normal;
    private int mLayout, mSize;
    private Timer timerLongPress = null;
    private boolean switchedKeyboard=false;

    int pageNum = 0;
    ArrayList customArr = new ArrayList<Integer>();
    Keyboard.Key pageNumKey = null;
    private String filePath = "/data/data/com.gazlaws.codeboard/files/customStrMenber";
    private ArrayList customStrMenber;
    final private int D_value = 68;

    @Override
    public View onCreateInputView() {
        SharedPreferences pre = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);

        int color;
        KeyboardView myTemp;
        String BackgroundColor[] = {"263238","eceff1","000000","ffffff","0d47a1","4a148c"};
        int RADIO_INDEX_COLOUR = pre.getInt("RADIO_INDEX_COLOUR", 0);
        switch (RADIO_INDEX_COLOUR) {
            case 0: case 1: case 2: case 3: case 4: case 5:
                color = Color.parseColor("#"+BackgroundColor[RADIO_INDEX_COLOUR]);
                break;
            case 6:
                color = pre.getInt("MyCustomColor",0);
                break;
            default:
                color = Color.parseColor("#"+BackgroundColor[RADIO_INDEX_COLOUR]);
                break;
        }
        if(Color.red(color)<128&&Color.blue(color)<128&&Color.green(color)<128)
            myTemp = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_dark, null);
        else
            myTemp = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        myTemp.setBackgroundColor(color);
        kv = myTemp;

        ctrl = false;
        shift = false;
        if (pre.getInt("PREVIEW", 0) == 1) {
            kv.setPreviewEnabled(true);
        } else kv.setPreviewEnabled(false);

        if (pre.getInt("SOUND", 1) == 1) {
            soundOn = true;
        } else soundOn = false;

        if (pre.getInt("VIBRATE", 1) == 1) {
            vibratorOn = true;
        } else vibratorOn = false;

        mLayout = pre.getInt("RADIO_INDEX_LAYOUT", 0);
        mSize = pre.getInt("SIZE", 2);
        mKeyboardState = R.integer.keyboard_normal;
        //reset to normal

        Keyboard keyboard = chooseKB(mLayout, mSize, mKeyboardState);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

        return kv;
    }
    private void onSYM(){
        if (kv != null) {
            if (mKeyboardState == R.integer.keyboard_normal) {
                //change to symbol keyboard
                Keyboard symbolKeyboard = chooseKB(mLayout, mSize, R.integer.keyboard_sym);

                kv.setKeyboard(symbolKeyboard);

                mKeyboardState = R.integer.keyboard_sym;
                readMenber();
            } else if (mKeyboardState == R.integer.keyboard_sym) {
                //change to normal keyboard
                Keyboard normalKeyboard = chooseKB(mLayout, mSize, R.integer.keyboard_normal);

                kv.setKeyboard(normalKeyboard);
                mKeyboardState = R.integer.keyboard_normal;
            }
            controlKeyUpdateView();
            shiftKeyUpdateView();
        }
    }
    private void trunPage(int code){
        int nextPage = pageNum+(code-30000);
        int maxPage = customStrMenber.size()/5+1;
        if(nextPage>=0 && nextPage<maxPage){
            pageNum = nextPage;
            updataCustomStr();
        }
    }
    @Override
    public void onKey(int primaryCode, int[] KeyCodes) {
        InputConnection ic = getCurrentInputConnection();
        keyboard = kv.getKeyboard();

        switch (primaryCode) {
            case 30001:
            case 29999:
                trunPage(primaryCode);
                break;

            case Keyboard.KEYCODE_DELETE:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                break;
            case Keyboard.KEYCODE_DONE:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
                break;

            case 27:
                //Escape
                long now = System.currentTimeMillis();
                ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE, 0, KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON));

                break;

            case -13:
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.showInputMethodPicker();
                break;
            case -15:
                onSYM();
                break;

            case 17:
//              ctrl key
                long nowCtrl = System.currentTimeMillis();
                if (ctrl)
                    ic.sendKeyEvent(new KeyEvent(nowCtrl, nowCtrl, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT, 0, META_CTRL_ON));
                else
                    ic.sendKeyEvent(new KeyEvent(nowCtrl, nowCtrl, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CTRL_LEFT, 0, META_CTRL_ON));

                ctrl = !ctrl;
                controlKeyUpdateView();
                break;

            case 16:
                // Log.e("CodeBoardIME", "onKey" + Boolean.toString(shiftLock));
                //Shift - runs after long press, so shiftlock may have just been activated
                long nowShift = System.currentTimeMillis();
                if (shift)
                    ic.sendKeyEvent(new KeyEvent(nowShift, nowShift, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT, 0, META_SHIFT_ON));
                else
                    ic.sendKeyEvent(new KeyEvent(nowShift, nowShift, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, META_SHIFT_ON));
                if (shiftLock) shift = true;
                else shift = !shift;
                shiftKeyUpdateView();
                break;

            case 9:
                //tab
                // ic.commitText("\u0009", 1);
                sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB);
                break;

            case 5000:
                handleArrow(KeyEvent.KEYCODE_DPAD_LEFT);
                break;
            case 5001:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
                break;
            case 5002:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
                break;
            case 5003:
                handleArrow(KeyEvent.KEYCODE_DPAD_RIGHT);
                break;

            default:
                char code = (char) primaryCode;
                if (ctrl) {
                    onKeyCtrl(code, ic);
                    if (!shiftLock) {
                        long nowS = System.currentTimeMillis();
                        shift = false;
                        ic.sendKeyEvent(new KeyEvent(nowS, nowS, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT, 0, META_SHIFT_ON));

                        shiftKeyUpdateView();
                    }
                    ctrl = false;
                    controlKeyUpdateView();
                } else if (Character.isLetter(code) && shift) {
                    code = Character.toUpperCase(code);
                    ic.commitText(String.valueOf(code), 1);
                    if (!shiftLock) {

                        long nowS = System.currentTimeMillis();
                        shift = false;
                        ic.sendKeyEvent(new KeyEvent(nowS, nowS, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT, 0, META_SHIFT_ON));

                        //Log.e("CodeboardIME", "Unshifted b/c no lock");
                    }

                    shiftKeyUpdateView();
                } else{
                    if(!switchedKeyboard) {
                        ic.commitText(String.valueOf(code), 1);
                    }
                    switchedKeyboard=false;
                }
        }

    }
    @Override
    public void onPress(final int primaryCode) {
        if (soundOn) {
            MediaPlayer keypressSoundPlayer = MediaPlayer.create(this, R.raw.keypress_sound);
            keypressSoundPlayer.start();
            keypressSoundPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.release();

                }
            });
        }
        if (vibratorOn) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if(vibrator!=null)
                vibrator.vibrate(20);
        }
        if (timerLongPress != null)
            timerLongPress.cancel();
        timerLongPress = new Timer();
        timerLongPress.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                CodeBoardIME.this.onKeyLongPress(primaryCode);
                            } catch (Exception e) {
                                Log.e(CodeBoardIME.class.getSimpleName(), "uiHandler.run: " + e.getMessage(), e);
                            }

                        }
                    };
                    uiHandler.post(runnable);
                } catch (Exception e) {
                    Log.e(CodeBoardIME.class.getSimpleName(), "Timer.run: " + e.getMessage(), e);
                }
            }

        }, ViewConfiguration.getLongPressTimeout());

    }
    public void onKeyLongPress(int keyCode) {
        // LongPress ESC Exit keyBoard
        if(keyCode == 27){
            kv.closing();
        }
        if (keyCode == 16) {
            shiftLock = !shiftLock;
        }
        if (keyCode == 32) {
            switchedKeyboard=true;
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm!=null)
                imm.showInputMethodPicker();
        }
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator!=null)
            vibrator.vibrate(50);
    }
    @Override
    public void onRelease(int primaryCode) {
        if (timerLongPress != null)
            timerLongPress.cancel();
    }

    private void gotoEnd(String str0){
        int end = str0.indexOf("$END$");
        if(end!=-1){
            for(int i = 0;i < str0.length()-end-5;i++)
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
        }else return;
    }
    @Override
    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if(text.toString().substring(0,2).equals("00")){
            int count = Integer.parseInt(text.toString().substring(2))+pageNum*5;
            if(count>=customStrMenber.size())
                text = "";
            else
                text = ((CustomStr) customStrMenber.get(count)).customStr;
        }
        CharSequence temp = text.toString().replace("$END$","");
        ic.commitText(temp, 1);
        gotoEnd(text.toString());
    }

    private boolean Judge_default_Ctrl(int code){
        char arr[] = {'a','A','c','C','v','V','x','X','z','Z'};
        for(int i = 0;i < 10;i++){
            if(code==arr[i])
                return false;}
        return true;
    }
    public void onKeyCtrl(int code, InputConnection ic) {
        long now2 = System.currentTimeMillis();

        //原来写在这的代码简直是一坨屎[ 手动微笑:) ]
        if(code>'a'&&code<'z'||code>'A'&&code<'Z'){
            if(Judge_default_Ctrl(code)){
                System.out.println(123);
                ic.sendKeyEvent(new KeyEvent(
                        now2 + 1, now2 + 1,
                        KeyEvent.ACTION_DOWN,
                        code-D_value, 0, META_CTRL_ON));
            } else {}}
        else
            switch (code) {
                case 'a': case 'A':{
                    if (sEditorInfo.imeOptions == 1342177286)//fix for DroidEdit
                    {
                        getCurrentInputConnection().performContextMenuAction(android.R.id.selectAll);
                    } else
                        ic.sendKeyEvent(new KeyEvent(now2 + 1, now2 + 1, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A, 0, META_CTRL_ON));
                    break;}
                case 'c': case 'C':{
                    if (sEditorInfo.imeOptions == 1342177286)//fix for DroidEdit
                    {
                        getCurrentInputConnection().performContextMenuAction(android.R.id.copy);
                    } else
                        ic.sendKeyEvent(new KeyEvent(now2 + 1, now2 + 1, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_C, 0, META_CTRL_ON));
                    break;}
                case 'v': case 'V':{
                    if (sEditorInfo.imeOptions == 1342177286)//fix for DroidEdit
                    {
                        getCurrentInputConnection().performContextMenuAction(android.R.id.paste);
                    } else
                        ic.sendKeyEvent(new KeyEvent(now2 + 1, now2 + 1, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_V, 0, META_CTRL_ON));
                    break;}
                case 'x': case 'X':{
                    if (sEditorInfo.imeOptions == 1342177286)//fix for DroidEdit
                    {
                        getCurrentInputConnection().performContextMenuAction(android.R.id.cut);
                    } else
                        ic.sendKeyEvent(new KeyEvent(now2 + 1, now2 + 1, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_X, 0, META_CTRL_ON));
                    break;}
                case 'z': case 'Z':{
                    if (shift) {
                        if (ic != null) {
                            if (sEditorInfo.imeOptions == 1342177286)//fix for DroidEdit
                            {
                                getCurrentInputConnection().performContextMenuAction(android.R.id.redo);
                            } else
                                ic.sendKeyEvent(new KeyEvent(now2 + 1, now2 + 1, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Z, 0, META_CTRL_ON | META_SHIFT_ON));

                            long nowS = System.currentTimeMillis();
                            shift = false;
                            ic.sendKeyEvent(new KeyEvent(nowS, nowS, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT, 0, META_SHIFT_ON));

                            shiftLock = false;
                            shiftKeyUpdateView();
                        }
                    } else {
                        //Log.e("ctrl", "z");
                        if (sEditorInfo.imeOptions == 1342177286)//fix for DroidEdit
                        {
                            getCurrentInputConnection().performContextMenuAction(android.R.id.undo);
                        } else
                            ic.sendKeyEvent(new KeyEvent(now2 + 1, now2 + 1, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Z, 0, META_CTRL_ON));

                    }

                    break;}

                default:
                    if (Character.isLetter(code) && shift) {
                        code = Character.toUpperCase(code);
                        ic.commitText(String.valueOf(code), 1);
                        if (!shiftLock) {
                            long nowS = System.currentTimeMillis();
                            shift = false;
                            ic.sendKeyEvent(new KeyEvent(nowS, nowS, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT, 0, META_SHIFT_ON));

                            //Log.e("CodeboardIME", "Unshifted b/c no lock");
                        }
                        shiftKeyUpdateView();
                    }
                    break;
            }
    }
    public Keyboard chooseKB(int layout, int size, int mode) {
        Keyboard keyboard;
        int[] qwearr_r = new int[]{R.xml.qwerty0r, R.xml.qwerty1r, R.xml.qwerty2r, R.xml.qwerty3r};
        int[] azearr_r = new int[]{R.xml.azerty0r, R.xml.azerty1r, R.xml.azerty2r, R.xml.azerty3r};
        if (layout == 0) {
            keyboard = new Keyboard(this,qwearr_r[size],mode);
        } else {
            keyboard = new Keyboard(this,azearr_r[size],mode);
        }
        keyboard.getHeight();
        return keyboard;
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        setInputView(onCreateInputView());
        sEditorInfo = attribute;
    }

    public void controlKeyUpdateView() {
        keyboard = kv.getKeyboard();
        int i;
        List<Keyboard.Key> keys = keyboard.getKeys();
        for (i = 0; i < keys.size(); i++) {
            if (ctrl) {
                if (keys.get(i).label != null && keys.get(i).label.equals("Ctrl")) {
                    keys.get(i).label = "CTRL";
                    break;
                }
            } else {
                if (keys.get(i).label != null && keys.get(i).label.equals("CTRL")) {
                    keys.get(i).label = "Ctrl";
                    break;
                }
            }
        }
        kv.invalidateKey(i);
    }
    public void shiftKeyUpdateView() {

        keyboard = kv.getKeyboard();
        List<Keyboard.Key> keys = keyboard.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            if (shift) {
                if (keys.get(i).label != null && keys.get(i).label.equals("Shft")) {
                    keys.get(i).label = "SHFT";
                    break;
                }
            } else {
                if (keys.get(i).label != null && keys.get(i).label.equals("SHFT")) {
                    keys.get(i).label = "Shft";
                    break;
                }
            }
        }
        keyboard.setShifted(shift);
        kv.invalidateAllKeys();
    }
    public void handleArrow(int keyCode) {
        InputConnection ic = getCurrentInputConnection();
        Long now2 = System.currentTimeMillis();
        if (ctrl && shift) {
            ic.sendKeyEvent(new KeyEvent(now2, now2, KeyEvent.ACTION_DOWN, KEYCODE_CTRL_LEFT, 0, META_SHIFT_ON | META_CTRL_ON));
            moveSelection(keyCode);
            ic.sendKeyEvent(new KeyEvent(now2 , now2, KeyEvent.ACTION_UP, KEYCODE_CTRL_LEFT, 0, META_SHIFT_ON | META_CTRL_ON));

        } else if (shift)
            moveSelection(keyCode);
        else if (ctrl)
            ic.sendKeyEvent(new KeyEvent(now2, now2, KeyEvent.ACTION_DOWN, keyCode, 0,  META_CTRL_ON));
        else {sendDownUpKeyEvents(keyCode);}
    }

    private void moveSelection(int keyCode) {
        InputConnection ic = getCurrentInputConnection();
        Long now2 = System.currentTimeMillis();
        ic.sendKeyEvent(new KeyEvent(now2, now2, KeyEvent.ACTION_DOWN, KEYCODE_SHIFT_LEFT, 0, META_SHIFT_ON | META_CTRL_ON));
        if (ctrl)
            ic.sendKeyEvent(new KeyEvent(now2, now2, KeyEvent.ACTION_DOWN, keyCode, 0, META_SHIFT_ON | META_CTRL_ON));

        else
            ic.sendKeyEvent(new KeyEvent(now2, now2 , KeyEvent.ACTION_DOWN, keyCode, 0, META_SHIFT_ON));
        ic.sendKeyEvent(new KeyEvent(now2, now2, KeyEvent.ACTION_UP, KEYCODE_SHIFT_LEFT, 0, META_SHIFT_ON | META_CTRL_ON));
    }
    private void readMenber(){
        customStrMenber = new ArrayList<CustomStr>();
        try
        {
            FileInputStream fileInputStream =
                    new FileInputStream(this.filePath+"/customStrMenber.ser");
            ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
            int size = inputStream.readInt();
            if(size!=0){
                for(int i = 0;i < size;i++) {
                    customStrMenber.add((CustomStr)inputStream.readObject());
                }inputStream.close();fileInputStream.close();
            }
        }catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        updataCustomStr();
    }
    private void updataCustomStr(){
        keyboard = kv.getKeyboard();
        List<Keyboard.Key> keys = keyboard.getKeys();
        if(pageNumKey!=null)
            pageNumKey.label = String.valueOf(pageNum);
        for (int i = 0; i < keys.size(); i++) {
            CharSequence label  = keys.get(i).label;
            if(label != null){
                if (label.toString().indexOf(",")==2) {
                    if(label.toString().substring(0,2).equals("00")){
                        if(pageNumKey==null)
                            customArr.add(i);
                        int count = Integer.parseInt(label.toString().split(",")[1])+5*pageNum;
                        if(count<customStrMenber.size())
                            keys.get(i).label = ((CustomStr) customStrMenber.get(count)).strName;
                        else
                            keys.get(i).label = "None";
                    }
                }
                if(label.toString().equals("pageNum"))
                    pageNumKey = keys.get(i);
            }
        }
        if(customArr.size()>0){
            for(int i = 0;i < customArr.size();i++){
                int tempcount = ((int)customArr.get(i));
                int count = i+5*pageNum;
                if(count<customStrMenber.size())
                    keys.get(tempcount).label = ((CustomStr) customStrMenber.get(count)).strName;
                else
                    keys.get(tempcount).label = "None";
            }
        }
        if(pageNumKey!=null)
            pageNumKey.label = String.valueOf(pageNum);
        kv.invalidateAllKeys();
    }

    @Override
    public void swipeDown() {
        kv.closing();
    }
    @Override
    public void swipeLeft() {
    }
    @Override
    public void swipeRight() {
    }
    @Override
    public void swipeUp() {
    }
}