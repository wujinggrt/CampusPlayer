package com.guochuang.campusplayer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringChain;
import com.guochuang.campusplayer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GUO on 2016/7/5.
 * 主页bar
 */
public class MenuBar extends RelativeLayout {

    private View mainView;

    private ImageView main_tool_bar_on;
    private ImageView main_tool_bar_off;
    private List<View> list = new ArrayList<>();
    private OnClickListener listener;

    private boolean isOpen;

    public MenuBar(Context context) {
        super(context);
        init(context);
    }

    public MenuBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MenuBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mainView = LayoutInflater.from(context).inflate(R.layout.menu_layout, this);
        View barLayout = mainView.findViewById(R.id.menu_layout);
        View barUpperLeft = mainView.findViewById(R.id.bar_upper_left);
        View barUpperRight = mainView.findViewById(R.id.bar_upper_right);
        View barBottomLeft = mainView.findViewById(R.id.bar_bottom_left);
        View barBottomRight = mainView.findViewById(R.id.bar_bottom_right);

        main_tool_bar_on = mainView.findViewById(R.id.main_bar_on);
        main_tool_bar_off = mainView.findViewById(R.id.main_bar_off);

        list.add(barUpperLeft);
        list.add(barUpperRight);
        list.add(barBottomLeft);
        list.add(barBottomRight);

        if (isInEditMode())
            return;

        setListener();
        initData();
    }


    private void setListener() {


        main_tool_bar_on.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOpen) {
                    isOpen = true;
                    if (listener != null) {
                        listener.onClick(v);
                    }
                    expand();
                }
            }
        });

        main_tool_bar_off.setOnClickListener(v -> {
            if (isOpen) {
                isOpen = false;
                collapse();
            }
        });
    }

    private void initData() {
    }


    public void loadAnimation(View view){
        for (int i = 0; i < list.size(); i++) {
            View animationView = list.get(i);
            if (view.getId() == animationView.getId()) {
                resolveButtonClickAnimationIn(animationView);
            } else {
                resolveButtonClickAnimationOut(animationView);
            }
        }
        OnButtonShow();
    }

    /**
     * 是否打开
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * 按键正常进入动画
     */
    public void startAnimationsIn() {

        SpringChain springChain = SpringChain.create(40, 6, 50, 7);

        for (int i = 0; i < list.size(); i++) {

            final View view = list.get(i);

            springChain.addSpring(new SimpleSpringListener() {
                @Override
                public void onSpringActivate(Spring spring) {
                    super.onSpringActivate(spring);
                    view.setVisibility(VISIBLE);
                }

                @Override
                public void onSpringUpdate(Spring spring) {
                    view.setTranslationY((float) spring.getCurrentValue());
                    float scale = (1 + 2 * (float) spring.getCurrentValue() / mainView.getHeight());
                    view.setScaleX(scale);
                    view.setScaleY(scale);
                }
            });
        }

        List<Spring> springs = springChain.getAllSprings();

        for (int i = 0; i < springs.size(); i++) {
            springs.get(i).setCurrentValue(mainView.getHeight());
        }

        springChain.setControlSpringIndex(0).getControlSpring().setEndValue(0);
    }

    /**
     * 按键正常退出动画
     */
    public void startAnimationsOut() {


        SpringChain springChain = SpringChain.create(40, 6, 50, 7);

        for (int i = 0; i < list.size(); i++) {

            final View view = list.get(i);

            springChain.addSpring(new SimpleSpringListener() {
                @Override
                public void onSpringUpdate(Spring spring) {
                    view.setTranslationY((float) spring.getCurrentValue());
                    if (spring.getCurrentValue() > 0) {
                        float scale = (mainView.getHeight() - 2 * (float)spring.getCurrentValue()) / mainView.getHeight();
                        view.setScaleX(scale);
                        view.setScaleY(scale);
                    }
                }
            });
        }

        List<Spring> springs = springChain.getAllSprings();

        for (int i = 0; i < springs.size(); i++) {
            springs.get(i).setCurrentValue(0);
        }

        springChain.setControlSpringIndex(3).getControlSpring().setEndValue(mainView.getHeight());


    }

    /**
     * 关闭所有
     */
    public void collapse() {
        startAnimationsOut();
        OnButtonShow();
        isOpen = false;
    }

    /**
     * 打开所有
     */
    public void expand() {
        startAnimationsIn();
        OffButtonShow();
        isOpen = true;
    }

    /**
     * 中间按键显示打开按键动画
     */
    private void OnButtonShow() {


        AnimationSet animationSet = new AnimationSet(true);

        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        Animation animation = getRotateAnimation(135, 0);

        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(animation);
        animationSet.setDuration(200);
        animationSet.setFillAfter(false);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                main_tool_bar_off.setVisibility(GONE);
                main_tool_bar_on.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        main_tool_bar_off.startAnimation(animationSet);

    }

    /**
     * 中间按键显示关闭按键动画
     */
    private void OffButtonShow() {
        AnimationSet animationSet = new AnimationSet(true);
        Animation animation = getRotateAnimation(0, 135);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.setDuration(200);
        animationSet.setFillAfter(false);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                main_tool_bar_off.setVisibility(VISIBLE);
                main_tool_bar_on.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animationSet.addAnimation(animation);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(new AlphaAnimation(1, 0));


        main_tool_bar_on.startAnimation(animationSet);
    }

    /**
     * 按键点击选中动画
     */
    private void resolveButtonClickAnimationIn(final View view) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);

        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.setDuration(200);
        animationSet.setFillAfter(false);

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animationSet);

    }

    /**
     * 按键点击退出动画
     */
    private void resolveButtonClickAnimationOut(final View view) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);

        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.setDuration(200);
        animationSet.setFillAfter(false);

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(GONE);
                isOpen = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animationSet);
    }

    /**
     * 按键点击跳转
     */
    private Animation getRotateAnimation(float fromDegrees,
                                         float toDegrees) {
        RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        return rotate;
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

}
