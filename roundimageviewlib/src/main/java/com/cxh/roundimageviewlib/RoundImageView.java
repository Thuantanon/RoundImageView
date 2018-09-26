package com.cxh.roundimageviewlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

/**
 * Created by Cxh
 * Time : 2018/9/9  下午5:40
 * Desc :
 */
public class RoundImageView extends ImageView {

    private Paint mPaintBackgroud;
    private Paint mPaintContent;
    private Paint mPaintBorder;
    private Paint mPaintCover;

    private float mRadius;
    private int mBorderWidth = 0;
    private int mBorderColor = Color.TRANSPARENT;
    private int mFillColor = Color.TRANSPARENT;
    private int mCoverColor = Color.TRANSPARENT;
    private boolean mBorderOverlay;

    private boolean mDisableTransform;
    private Bitmap mImageBitmap;
    private BitmapShader mBitmapShader;
    private ColorFilter mColorFilter;
    private Matrix mShaderMatrix;

    private RectF mContentRect = new RectF();
    private RectF mBorderRect = new RectF();


    public RoundImageView(Context context) {
        super(context);
        init();
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // declare style
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView,
                defStyleAttr, 0);

        if (ta.hasValue(R.styleable.RoundImageView_cc_radius)) {
            mRadius = ta.getDimension(R.styleable.RoundImageView_cc_radius, 0);
        }
        if (ta.hasValue(R.styleable.RoundImageView_cc_border_width)) {
            mBorderWidth = (int) ta.getDimension(R.styleable
                    .RoundImageView_cc_border_width, 0);
        }
        if (ta.hasValue(R.styleable.RoundImageView_cc_border_color)) {
            mBorderColor = ta.getColor(R.styleable.RoundImageView_cc_border_color,
                    Color.TRANSPARENT);
        }
        if (ta.hasValue(R.styleable.RoundImageView_cc_fill_color)) {
            mFillColor = ta.getColor(R.styleable
                    .RoundImageView_cc_fill_color, Color.TRANSPARENT);
        }
        if (ta.hasValue(R.styleable.RoundImageView_cc_cover_color)) {
            mCoverColor = ta.getColor(R.styleable.RoundImageView_cc_cover_color, Color
                    .TRANSPARENT);
        }
        if (ta.hasValue(R.styleable.RoundImageView_cc_border_overlay)) {
            mBorderOverlay = ta.getBoolean(R.styleable.RoundImageView_cc_border_overlay,
                    false);
        }

        ta.recycle();

        init();
    }


    private void init() {
        super.setScaleType(ScaleType.CENTER_CROP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new RoundOutLineProvider());
        }

        initData();
    }


    private void initData() {

        if (mDisableTransform) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        if (mImageBitmap == null) {
            invalidate();
            return;
        }

        if (null == mBitmapShader) {
            mBitmapShader = new BitmapShader(mImageBitmap, Shader.TileMode.CLAMP, Shader.TileMode
                    .CLAMP);
        }

        if (null == mPaintContent) {
            mPaintContent = new Paint();
            mPaintContent.setAntiAlias(true);
            mPaintContent.setDither(true);
            mPaintContent.setStrokeWidth(0);
        }
        mPaintContent.setShader(mBitmapShader);

        if (null == mPaintCover) {
            mPaintCover = new Paint();
            mPaintCover.setDither(true);
            mPaintCover.setAntiAlias(true);
            mPaintCover.setStyle(Paint.Style.FILL);
        }
        mPaintCover.setColor(mCoverColor);

        if (null == mPaintBackgroud) {
            mPaintBackgroud = new Paint();
            mPaintBackgroud.setDither(true);
            mPaintBackgroud.setAntiAlias(true);
            mPaintBackgroud.setStyle(Paint.Style.FILL);
        }
        mPaintBackgroud.setColor(mFillColor);

        if (null == mPaintBorder) {
            mPaintBorder = new Paint();
            mPaintBorder.setDither(true);
            mPaintBorder.setAntiAlias(true);
            mPaintBorder.setStyle(Paint.Style.STROKE);
        }
        mPaintBorder.setColor(mBorderColor);
        mPaintBorder.setStrokeWidth(mBorderWidth);

        if (null == mBorderRect) {
            mBorderRect = new RectF();
        }
        mBorderRect.set(getEnableBounds());
        if (null == mContentRect) {
            mContentRect = new RectF();
        }
        mContentRect.set(mBorderRect);
        if (!mBorderOverlay && mBorderWidth > 0) {
            mContentRect.inset(mBorderWidth / 2 - 1.0f, mBorderWidth / 2 - 1.0f);
        }

        // 更新UI
        if (null != mPaintContent) {
            mPaintContent.setColorFilter(mColorFilter);
        }
        updateShaderMatrix();
        invalidate();
    }

    // 缩放图片
    private void updateShaderMatrix() {

        if (null == mShaderMatrix) {
            mShaderMatrix = new Matrix();
        }
        mShaderMatrix.set(null);

        float scale;
        float dx = 0;
        float dy = 0;

        if (mImageBitmap.getWidth() * mContentRect.height() > mImageBitmap.getHeight() *
                mContentRect.width()) {
            scale = mContentRect.height() / (float) mImageBitmap.getHeight();
            dx = (mContentRect.width() - mImageBitmap.getWidth() * scale) * 0.5f;
        } else {
            scale = mContentRect.width() / (float) mImageBitmap.getWidth();
            dy = (mContentRect.height() - mImageBitmap.getHeight() * scale) * 0.5f;
        }
        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate(dx + mContentRect.left, dy + mContentRect.top);
        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

    private RectF getEnableBounds() {
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight() - mBorderWidth;
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom() - mBorderWidth;
        int availableLeft = getPaddingLeft() + mBorderWidth / 2;
        int availableTop = getPaddingTop() + mBorderWidth / 2;

        return new RectF(availableLeft, availableTop, availableLeft + availableWidth,
                availableTop + availableHeight);
    }

    private void initBitmapData() {

        if (mDisableTransform) {
            mImageBitmap = null;
        } else {

            Drawable drawable = getDrawable();
            if (null != drawable) {
                if (drawable instanceof BitmapDrawable) {
                    mImageBitmap = ((BitmapDrawable) drawable).getBitmap();
                } else {
                    try {
                        // 如果src设置的是背景色
                        if (drawable instanceof ColorDrawable) {
                            mImageBitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
                        } else {
                            mImageBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                                    drawable
                                            .getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        }

                        Canvas canvas = new Canvas(mImageBitmap);
                        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        drawable.draw(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        initData();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mDisableTransform) {
            super.onDraw(canvas);
            return;
        }

        if (null == mImageBitmap) {
            return;
        }

        if (mFillColor != Color.TRANSPARENT) {
            canvas.drawRoundRect(mBorderRect, mRadius, mRadius, mPaintBorder);
        }

        canvas.drawRoundRect(mContentRect, mRadius, mRadius, mPaintContent);
        if (mCoverColor != Color.TRANSPARENT) {
            canvas.drawRoundRect(mContentRect, mRadius, mRadius, mPaintCover);
        }
        if (mBorderColor != Color.TRANSPARENT && mBorderWidth != 0) {
            canvas.drawRoundRect(mBorderRect, mRadius + mBorderWidth / 2, mRadius + mBorderWidth
                            / 2,
                    mPaintBorder);
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initData();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        initData();
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
        initData();
    }


    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        initBitmapData();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        initBitmapData();
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        initBitmapData();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        initBitmapData();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (cf == mColorFilter) {
            return;
        }

        mColorFilter = cf;
        if (null != mImageBitmap) {
            mPaintContent.setColorFilter(mColorFilter);
        }
        invalidate();
    }

    @Override
    public ColorFilter getColorFilter() {
        return mColorFilter;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class RoundOutLineProvider extends ViewOutlineProvider {

        @Override
        public void getOutline(View view, Outline outline) {
            if (mRadius > 0) {
                Rect rect = new Rect();
                rect.set((int) mBorderRect.left, (int) mBorderRect.top, (int) mBorderRect.right,
                        (int) mBorderRect.bottom);
                outline.setRoundRect(rect, mRadius + mBorderWidth / 2);
            }
        }
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != ScaleType.CENTER_CROP) {
            throw new IllegalArgumentException("RoundImageView only support ScaleType.CENTER_CROP");
        }
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (adjustViewBounds) {
            throw new IllegalArgumentException("RoundImageView not support setAdjustViewBounds");
        }
    }

    public boolean isDisableTransform() {
        return mDisableTransform;
    }

    public void setDisableTransform(boolean disableTransform) {
        if (disableTransform == this.mDisableTransform) {
            return;
        }
        this.mDisableTransform = disableTransform;
        initBitmapData();
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius) {
        if (radius == mRadius) {
            return;
        }
        mRadius = radius;
        initBitmapData();
    }

    public int getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        if (borderWidth == mBorderWidth) {
            return;
        }
        mBorderWidth = borderWidth;
        initData();
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(@ColorInt int borderColor) {
        if (borderColor == mBorderColor) {
            return;
        }
        mBorderColor = borderColor;
        initData();
    }

    public int getCoverColor() {
        return mCoverColor;
    }

    public void setCoverColor(@ColorInt int coverColor) {
        if (coverColor == mCoverColor) {
            return;
        }
        mCoverColor = coverColor;
        initData();
    }

    public boolean isBorderOverlay() {
        return mBorderOverlay;
    }

    public void setBorderOverlay(boolean borderOverlay) {
        if (borderOverlay == mBorderOverlay) {
            return;
        }
        mBorderOverlay = borderOverlay;
        initData();
    }

    public int getFillColor() {
        return mFillColor;
    }

    public void setFillColor(@ColorInt int fillColor) {
        if (fillColor == mFillColor) {
            return;
        }
        mFillColor = fillColor;
        initData();
    }
}
