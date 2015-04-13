package com.example.spotme.com.example.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.example.spotme.R;

/**
 * Created by Luffy on 4/13/2015.
 */
public class HomeScreen extends Activity
{
    @InjectView(R.id.up)
    Button _up;
    @InjectView(R.id.down)
    Button _down;
    @InjectView(R.id.line)
    View _view;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seesaw);
        ButterKnife.inject(this);
        AnimatorSet as = new AnimatorSet();
        as.play(onDownClick()).with(onUpClick());
        as.start();
    }
    public AnimatorSet onDownClick()
    {
        final ObjectAnimator transAnimation = ObjectAnimator.ofFloat(_up, View.TRANSLATION_Y, 320);
        final ObjectAnimator transAnimationT = ObjectAnimator.ofFloat(_up, View.TRANSLATION_X, -320);
        final ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(_view, View.ROTATION, 0,40);
        final ObjectAnimator rotateAnimationT = ObjectAnimator.ofFloat(_up, View.ROTATION, 0,40);
        _up.setPivotX(0);
        _up.setPivotY(_up.getHeight());
        final AnimatorSet as = new AnimatorSet();
        as.play(rotateAnimation).after(transAnimation);
        as.play(rotateAnimation).with(rotateAnimationT).with(transAnimationT);
        as.start();
        return as;
    }


    public AnimatorSet onUpClick()
    {
        final ObjectAnimator transAnimation = ObjectAnimator.ofFloat(_down, View.TRANSLATION_Y, 320);
        final ObjectAnimator transAnimationT = ObjectAnimator.ofFloat(_down, View.TRANSLATION_X, +320);
        final ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(_view, View.ROTATION, 0,40);
        final ObjectAnimator rotateAnimationT = ObjectAnimator.ofFloat(_down, View.ROTATION, 0,40);
        _down.setPivotX(0);
        _down.setPivotY(_down.getHeight());
        final AnimatorSet as = new AnimatorSet();
        as.play(rotateAnimation).after(transAnimation);
        as.play(rotateAnimation).with(rotateAnimationT).with(transAnimationT);
        as.start();
        return as;
    }

    @OnClick(R.id.up)
    public void onClickUp()
    {
        // Implement this
    }

    @OnClick(R.id.down)
    public void onClickDown()
    {
        // implement this
    }
}