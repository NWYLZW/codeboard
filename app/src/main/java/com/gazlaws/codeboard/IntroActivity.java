package com.gazlaws.codeboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by Ruby on 05/12/2016.
 */
/**
 * @Title: IntroActivity.java
 * @author Superme
 * @Description: 翻译
 * Supplement with IntelliJ IDEA
 * Supplement by YIJIE on 2019/11/2.
 */
public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 介绍你的CodeBoard's APP
        //设置你自己的幻灯片
//        addSlide(AppIntroFragment.newInstance(title, description, image, backgroundColor));
        addSlide(CodeboardIntro.newInstance(R.layout.codeboard_intro1));
        addSlide(CodeboardIntro.newInstance(R.layout.codeboard_intro2));
        addSlide(AppIntroFragment.newInstance(
                "下面是一些快捷键...",
                "点击 'ctrl' 来进行选择，剪切，复制，粘贴与撤回\n"+
                        "Ctrl+Shift+Z 返回上一步\n"+
                        "长按空格键可更改输入法\n"+
                        "SYM 有着更加多的符号",
                R.drawable.intro3, Color.parseColor("#555555")));
        addSlide(AppIntroFragment.newInstance(
                "SYM界面拥有着更高级的操作...",
                "快速生成特定代码片段\n[for(;;),printf,scanf]\n"+
                        "更加多的功能正在开发",
                R.drawable.intro4, Color.parseColor("#555555")));


        // 跳跃键按钮
       showSkipButton(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
//        setVibrate(true);
//        setVibrateIntensity(30);
//        setFadeAnimation();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
    public void enableButtonIntro(View v){
        Intent intent=new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
        startActivity(intent);
    }

    public void changeButtonIntro(View v){
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showInputMethodPicker();
    }
}

