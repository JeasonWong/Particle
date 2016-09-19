package me.wangyuwei.particleview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;

import me.wangyuwei.particalview.R;

/**
 * 作者： 巴掌 on 16/8/27 11:29
 * Github: https://github.com/JeasonWong
 */
public class ParticleView extends View {

    private final int STATUS_MOTIONLESS = 0;
    private final int STATUS_PARTICLE_GATHER = 1;
    private final int STATUS_TEXT_MOVING = 2;

    private final int ROW_NUM = 10;
    private final int COLUMN_NUM = 10;

    private final int DEFAULT_MAX_TEXT_SIZE = sp2px(80);
    private final int DEFAULT_MIN_TEXT_SIZE = sp2px(30);

    public final int DEFAULT_TEXT_ANIM_TIME = 1000;
    public final int DEFAULT_SPREAD_ANIM_TIME = 300;
    public final int DEFAULT_HOST_TEXT_ANIM_TIME = 800;

    private Paint mHostTextPaint;
    private Paint mParticleTextPaint;
    private Paint mCirclePaint;
    private Paint mHostBgPaint;
    private int mWidth, mHeight;

    private Particle[][] mParticles = new Particle[ROW_NUM][COLUMN_NUM];
    private Particle[][] mMinParticles = new Particle[ROW_NUM][COLUMN_NUM];

    //背景色
    private int mBgColor;
    //粒子色
    private int mParticleColor;
    //默认粒子文案大小
    private int mParticleTextSize = DEFAULT_MIN_TEXT_SIZE;

    private int mStatus = STATUS_MOTIONLESS;

    private ParticleAnimListener mParticleAnimListener;

    //粒子文案
    private String mParticleText;
    //主文案
    private String mHostText;
    //扩散宽度
    private float mSpreadWidth;
    //Host文字展现宽度
    private float mHostRectWidth;
    //粒子文案的x坐标
    private float mParticleTextX;
    //Host文字的x坐标
    private float mHostTextX;

    //Text anim time in milliseconds
    private int mTextAnimTime;
    //Spread anim time in milliseconds
    private int mSpreadAnimTime;
    //HostText anim time in milliseconds
    private int mHostTextAnimTime;

    private PointF mStartMaxP, mEndMaxP;
    private PointF mStartMinP, mEndMinP;

    public ParticleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParticleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {

        TypedArray typeArray = getContext().obtainStyledAttributes(attrs, R.styleable.ParticleView);
        mHostText = null == typeArray.getString(R.styleable.ParticleView_pv_host_text) ? "" : typeArray.getString(R.styleable.ParticleView_pv_host_text);
        mParticleText = null == typeArray.getString(R.styleable.ParticleView_pv_particle_text) ? "" : typeArray.getString(R.styleable.ParticleView_pv_particle_text);
        mParticleTextSize = (int) typeArray.getDimension(R.styleable.ParticleView_pv_particle_text_size, DEFAULT_MIN_TEXT_SIZE);
        int hostTextSize = (int) typeArray.getDimension(R.styleable.ParticleView_pv_host_text_size, DEFAULT_MIN_TEXT_SIZE);
        mBgColor = typeArray.getColor(R.styleable.ParticleView_pv_background_color, 0xFF0867AB);
        mParticleColor = typeArray.getColor(R.styleable.ParticleView_pv_text_color, 0xFFCEF4FD);
        mTextAnimTime = typeArray.getInt(R.styleable.ParticleView_pv_text_anim_time, DEFAULT_TEXT_ANIM_TIME);
        mSpreadAnimTime = typeArray.getInt(R.styleable.ParticleView_pv_text_anim_time, DEFAULT_SPREAD_ANIM_TIME);
        mHostTextAnimTime = typeArray.getInt(R.styleable.ParticleView_pv_text_anim_time, DEFAULT_HOST_TEXT_ANIM_TIME);
        typeArray.recycle();

        mHostTextPaint = new Paint();
        mHostTextPaint.setAntiAlias(true);
        mHostTextPaint.setTextSize(hostTextSize);
        mParticleTextPaint = new Paint();
        mParticleTextPaint.setAntiAlias(true);
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mHostBgPaint = new Paint();
        mHostBgPaint.setAntiAlias(true);
        mHostBgPaint.setTextSize(hostTextSize);

        mParticleTextPaint.setTextSize(mParticleTextSize);
        mCirclePaint.setTextSize(mParticleTextSize);

        mParticleTextPaint.setColor(mBgColor);
        mHostTextPaint.setColor(mBgColor);
        mCirclePaint.setColor(mParticleColor);
        mHostBgPaint.setColor(mParticleColor);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mStartMinP = new PointF(mWidth / 2 - getTextWidth(mParticleText, mParticleTextPaint) / 2f - dip2px(4), mHeight / 2 + getTextHeight(mHostText, mHostTextPaint) / 2 - getTextHeight(mParticleText, mParticleTextPaint) / 0.7f);
        mEndMinP = new PointF(mWidth / 2 + getTextWidth(mParticleText, mParticleTextPaint) / 2f + dip2px(10), mHeight / 2 + getTextHeight(mHostText, mHostTextPaint) / 2);

        for (int i = 0; i < ROW_NUM; i++) {
            for (int j = 0; j < COLUMN_NUM; j++) {
                mMinParticles[i][j] = new Particle(mStartMinP.x + (mEndMinP.x - mStartMinP.x) / COLUMN_NUM * j, mStartMinP.y + (mEndMinP.y - mStartMinP.y) / ROW_NUM * i, dip2px(0.8f));
            }
        }

        mStartMaxP = new PointF(mWidth / 2 - DEFAULT_MAX_TEXT_SIZE, mHeight / 2 - DEFAULT_MAX_TEXT_SIZE);
        mEndMaxP = new PointF(mWidth / 2 + DEFAULT_MAX_TEXT_SIZE, mHeight / 2 + DEFAULT_MAX_TEXT_SIZE);

        for (int i = 0; i < ROW_NUM; i++) {
            for (int j = 0; j < COLUMN_NUM; j++) {
                mParticles[i][j] = new Particle(mStartMaxP.x + (mEndMaxP.x - mStartMaxP.x) / COLUMN_NUM * j, mStartMaxP.y + (mEndMaxP.y - mStartMaxP.y) / ROW_NUM * i, getTextWidth(mHostText + mParticleText, mParticleTextPaint) / (COLUMN_NUM * 1.8f));
            }
        }

        Shader linearGradient = new LinearGradient(mWidth / 2 - getTextWidth(mParticleText, mCirclePaint) / 2f,
                mHeight / 2 - getTextHeight(mParticleText, mCirclePaint) / 2,
                mWidth / 2 - getTextWidth(mParticleText, mCirclePaint) / 2,
                mHeight / 2 + getTextHeight(mParticleText, mCirclePaint) / 2,
                new int[]{mParticleColor, Color.argb(120, getR(mParticleColor), getG(mParticleColor), getB(mParticleColor))}, null, Shader.TileMode.CLAMP);
        mCirclePaint.setShader(linearGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mStatus == STATUS_PARTICLE_GATHER) {
            for (int i = 0; i < ROW_NUM; i++) {
                for (int j = 0; j < COLUMN_NUM; j++) {
                    canvas.drawCircle(mParticles[i][j].x, mParticles[i][j].y, mParticles[i][j].radius, mCirclePaint);
                }
            }
        }

        if (mStatus == STATUS_TEXT_MOVING) {
            canvas.drawText(mHostText, mHostTextX, mHeight / 2 + getTextHeight(mHostText, mHostBgPaint) / 2, mHostBgPaint);
            canvas.drawRect(mHostTextX + mHostRectWidth, mHeight / 2 - getTextHeight(mHostText, mHostBgPaint) / 1.2f, mHostTextX + getTextWidth(mHostText, mHostTextPaint), mHeight / 2 + getTextHeight(mHostText, mHostBgPaint) / 1.2f, mHostTextPaint);
        }

        if (mStatus == STATUS_PARTICLE_GATHER) {
            canvas.drawRoundRect(new RectF(mWidth / 2 - mSpreadWidth, mStartMinP.y, mWidth / 2 + mSpreadWidth, mEndMinP.y), dip2px(2), dip2px(2), mHostBgPaint);
            canvas.drawText(mParticleText, mWidth / 2 - getTextWidth(mParticleText, mParticleTextPaint) / 2, mStartMinP.y + (mEndMinP.y - mStartMinP.y) / 2 + getTextHeight(mParticleText, mParticleTextPaint) / 2, mParticleTextPaint);
        } else if (mStatus == STATUS_TEXT_MOVING) {
            canvas.drawRoundRect(new RectF(mParticleTextX - dip2px(4), mStartMinP.y, mParticleTextX + getTextWidth(mParticleText, mParticleTextPaint) + dip2px(4), mEndMinP.y), dip2px(2), dip2px(2), mHostBgPaint);
            canvas.drawText(mParticleText, mParticleTextX, mStartMinP.y + (mEndMinP.y - mStartMinP.y) / 2 + getTextHeight(mParticleText, mParticleTextPaint) / 2, mParticleTextPaint);
        }

    }

    private void startParticleAnim() {

        mStatus = STATUS_PARTICLE_GATHER;

        Collection<Animator> animList = new ArrayList<>();

        ValueAnimator textAnim = ValueAnimator.ofInt(DEFAULT_MAX_TEXT_SIZE, mParticleTextSize);
        textAnim.setDuration((int) (mTextAnimTime * 0.8f));
        textAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int textSize = (int) valueAnimator.getAnimatedValue();
                mParticleTextPaint.setTextSize(textSize);
            }
        });
        animList.add(textAnim);

        for (int i = 0; i < ROW_NUM; i++) {
            for (int j = 0; j < COLUMN_NUM; j++) {
                final int tempI = i;
                final int tempJ = j;
                ValueAnimator animator = ValueAnimator.ofObject(new LineEvaluator(), mParticles[i][j], mMinParticles[i][j]);
                animator.setDuration(mTextAnimTime + ((int) (mTextAnimTime * 0.02f)) * i + ((int) (mTextAnimTime * 0.03f)) * j);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mParticles[tempI][tempJ] = (Particle) animation.getAnimatedValue();
                        if (tempI == ROW_NUM - 1 && tempJ == COLUMN_NUM - 1) {
                            invalidate();
                        }
                    }
                });
                animList.add(animator);
            }
        }

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animList);
        set.start();

        set.addListener(new AnimListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startSpreadAnim();
            }
        });

    }

    private void startSpreadAnim() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, getTextWidth(mParticleText, mParticleTextPaint) / 2 + dip2px(4));
        animator.setDuration(mSpreadAnimTime);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSpreadWidth = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new AnimListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startHostTextAnim();
            }
        });
        animator.start();
    }

    private void startHostTextAnim() {
        mStatus = STATUS_TEXT_MOVING;

        Collection<Animator> animList = new ArrayList<>();

        ValueAnimator particleTextXAnim = ValueAnimator.ofFloat(mStartMinP.x + dip2px(4), mWidth / 2 - (getTextWidth(mHostText, mHostTextPaint) + getTextWidth(mParticleText, mParticleTextPaint)) / 2 + getTextWidth(mHostText, mHostTextPaint));
        particleTextXAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mParticleTextX = (float) animation.getAnimatedValue();
            }
        });
        animList.add(particleTextXAnim);

        ValueAnimator animator = ValueAnimator.ofFloat(0, getTextWidth(mHostText, mHostTextPaint));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHostRectWidth = (float) animation.getAnimatedValue();
            }
        });
        animList.add(animator);

        ValueAnimator hostTextXAnim = ValueAnimator.ofFloat(mStartMinP.x, mWidth / 2 - (getTextWidth(mHostText, mHostTextPaint) + getTextWidth(mParticleText, mParticleTextPaint) + dip2px(20)) / 2);
        hostTextXAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHostTextX = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animList.add(hostTextXAnim);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animList);
        set.setDuration(mHostTextAnimTime);
        set.addListener(new AnimListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (null != mParticleAnimListener) {
                    mParticleAnimListener.onAnimationEnd();
                }
            }
        });
        set.start();

    }

    public void startAnim() {
        post(new Runnable() {
            @Override
            public void run() {
                startParticleAnim();
            }
        });
    }

    private abstract class AnimListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    public void setOnParticleAnimListener(ParticleAnimListener particleAnimListener) {
        mParticleAnimListener = particleAnimListener;
    }

    public interface ParticleAnimListener {
        void onAnimationEnd();
    }

    private int dip2px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private float getTextHeight(String text, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height() / 1.1f;
    }

    private float getTextWidth(String text, Paint paint) {
        return paint.measureText(text);
    }

    private int getR(int color) {
        int r = (color >> 16) & 0xFF;
        return r;
    }

    private int getG(int color) {
        int g = (color >> 8) & 0xFF;
        return g;
    }

    private int getB(int color) {
        int b = color & 0xFF;
        return b;
    }
}

