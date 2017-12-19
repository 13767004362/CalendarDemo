package com.xingen.calendardemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by ${xinGen} on 2017/9/13.
 * <p>
 * 用途：
 * <p>
 * 日期表
 */
public class DateTableView extends View {
    private static final String TAG = DateTableView.class.getSimpleName();
    /**
     *  线段颜色
     */
    private static final int lineColor = Color.parseColor("#e9e5cb");
    /**
     * 周末文字颜色
     */
    private static final int weekTextColor = Color.parseColor("#999999");
    /**
     * 阳历字体颜色
     */
    private static final int solarTextColor = Color.parseColor("#333333");
    /**
     * 三种农历字体颜色
     */
    private static final int[] lunarTextArray = {Color.parseColor("#999999"), Color.parseColor("#ff4a4a"), Color.parseColor("#ff4a4a")};
    private Context context;
    /**
     * 阳历字体大小
     */
    private final float textSize_solar = 20;
    private final float textSize_lunar = 11;
    /**
     *  周末文字的大小
     */
    private final float textSize_week = 15.7f;
    private Paint paint;
    /**
     * padding 的一边的比率
     */
    private float paddingProportion = 0.06f;
    private float padding_top_week = 17.3f;
    /**
     * 周末文字到日期的偏移量
     */
    private float padding_top_view = 8;
    /**
     * 底部的偏移量
     */
    private float padding_bottom_view = 5.3f;
    private CompositeSubscription compositeSubscription;
    private String selectDate;
    public DateTableView(Context context) {
        super(context);
        this.context = context;
        initConfig();
    }
    public DateTableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initConfig();
    }
    private void initConfig() {
        this.compositeSubscription = new CompositeSubscription();
        createPaint();
        this.selectDate = getDefaultDate();
    }
    /**
     * 获取默认的日期
     *
     * @return
     */
    private String getDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(calendar.get(Calendar.YEAR));
        stringBuilder.append("-");
        stringBuilder.append(calendar.get(Calendar.MONTH) + 1);
        stringBuilder.append("-");
        stringBuilder.append(calendar.get(Calendar.DAY_OF_MONTH));
        return stringBuilder.toString();
    }
    /**
     * 创建画笔
     */
    private void createPaint() {
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
    }
    /**
     * 计算行数
     *
     * @return
     */
    private int calculateRows() {
        //先确定摆放天的行数(5行还是6行)
        int size = (7 - (month.firstDayWeek - 1)) + 4 * 7;
        return month.currentMaxDay <= size ? 5 : 6;
    }
    private int rowQuantity;
    private int columnQuantity;
    private float rowSize, columnSize;
    private int calculationViewWith() {
        return getContext().getResources().getDisplayMetrics().widthPixels;
    }
    private int calculationViewHeight() {
        int width = calculationViewWith();
        paddingTop = getPaddingTops();
        weekHeight = getWeekHeight();
        rowQuantity =( rowQuantity == 0 ? calculateRows() : rowQuantity);
        int height = (int) ( weekHeight + width * ( 1 - paddingProportion * 2 ) / 7 * rowQuantity + DisplayUtils.dip2px(context, padding_bottom_view) );
        Log.i(TAG," 计算高度 "+height+" 宽度 "+width+" 日期行数 ："+rowQuantity);
        return height;
    }
    private int getPaddingTops() {
        return paddingTop == 0 ? DisplayUtils.dip2px(context, padding_top_week) : paddingTop;
    }
    private int getWeekHeight() {
        if (weekHeight == 0) {
            //绘制背景
            paint.reset();
            paint.setAntiAlias(true);
            paint.setFakeBoldText(true);
            paint.setTextSize(DisplayUtils.sp2px(context, textSize_week));
            Rect rect = new Rect();
            paint.getTextBounds(weekTexts[0], 0, weekTexts[0].length(), rect);
            weekHeight = paddingTop + rect.height() + DisplayUtils.dip2px(context, padding_top_view);
        }
        return weekHeight;
    }
    /**
     * 行高与列宽
     */
    private void calculateRowOrColumnSize() {
        paddingTop = getPaddingTops();
        weekHeight = getWeekHeight();
        //行高与列宽
        rowQuantity = rowQuantity == 0 ? calculateRows() : rowQuantity;
        columnQuantity = 7;
        rowSize = (float) (getHeight() - weekHeight - DisplayUtils.dip2px(context, padding_bottom_view)) / rowQuantity;
        columnSize = (getWidth() * (1 - paddingProportion * 2)) / columnQuantity;
        Log.i(TAG," 计算行 的长和宽"+ columnSize+" "+rowSize);
        MonthUtils.calculationDayRect(month, weekHeight,(int) (getWidth()*paddingProportion), rowSize, columnSize);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (month == null){ return;}
        calculateRowOrColumnSize();
        drawWeekText(canvas);
        drawDateText(canvas);
    }
    private int paddingTop;
    private int weekHeight;
    private final String[] weekTexts = {"日", "一", "二", "三", "四", "五", "六"};
    /**
     * 绘制周末的文字
     *
     * @param canvas
     */
    private void drawWeekText(Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#cccccc"));
        paint.setStyle(Paint.Style.FILL);
        float y1=DisplayUtils.dip2px(context,0.5f);
        canvas.drawLine(0,y1,getWidth(),y1,paint);
        //画周日提示字
        paint.reset();
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setTextSize(DisplayUtils.sp2px(context, textSize_week));
        paint.setColor(weekTextColor);
        int move_left = (int) (getWidth() * paddingProportion);
        for (int i = 1; i <= weekTexts.length; ++i) {
            String week = weekTexts[i - 1];
            Rect rect1 = new Rect();
            paint.getTextBounds(week, 0, week.length(), rect1);
            float x = move_left + (i - 1) * columnSize + (columnSize - rect1.width()) / 2;
            float y = weekHeight - (weekHeight - rect1.height()) / 2;
            canvas.drawText(week, x, y, paint);
        }
    }
    /**
     * 绘制日期背景
     *
     * @param canvas
     */
    private void drawDateBG(RectF rectF, Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#1cbf61"));
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.height() / 2, paint);
    }
    /**
     * 判断是否选中日期
     *
     * @param day
     * @return
     */
    protected boolean isSelect(String day) {
        boolean select = false;
        String[] s1 = selectDate.split("-");
        String[] s2 = incomingDate.split("-");
        if (s1[0].equals(s2[0])) {
            if (Integer.valueOf(s1[1]) == Integer.valueOf(s2[1])) {
                if (Integer.valueOf(day) == Integer.valueOf(s1[2])) {
                    select = true;
                }
            }
        }
       // Log.i(TAG, "判断是否是选中日期    " + selectDate + " 传入的年月 " + incomingDate + " 天 " + day + select);
        return select;
    }
    /**
     * 绘制日期 : 阳历、农历、24节气、阳历假日、农历假日
     */
    private void drawDateText(Canvas canvas) {
        //绘制日期
        for (int i = 1; i <= month.currentMaxDay; ++i) {
            Month.Day day = month.dayList.get(i - 1);
            //计算每个号所占的区域面积
            RectF rectF = day.rectF;
            //选中的日期背景绘制
            boolean isSelect = isSelect(day.solar);
            if (isSelect) {
                drawDateBG(rectF, canvas);
            }
            //绘制阳历
            paint.reset();
            paint.setAntiAlias(true);
            paint.setTextSize(DisplayUtils.sp2px(context, textSize_solar));
            paint.setColor(isSelect ? Color.WHITE : day.solarTextColor);
            paint.setFakeBoldText(true);
            Rect solarBounds = new Rect();
            paint.getTextBounds(day.solar, 0, day.solar.length(), solarBounds);
            float left = rectF.centerX()- (float) solarBounds.width()/2 ;
            float bottom = rectF.centerY() -5;
            canvas.drawText(day.solar, left, bottom, paint);

            //绘制阴历
            paint.reset();
            paint.setColor(isSelect ? Color.WHITE : day.lunarTextColor);
            paint.setAntiAlias(true);
            paint.setTextSize(DisplayUtils.sp2px(context, textSize_lunar));
            Rect lunarBounds = new Rect();
            paint.getTextBounds(day.lunar, 0, day.lunar.length(), lunarBounds);
            float y1 = rectF.centerY()+lunarBounds.height()+5;
            float x1 = rectF.centerX()- (float) lunarBounds.width()/2;
            canvas.drawText(day.lunar, x1, y1, paint);
        }
    }
    /**
     * 绘制列线和行线
     *
     * @param canvas
     */
    private void drawColumnLineOrRowLine(Canvas canvas) {
        // 画行线
        for (int i = 1; i <= rowQuantity; ++i) {
            float start_x = 0;
            float start_y = weekHeight + i * rowSize;
            float end_x = getWidth();
            float end_y = start_y;
            drawLine(lineColor, start_x, start_y, end_x, end_y, canvas);
        }
        //画列线
        for (int i = 1; i <= columnQuantity; ++i) {
            float start_x = i * columnSize;
            float start_y = 0;
            float end_x = start_x;
            float end_y = getHeight();
            drawLine(lineColor, start_x, start_y, end_x, end_y, canvas);
        }
    }
    /**
     * 绘制Line
     *
     * @param color
     * @param start_x
     * @param start_y
     * @param end_x
     * @param end_y
     * @param canvas
     */
    public void drawLine(int color, float start_x, float start_y, float end_x, float end_y, Canvas canvas) {
        paint.reset();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(start_x, start_y, end_x, end_y, paint);
    }
    /**
     * 画text
     *
     * @param color
     * @param s
     * @param canvas x,y代表文字边框的左下角坐标
     */
    public void drawText(int color, String s, float x, float y, float textSize, Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(textSize);
        canvas.drawText(s, x, y, paint);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (month != null) {
            setMeasuredDimension(calculationViewWith(), calculationViewHeight());
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
    private int measureSize(int defaultSize, int measureSpec) {
        //设置高度一个默认值
        int result = DisplayUtils.dip2px(context, defaultSize);
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            //已经控件的大小
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            //遵循 AT_MOST,result不能大于specSize
            result = Math.min(result, specSize);
        } else { //设置高度一个默认值
            result = DisplayUtils.dip2px(context, defaultSize);
        }
        return result;
    }
    /**
     * 传入参数格式：年 - 月。
     * <p>
     * 例如：2017-09，不可传入2017-9。
     *
     * @param date
     */
    public void addData(String date) {
        if (month != null && month.currentMonth.equals(date)) {
            return;
        }
        conversionMonth(date);
    }
    /**
     * 传入时间
     */
    private String incomingDate;
    private void conversionMonth(final String date) {
        this.incomingDate = date;
        this.compositeSubscription.clear();
        Subscription subscription = Observable.create(new Observable.OnSubscribe<Month>() {
            @Override
            public void call(Subscriber<? super Month> subscriber) {
                Month month = Month.createInstance(date);
                subscriber.onNext(month);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Month>() {
            @Override
            public void call(Month months) {
                month = months;
                invalidate();
            }
        });
        this.compositeSubscription.add(subscription);
    }
    private Month month;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleClickEvent(event);
        return true;
    }
    private int down_x, down_y, move_x, move_y;
    /**
     * 处理点击事件
     *
     * @param event
     */
    private void handleClickEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.down_x = (int) event.getRawX();
                this.down_y = (int) event.getRawY();
                this.move_x = this.down_x;
                this.move_y = this.down_y;
                break;
            case MotionEvent.ACTION_MOVE:
                this.move_x = (int) event.getRawX();
                this.move_y = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                if (down_y == move_y && move_x == down_x) {
                    if (month != null) {
                        int x = (int) event.getX();
                        int y = (int) event.getY();
                        for (Month.Day day : month.dayList) {
                            if (day.rectF != null && day.rectF.contains(x, y)) {
                                showToast(" 您点击了 " + day.solar);
                                String date = getClickDate(day);
                                if (selectDate.equals(date)){ return;}
                                selectDate = date;
                                invalidate();
                                if (itemClickListener != null) {
                                    itemClickListener.clickDate(date);
                                }
                                break;
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
    private String getClickDate(Month.Day day) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(month.currentMonth);
        stringBuilder.append("-");
        if (day.solar.length() == 1) {
            stringBuilder.append("0");
        }
        stringBuilder.append(day.solar);
        return stringBuilder.toString();
    }
    private void showToast(String content) {
        Toast.makeText(getContext().getApplicationContext(), content, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (compositeSubscription != null) {
            compositeSubscription.clear();
        }
    }
    private ItemClickListener itemClickListener;
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    /**
     * 点击具体某一个日期的响应
     */
    public interface ItemClickListener {
        /**
         * 响应数据格式:2017-09-26
         *
         * @param clickDate
         */
        void clickDate(String clickDate);
    }

    /**
     * 月份数据
     */
    public static class Month {
        /**
         *  当前年，月
         */
        public String currentMonth;
        /**
         * 一个月的最大天数
         */
        public int currentMaxDay;
        public int firstDayWeek;
        /**
         *     一个月的天数
         */
        public List<Day> dayList;
        /**
         * 天的数据
         */
        public static class Day {
            /**
             * 屏幕中的区域
             */

            public RectF rectF;
            /**
             * 阳历
             */

            public String solar;
            /**
             *  农历
             */
            public String lunar;
            public int solarTextColor;
            public int lunarTextColor;
            public static final String[] specialLunar = {"春节", "元宵", "端午", "七夕", "中秋", "重阳", "腊八", "除夕"};
            public static final String[] specialSolar = {"元旦", "情人", "妇女", "植树", "愚人", "劳动", "青年", "儿童", "建党", "建军", "教师", "国庆", "光棍", "艾滋病", "圣诞"};
        }

        public static Month createInstance(String monthText) {
            Month month = new Month();
            month.currentMonth = monthText;
            MonthUtils.calculationMonth(monthText, month);
            MonthUtils.calculationDay(month);
            return month;
        }
    }
    private static class MonthUtils {
        public static Month calculationMonth(String monthText, Month month) {
            /**
             * 获取当前的年，月，号，当前月最大号数
             * <p>
             * Calendar中的月份是：0-11
             */
            Calendar calendar;
            String[] s = monthText.split("-");
            int currentYear = Integer.valueOf(s[0]);
            int currentMonth = Integer.valueOf(s[1]) - 1;
            calendar = Calendar.getInstance(Locale.getDefault());
            //设置年
            calendar.set(Calendar.YEAR, currentYear);
            calendar.set(Calendar.MONTH, currentMonth);
            //获取当月最大天数
            month.currentMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            //设置为第一天
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            //结果是以星期天作为第一天
            month.firstDayWeek = calendar.get(Calendar.DAY_OF_WEEK);
            return month;
        }

        /**
         * 计算一月的天数
         *
         * @param month
         */
        public static void calculationDay(Month month) {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            String[] s = month.currentMonth.split("-");
            int currentYear = Integer.valueOf(s[0]);
            int currentMonth = Integer.valueOf(s[1]) - 1;
            //设置年
            calendar.set(Calendar.YEAR, currentYear);
            calendar.set(Calendar.MONTH, currentMonth);
            List<Month.Day> dayList = new ArrayList<>();
            for (int i = 1; i <= month.currentMaxDay; ++i) {
                calendar.set(Calendar.DAY_OF_MONTH, i);
                Month.Day day = new Month.Day();
                day.solar = String.valueOf(i);
                day.solarTextColor = DateTableView.solarTextColor;
                day.lunar = new CalendarUtils(calendar).toString();
                day.lunarTextColor = calculationDayColor(day.lunar);
                dayList.add(day);
            }
            month.dayList = dayList;
        }

        /**
         * 计算特殊节日的文字颜色
         *
         * @param day
         * @return
         */
        public static int calculationDayColor(String day) {
            int color = lunarTextArray[0];
            for (int i = 0; i < CalendarUtils.SolarTermsUtil.principleTermNames.length; ++i) {
                if (day.equals(CalendarUtils.SolarTermsUtil.principleTermNames[i])) {
                    return lunarTextArray[2];
                }
            }
            for (int i = 0; i < CalendarUtils.SolarTermsUtil.sectionalTermNames.length; ++i) {
                if (day.equals(CalendarUtils.SolarTermsUtil.sectionalTermNames[i])) {
                    return lunarTextArray[2];
                }
            }
            for (int i = 0; i < Month.Day.specialSolar.length; ++i) {
                if (day.equals(Month.Day.specialSolar[i])) {
                    return lunarTextArray[1];
                }
            }
            for (int i = 0; i < Month.Day.specialLunar.length; ++i) {
                if (day.equals(Month.Day.specialLunar[i])) {
                    return lunarTextArray[1];
                }
            }
            return color;
        }

        public static void calculationDayRect(Month month, int weekHeight,float marginLeft,float rowSize, float columnSize) {
            for (int i = 1; i <= month.currentMaxDay; ++i) {
                Month.Day day = month.dayList.get(i - 1);
                int actualRow;
                int positionColumn;
                if ((month.firstDayWeek - 1 + i) % 7 == 0) {
                    actualRow = (month.firstDayWeek - 1 + i) / 7 - 1;
                    positionColumn = 7;
                } else {
                    actualRow = (month.firstDayWeek - 1 + i) / 7;
                    positionColumn = (month.firstDayWeek - 1 + i) - actualRow * 7;
                }
                //计算每个号所占的区域面积
                RectF rectF = new RectF();
                rectF.top = weekHeight + actualRow * rowSize;
                rectF.left = marginLeft+(positionColumn - 1) * columnSize;
                rectF.bottom = rectF.top + rowSize;
                rectF.right = rectF.left + columnSize;
                day.rectF = rectF;
            }
        }
    }

}
