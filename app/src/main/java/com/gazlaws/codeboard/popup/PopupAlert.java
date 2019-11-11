package com.gazlaws.codeboard.popup;

import android.view.*;
import android.app.Dialog;
import com.gazlaws.codeboard.R;
import android.support.v7.app.AppCompatActivity;

public class PopupAlert{
    View view;
    Dialog dialog;
    Window dialogWindow;
    WindowManager.LayoutParams layoutParams;
    public PopupAlert(AppCompatActivity activity,int sourceseID) {
        dialog = new Dialog(activity, R.style.NoneDialogStyle);
        try{
            this.view = View.inflate(activity, sourceseID, null);
        } catch (Exception e) {
            System.out.println("没有资源");
            e.printStackTrace();
        }
        dialogWindow = dialog.getWindow();
        layoutParams = dialogWindow.getAttributes();
    }
    public void show(){
        dialog.show();
    }
    public void SET(){
        dialogWindow.setAttributes(this.layoutParams);
        dialog.setContentView(this.view);
    }
    public Dialog getDialog() {
        return dialog;
    }
    public View getView() {
        return view;
    }
    public WindowManager.LayoutParams getLayoutParams() {
        return layoutParams;
    }
}
