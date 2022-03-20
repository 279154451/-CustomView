package com.single.code.flowlayout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间：2022/3/19
 * 创建人：singleCode
 * 功能描述：
 **/
public class FlowLayout extends ViewGroup {

    private int mHorizontalSpacing = dp2px(2);//一行中每个子View的间隔大小
    private int mVerticalSpacing = dp2px(2);//每一行的间隔大小
    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private List<List<View>> allLines = new ArrayList<>();//记录所有的行，一行一行的存储
    private List<Integer> lineHeights = new ArrayList<>();//记录每一行的高度

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //因为子View 可能会被多次执行测量，所以onMeasure可能会执行多次，所以这里的列表需要清零
        allLines.clear();
        lineHeights.clear();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int childCount = getChildCount();
        int selfWidth = MeasureSpec.getSize(widthMeasureSpec);//ViewGroup解析爷爷传下来的限制宽度和高度
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec);
        List<View> lineView = new ArrayList<>();//保存一行中所有的View
        int lineWidthUse = 0;//记录这一行已经使用了多少宽
        int lineHeight = 0;//这一行的高度，取最高的那一个

        int viewGroupNeededWidth = 0;//ViewGroup自身的测量宽
        int viewGroupNeededHeight = 0;//ViewGroup自身的测量高
        for (int i=0;i<childCount;i++){
            View child = getChildAt(i);
            if(child.getVisibility() == GONE) continue;//子View不可见不需要测量
            LayoutParams params = child.getLayoutParams();
            int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,paddingLeft+paddingRight,params.width);
            int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,paddingTop+paddingBottom,params.height);
            child.measure(childWidthMeasureSpec,childHeightMeasureSpec);
            int childMeasureWidth = child.getMeasuredWidth();
            int childMeasureHeight = child.getMeasuredHeight();
            //如果一行中新增child后宽度总宽度大于viewGroup自身限制大小，则需要换行
            if(childMeasureWidth+lineWidthUse+mHorizontalSpacing>selfWidth){
                allLines.add(lineView);//保存这一行的所有View
                lineHeights.add(lineHeight);//保存行高
                viewGroupNeededHeight =viewGroupNeededHeight+lineHeight+mVerticalSpacing;
                viewGroupNeededWidth = Math.max(viewGroupNeededWidth,lineWidthUse+mHorizontalSpacing);
                lineView = new ArrayList<>();
                lineWidthUse = 0;
                lineHeight = 0;
            }
            lineView.add(child);
            lineWidthUse = lineWidthUse+childMeasureWidth+mHorizontalSpacing;
            lineHeight = Math.max(lineHeight,childMeasureHeight);//取一行中最高的哪一个
            if(i == childCount-1){//处理最后一行
                allLines.add(lineView);
                lineHeights.add(lineHeight);
                viewGroupNeededHeight =viewGroupNeededHeight+lineHeight+mVerticalSpacing;
                viewGroupNeededWidth = Math.max(viewGroupNeededWidth,lineWidthUse+mHorizontalSpacing);
            }
        }
        int viewGroupWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int viewGroupHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        //如果爷爷给ViewGroup设置了确定的宽，那viewGroup的宽就只能爷爷给的值selfWidth,否则是子View计算来的值
        int realWidth = (viewGroupWidthMode == MeasureSpec.EXACTLY)?selfWidth:viewGroupNeededWidth;
        int realHeight = (viewGroupHeightMode == MeasureSpec.EXACTLY)?selfHeight:viewGroupNeededHeight;

        setMeasuredDimension(realWidth,realHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int currentLeft = getPaddingLeft();
        int currentTop = getPaddingTop();
        int lineCount = allLines.size();
        for (int i=0;i<lineCount;i++){//一行行确定布局
            List<View> lineViews = allLines.get(i);
            for (int j =0;j<lineViews.size();j++){
                View child = lineViews.get(j);
                int left = currentLeft;
                int top = currentTop;
                int right = left+child.getMeasuredWidth();
                int bottom = top+child.getMeasuredHeight();
                child.layout(left,top,right,bottom);
                currentLeft = right+mHorizontalSpacing;
            }
            //换行后新的一行起始Top
            currentTop = currentTop+lineHeights.get(i)+mVerticalSpacing;
            //换行后新的一行起始Left
            currentLeft = getPaddingLeft();

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
//        paint.setStrokeWidth(1);//设置笔宽
//        paint.setAntiAlias(true);//是否抗锯齿
//        // 生成色彩矩阵
//        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
//                1, 0, 0, 0, 0,
//                0, 1, 0, 0, 0,
//                0, 0, 1, 0, 0,
//                0, 0, 0, 1, 0,
//        });
//        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
//
//        // 设置画笔颜色为自定义颜色
//        paint.setColor(Color.argb(255, 255, 128, 103));
//
//        // 绘制圆环 (x坐标，y坐标，半径，画笔)
//        canvas.drawCircle(240, 600 / 2, 200, paint);
    }

    public static int dp2px(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp, Resources.getSystem().getDisplayMetrics());
    }
}
