package com.gazlaws.codeboard;

import java.io.*;
import java.util.*;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.gazlaws.codeboard.based.ColorPickerView;
import com.gazlaws.codeboard.popup.PopupAlert;

public class GreaterSettingActivity extends AppCompatActivity {
    final AppCompatActivity thisActivity = this;

    int buttonNum = 1000;
    final int MostCustomKeys = 50;

    private String filePath = "/data/data/com.gazlaws.codeboard/files/customStrMenber";
    private ArrayList linearMenber = new ArrayList<LinearLayout>();
    private ArrayList customStrMenber = new ArrayList<CustomStr>();

    Hashtable TextEdit = new Hashtable<TextView,EditText>();
    Hashtable TextStr = new Hashtable<TextView,CustomStr>();
    Hashtable TextSave = new Hashtable<TextView,Button>();
    Hashtable SaveDelete = new Hashtable<Button,Button>();
    Hashtable ButtonText = new Hashtable<Button,TextView>();

    private LinearLayout ll;
    private TextView tv;
    private ColorPickerView colorPickerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greater_setting);
        readMenbers();
        for(int i = 0;i < customStrMenber.size();i++){
            addMenber((CustomStr) customStrMenber.get(i));
        }

        ll = (LinearLayout) findViewById(R.id.ll_color);
        tv = (TextView) findViewById(R.id.tv_info);
        colorPickerView = new ColorPickerView(this);
        ll.addView(colorPickerView);
        colorPickerView.setOnColorBackListener(new ColorPickerView.OnColorBackListener() {
            @Override
            public void onColorBack(int a, int r, int g, int b) {
                tv.setTextColor(Color.argb(a, r, g, b));
                SavePreferences("MyCustomColor",Color.argb(a, r, g, b));
            }
        });

    }
    private void SavePreferences(String key, int value) {
        SharedPreferences sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }
    private View generateMenber(){
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.custom_newmenber,null);
        v.findViewById(R.id.menber_rename).setId(buttonNum++);
        v.findViewById(R.id.menber_delete).setId(1000+buttonNum);
        return v;
    }
    public void reName(View v){
        final View vtemp = v;
        final PopupAlert popupAlert = new PopupAlert(this,R.layout.dialog_input_text);
        final Dialog dialog = popupAlert.getDialog();
        View view = popupAlert.getView();
        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        TextView confirm = (TextView) view.findViewById(R.id.confirm);
        ((TextView)view.findViewById(R.id.setinputName)).setText("重命名");
        popupAlert.getLayoutParams().gravity = Gravity.CENTER;
        popupAlert.SET();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputstr = ((EditText) dialog.findViewById(R.id.inputName)).getText().toString();
                if(inputstr!=""){
                    dialog.dismiss();
                    for(int i = 0; i < linearMenber.size();i++){
                        LinearLayout layout = ((LinearLayout)linearMenber.get(i));
                        if(layout.findViewById(vtemp.getId())!=null){
                            ((TextView)layout.findViewById(R.id.menberName)).setText(inputstr);
                            CustomStr ctemp = ((CustomStr)customStrMenber.get(i));
                            ctemp.setStrName(inputstr);
                            return;
                        }
                    }
                    System.out.println("找不到LinearMenber");
                }
                else ((Button) v).setText("输入不能为空");
            }
        });
        popupAlert.show();
    }
    public void deleteMenber(View v){
        final View vtemp = v;
        final AppCompatActivity activity = this;
        final PopupAlert popupAlert = new PopupAlert(this,R.layout.dialog_alert);
        final Dialog dialog = popupAlert.getDialog();
        View view = popupAlert.getView();
        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        TextView confirm = (TextView) view.findViewById(R.id.confirm);
        ((TextView)view.findViewById(R.id.setinputName)).setText("确认是否删除");
        ((TextView)view.findViewById(R.id.setinputName)).setTextColor(Color.parseColor("#ff0000"));
        popupAlert.getLayoutParams().gravity = Gravity.CENTER;
        popupAlert.SET();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                for(int i = 0; i < linearMenber.size();i++){
                    LinearLayout layout = ((LinearLayout)linearMenber.get(i));
                    if(layout.findViewById(vtemp.getId())!=null) {
                        LinearLayout layout01 = (LinearLayout) activity.findViewById(R.id.customStr);

                        Animation viewUp = AnimationUtils.loadAnimation(thisActivity,R.anim.view_up);
                        ((LinearLayout)linearMenber.get(i)).startAnimation(viewUp);

                        linearMenber.remove(i);customStrMenber.remove(i);
                        layout01.removeViewAt(i);
                        buttonNum--;
                        return;
                    }
                }
            }
        });
        popupAlert.show();
    }

    public void addMenber(View v){
        if(buttonNum-1000<MostCustomKeys){
            LinearLayout layout01 = (LinearLayout) this.findViewById(R.id.customStr);
            View temp = generateMenber();
            layout01.addView(temp);

            TextView textView = (TextView)temp.findViewById(R.id.custom_text_str);
            EditText editText = ((EditText)temp.findViewById(R.id.custom_edit_str));
            Button saveButton = (Button)temp.findViewById(R.id.menber_save);
            Button delButton = (Button)temp.findViewById(1000+buttonNum);
            CustomStr customStr = new CustomStr();
            TextEdit.put(textView, editText);
            TextStr.put(textView, customStr);
            TextSave.put(textView,saveButton);
            SaveDelete.put(saveButton,delButton);
            ButtonText.put(saveButton,textView);
            textView.setOnLongClickListener(new TextOnLongClick());

            customStrMenber.add(customStr);
            linearMenber.add((LinearLayout) temp);
            temp.setVisibility(View.VISIBLE);
            Animation viewDown = AnimationUtils.loadAnimation(thisActivity,R.anim.view_down);
            temp.startAnimation(viewDown);
            return;
        }
        alert("最大个数10");
    }
    private void addMenber(CustomStr customStr){
        LinearLayout layout01 = (LinearLayout) this.findViewById(R.id.customStr);
        View temp = generateMenber();
        ((TextView) temp.findViewById(R.id.menberName)).setText(customStr.strName);
        layout01.addView(temp);

        TextView textView = (TextView)temp.findViewById(R.id.custom_text_str);
        EditText editText = ((EditText)temp.findViewById(R.id.custom_edit_str));
        Button saveButton = (Button)temp.findViewById(R.id.menber_save);
        Button delButton = (Button)temp.findViewById(1000+buttonNum);

        textView.setText(customStr.customStr);

        TextEdit.put(textView, editText);
        TextStr.put(textView, customStr);
        TextSave.put(textView,saveButton);
        SaveDelete.put(saveButton,delButton);
        ButtonText.put(saveButton,textView);
        textView.setOnLongClickListener(new TextOnLongClick());

        linearMenber.add((LinearLayout) temp);
        temp.setVisibility(View.VISIBLE);
        Animation viewDown = AnimationUtils.loadAnimation(thisActivity,R.anim.view_down);
        temp.startAnimation(viewDown);
    }
    public void saveMenber(View v){
        TextView textview = (TextView)ButtonText.get((Button)v);
        EditText edittext = (EditText)TextEdit.get(textview);
        Button saveButton = (Button)TextSave.get(textview);
        Button delButton = (Button)SaveDelete.get(saveButton);
        Animation translateIn = AnimationUtils.loadAnimation(thisActivity,R.anim.button_in_left);
        Animation viewUp = AnimationUtils.loadAnimation(thisActivity,R.anim.view_up);

        saveButton.setVisibility(View.GONE);
        delButton.setVisibility(View.VISIBLE);
        delButton.startAnimation(translateIn);

        textview.setText(edittext.getText());
        ((CustomStr)TextStr.get(textview)).setCustomStr(edittext.getText().toString());

        edittext.startAnimation(viewUp);
        edittext.setVisibility(View.GONE);
        textview.setVisibility(View.VISIBLE);
    }
    public void saveMenbers(View v){
        File file = new File(filePath);
        if(!file.exists()){file.mkdirs();}
        try {
            FileOutputStream fileOutputStream =
                    new FileOutputStream(this.filePath+"/customStrMenber.ser");
            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
            outputStream.writeInt(customStrMenber.size());
            for(int i = 0;i < customStrMenber.size();i++){
                outputStream.writeObject(customStrMenber.get(i));
            }outputStream.close();fileOutputStream.close();
            alert("保存成功");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void readMenbers(){
        try
        {
            FileInputStream fileInputStream =
                    new FileInputStream(this.filePath+"/customStrMenber.ser");
            ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
            int size = inputStream.readInt();
            for(int i = 0;i < size;i++) {
                customStrMenber.add((CustomStr)inputStream.readObject());
            }inputStream.close();fileInputStream.close();
        }catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void alert(String alertStr){
        final AppCompatActivity activity = this;
        final PopupAlert popupAlert = new PopupAlert(this,R.layout.dialog_alert);
        final Dialog dialog = popupAlert.getDialog();
        View view = popupAlert.getView();
        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        TextView confirm = (TextView) view.findViewById(R.id.confirm);
        ((TextView)view.findViewById(R.id.setinputName)).setText(alertStr);
        ((TextView)view.findViewById(R.id.setinputName)).setTextColor(Color.parseColor("#ff0000"));
        popupAlert.getLayoutParams().gravity = Gravity.CENTER;
        popupAlert.SET();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        popupAlert.show();
    }
    private class TextOnLongClick implements View.OnLongClickListener{
        @Override
        public boolean onLongClick(View v) {
            TextView textView = (TextView)v;
            EditText editText = (EditText)TextEdit.get(textView);
            Button saveButton = (Button)TextSave.get(textView);
            Button delButton = (Button)SaveDelete.get(saveButton);
            Animation translateIn = AnimationUtils.loadAnimation(thisActivity,R.anim.button_in_left);
            Animation viewUp = AnimationUtils.loadAnimation(thisActivity,R.anim.view_up);

            editText.setText(textView.getText());

            delButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
            saveButton.startAnimation(translateIn);

            textView.startAnimation(viewUp);
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            return false;
        }
    }
}
class CustomStr implements java.io.Serializable{
    String customStr="";
    String strName="";

    public CustomStr() {
    }
    public CustomStr(String customStr, String strName) {
        this.customStr = customStr;
        this.strName = strName;
    }
    public String getCustomStr() {
        return customStr;
    }
    public void setCustomStr(String customStr) {
        this.customStr = customStr;
    }
    public String getStrName() {
        return strName;
    }
    public void setStrName(String strName) {
        this.strName = strName;
    }
}