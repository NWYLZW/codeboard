package com.gazlaws.codeboard;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class SponsorActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(AppIntroFragment.newInstance(
                "欢迎姥爷打赏",
                "上面是支付宝二维码,支持花呗哦\n"+
                        "下一页微信支付",
                R.drawable.pay_ali, Color.parseColor("#888888")));
        addSlide(AppIntroFragment.newInstance(
                "欢迎姥爷打赏",
                "上面是微信二维码",
                R.drawable.pay_wechat, Color.parseColor("#888888")));
        setSkipText("X");
        setDoneText("X");
    }
    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }
    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }
}
