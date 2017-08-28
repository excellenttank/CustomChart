package com.excellent_tank.customchart.chart;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.excellent_tank.customchart.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 各路段事故数量统计
 *
 * @author wbb
 */
public class HistogramChart extends View {

    private String[] pileNo;
    private Resources resources;
    List<HashMap<String, Object>> pileNoDateList = new ArrayList<HashMap<String, Object>>();

    private List<HashMap<String, Integer>> accident_data = new ArrayList<HashMap<String, Integer>>();

    private int[] accident_count;

    private int maxValue;

    private int addValue;

    private DashPathEffect dashPathEffect;

    /**
     * 用于放置Y轴刻度值
     */
    private LinearLayout histogram_text_ll;

    private boolean drawYDegree = false;

    private LinearLayout histogram_text_whole_ll;

    private Paint paintCommon = new Paint();

    private Path path = new Path();

    private boolean isRefresh = false;

    private int screenWidth = 0;

    public boolean isRefresh() {
        return isRefresh;
    }

    public void setRefresh(boolean isRefresh) {
        this.isRefresh = isRefresh;
    }

    private void init() {
        resources = this.getContext().getResources();
        DisplayMetrics metric = getResources().getDisplayMetrics();
        screenWidth = metric.widthPixels; // 屏幕宽度（像素）
        dashPathEffect = new DashPathEffect(new float[]{resources.getDimension(R.dimen.accidentDashDistance), resources.getDimension(R.dimen.accidentDashDistance), resources.getDimension(R.dimen.accidentDashDistance), resources.getDimension(R.dimen.accidentDashDistance)}, 1);
    }

    public HistogramChart(Context context) {
        super(context);
        init();
    }

    public HistogramChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HistogramChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureWidth();
        int height = (int) resources.getDimension(R.dimen.accident_grid_height);
        setMeasuredDimension(width, height);
    }

    private int measureWidth() {
        int result = 0; // 结果
        int myWidth = 0;
        if (null != pileNo && pileNo.length > 0) {
            myWidth = (int) resources.getDimension(R.dimen.accident_begin_x) + (int) (pileNo.length * resources.getDimension(R.dimen.accident_y_space) + resources.getDimension(R.dimen.accident_begin_y) * 2);
        }

        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        if (myWidth < screenWidth) {
            result = screenWidth - (int) (2 * resources.getDimension(R.dimen.cb_padding_right));
            drawYDegree = true;
        } else {
            result = myWidth - (int) (3 * resources.getDimension(R.dimen.cb_padding_right));
        }

        return result;
    }

    public void setData(List<HashMap<String, Object>> pileNoDateList, LinearLayout histogram_text_ll, LinearLayout histogram_text_whole_ll) {

        accident_count = new int[]{0, 5, 10, 15, 20, 25};
        maxValue = 0;
        accident_data.clear();

        this.histogram_text_whole_ll = histogram_text_whole_ll;

        this.histogram_text_ll = histogram_text_ll;

        this.pileNoDateList.clear();
        this.pileNoDateList.addAll(pileNoDateList);

        pileNo = new String[pileNoDateList.size()];

        for (int i = 0; i < pileNoDateList.size(); i++) {
            HashMap<String, Integer> hm = new HashMap<String, Integer>();
            int pileNoCache = (int) Float.parseFloat(pileNoDateList.get(i).get("startPileNo").toString());
            pileNo[i] = String.valueOf(pileNoCache);

            int downCount = (int) Float.parseFloat(pileNoDateList.get(i).get("downCount").toString());

            int upCount = (int) Float.parseFloat(pileNoDateList.get(i).get("upCount").toString());

            if (maxValue < downCount) {
                maxValue = downCount;
            }

            if (maxValue < upCount) {
                maxValue = upCount;
            }

            hm.put("left", (int) Float.parseFloat(pileNoDateList.get(i).get("upCount").toString()));
            hm.put("right", (int) Float.parseFloat(pileNoDateList.get(i).get("downCount").toString()));

            accident_data.add(hm);

        }

        if (maxValue > 25) {
            addValue = (int) ((maxValue / 5f) + 1f);
            for (int i = 1; i < accident_count.length; i++) {
                accident_count[i] = i * addValue;
            }
        } else {
            addValue = 5;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        resources = null;
        pileNoDateList.clear();
        pileNoDateList = null;
        accident_data.clear();
        accident_data = null;
        paintCommon = null;
        path = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isRefresh)
            return;

        float gridX = resources.getDimension(R.dimen.accident_begin_x);

        float gridY = resources.getDimension(R.dimen.accident_begin_y);

        float x_space = resources.getDimension(R.dimen.accident_y_space);

        int lineCount = 11;

        float y_space = (getHeight() - resources.getDimension(R.dimen.bottom_subtractor) - gridY - 2 * resources.getDimension(R.dimen.y_padding)) / (lineCount - 1);

        float y_single_height = (getHeight() - resources.getDimension(R.dimen.bottom_subtractor) - gridY - 2 * resources.getDimension(R.dimen.y_padding)) / (accident_count[accident_count.length - 1] * 2);

        float gridMiddlePosition = (getHeight() - resources.getDimension(R.dimen.bottom_subtractor) - gridY) / 2f;

        path.reset();

        if (null != pileNo && pileNo.length > 0) {
            float chart_width = x_space * pileNo.length;
            if (chart_width < screenWidth) {

                gridX = (screenWidth / 2f) - (chart_width / 2f);

            }

            paintCommon.reset();
            paintCommon.setAntiAlias(true);// 抗锯齿
            paintCommon.setColor(Color.parseColor("#E4E4E4"));
            paintCommon.setAlpha(50);

            // 绘制背景矩形
            canvas.drawRect(gridX, //
                    gridY - resources.getDimension(R.dimen.line_space), //
                    gridX + x_space * pileNo.length, //
                    gridY + resources.getDimension(R.dimen.line_space) + y_space * (lineCount - 1) + 2 * resources.getDimension(R.dimen.y_padding), paintCommon);

            paintCommon.reset();
            paintCommon.setAntiAlias(true);// 抗锯齿
            paintCommon.setColor(Color.parseColor("#E4E4E4"));
            paintCommon.setStrokeWidth(resources.getDimension(R.dimen.accidentXYLineWidth));
            // 最上方的横线
            canvas.drawLine(gridX, //
                    gridY - resources.getDimension(R.dimen.line_space), //
                    gridX + x_space * pileNo.length, //
                    gridY - resources.getDimension(R.dimen.line_space), //
                    paintCommon);
            // 最下方的横线
            canvas.drawLine(gridX, //
                    gridY + resources.getDimension(R.dimen.line_space) + y_space * (lineCount - 1) + 2 * resources.getDimension(R.dimen.y_padding), //
                    gridX + x_space * pileNo.length, //
                    gridY + resources.getDimension(R.dimen.line_space) + y_space * (lineCount - 1) + 2 * resources.getDimension(R.dimen.y_padding), //
                    paintCommon);
            // 最右方的竖线
            canvas.drawLine(gridX + x_space * pileNo.length - resources.getDimension(R.dimen.line_space), //
                    gridY, //
                    gridX + x_space * pileNo.length - resources.getDimension(R.dimen.line_space), //
                    gridY + y_space * (lineCount - 1) + 2 * resources.getDimension(R.dimen.y_padding), //
                    paintCommon);
            // 绘制纵向线
            for (int i = 1; i < pileNo.length; i++) {
                canvas.drawLine(gridX + x_space * i, //
                        gridY + y_space * (lineCount - 1) + 2 * resources.getDimension(R.dimen.y_padding), //
                        gridX + x_space * i, //
                        gridY + y_space * (lineCount - 1) + resources.getDimension(R.dimen.lenged_length) + 2 * resources.getDimension(R.dimen.y_padding), //
                        paintCommon);

            }
            // 绘制内部框线
            paintCommon.reset();
            paintCommon.setStyle(Style.STROKE);
            paintCommon.setAntiAlias(true);// 抗锯齿
            paintCommon.setStrokeWidth(1f);
            paintCommon.setColor(Color.parseColor("#E4E4E4"));
            paintCommon.setPathEffect(dashPathEffect);// 虚线设置

            path.reset();

            // 绘制横向内部虚线
            float xx = x_space * pileNo.length;

            for (int i = 0; i < lineCount; i++) {

                int xC = (int) xx / screenWidth;
                float moveToY = gridY + resources.getDimension(R.dimen.y_padding) + y_space * i;
                float lineToY = gridY + resources.getDimension(R.dimen.y_padding) + y_space * i;
                if (xC <= 0) {
                    float moveToX = gridX;
                    float lineToX = gridX + xx;
                    path.reset();
                    path.moveTo(moveToX, moveToY);
                    path.lineTo(lineToX, lineToY);
                    canvas.drawPath(path, paintCommon);

                } else {
                    xC += 1;
                    for (int m = 0; m < xC; m++) {
                        float moveToX = gridX + screenWidth * m;
                        float lineToX = gridX + screenWidth * (m + 1);
                        path.reset();
                        path.moveTo(moveToX, moveToY);
                        path.lineTo(lineToX, lineToY);
                        canvas.drawPath(path, paintCommon);

                    }

                }

            }
            //			path.reset();
            //			for (int i = 0; i < lineCount; i++) {
            //
            //				float moveToX = gridX;
            //				float moveToY = gridY + resources.getDimension(R.dimen.y_padding) + y_space * i;
            //				float lineToX = gridX + x_space * pileNo.length;
            //				float lineToY = gridY + resources.getDimension(R.dimen.y_padding) + y_space * i;
            //
            //				path.moveTo(moveToX,moveToY );
            //				path.lineTo(lineToX/2f,lineToY );
            //				canvas.drawPath(path, paintCommon);
            //			}
            //			path.reset();
            //			for (int i = 0; i < lineCount; i++) {
            //
            //				float moveToX = gridX;
            //				float moveToY = gridY + resources.getDimension(R.dimen.y_padding) + y_space * i;
            //				float lineToX = gridX + x_space * pileNo.length;
            //				float lineToY = gridY + resources.getDimension(R.dimen.y_padding) + y_space * i;
            //
            //				path.moveTo(lineToX/2f+50,moveToY );
            //				path.lineTo(lineToX,lineToY );
            //				canvas.drawPath(path, paintCommon);
            //			}

            // 绘制X周刻度
            paintCommon.reset();
            paintCommon.setAntiAlias(true);// 抗锯齿
            paintCommon.setColor(Color.parseColor("#7E7E7E"));
            paintCommon.setTextSize(resources.getDimension(R.dimen.pile_no_textsize));
            paintCommon.setTextAlign(Align.CENTER);
            // 绘制桩号
            for (int i = 0; i < pileNo.length; i++) {
                canvas.drawText(pileNo[i], gridX + x_space / 2 + x_space * i, getHeight() - resources.getDimension(R.dimen.bottom_text_subtractor), paintCommon);
            }
            // 绘制圆角矩形
            float start_space = x_space / 2f - resources.getDimension(R.dimen.histogram_width) / 2f;

            int accident_data_count = accident_data.size();

            if (accident_data_count > 0) {
                for (int i = 0; i < accident_data_count; i++) {

                    int leftSize = accident_data.get(i).get("left");
                    int rightSize = accident_data.get(i).get("right");

                    int leftEnd = leftSize;

                    int rightEnd = rightSize;
                    // 左方向
                    paintCommon.reset();
                    paintCommon.setAntiAlias(true);// 抗锯齿
                    paintCommon.setTextSize(resources.getDimension(R.dimen.pile_no_textsize));
                    paintCommon.setColor(Color.parseColor("#00CCFF"));
                    canvas.drawRect(gridX + x_space * i + start_space,//
                            gridY + gridMiddlePosition - leftEnd * y_single_height,//
                            gridX + x_space * i + start_space + resources.getDimension(R.dimen.histogram_width),//
                            gridY + gridMiddlePosition, paintCommon);

                    // 绘制左方向的数值
                    if (leftSize > 0) {
                        paintCommon.reset();
                        paintCommon.setAntiAlias(true);// 抗锯齿
                        paintCommon.setTextSize(resources.getDimension(R.dimen.left_right_text_size));
                        paintCommon.setTextAlign(Align.CENTER);
                        paintCommon.setColor(Color.parseColor("#00CCFF"));
                        canvas.drawText(String.valueOf(leftSize), //
                                gridX + x_space / 2 + x_space * i, //
                                gridY + gridMiddlePosition - leftEnd * y_single_height - resources.getDimension(R.dimen.left_count_y_offset), //
                                paintCommon);//
                    }

                    paintCommon.reset();
                    paintCommon.setAntiAlias(true);// 抗锯齿
                    paintCommon.setTextSize(resources.getDimension(R.dimen.pile_no_textsize));
                    paintCommon.setColor(Color.parseColor("#4FA975"));
                    canvas.drawRect(gridX + x_space * i + start_space,//
                            gridY + gridMiddlePosition,//
                            gridX + x_space * i + start_space + resources.getDimension(R.dimen.histogram_width),//
                            gridY + gridMiddlePosition + rightEnd * y_single_height, paintCommon);

                    // 绘制右方向的数值
                    if (rightSize > 0) {
                        paintCommon.reset();
                        paintCommon.setAntiAlias(true);// 抗锯齿
                        paintCommon.setTextSize(resources.getDimension(R.dimen.left_right_text_size));
                        paintCommon.setTextAlign(Align.CENTER);
                        paintCommon.setColor(Color.parseColor("#4FA975"));
                        canvas.drawText(String.valueOf(rightSize), //
                                gridX + x_space / 2 + x_space * i, //
                                gridY + gridMiddlePosition + rightEnd * y_single_height + resources.getDimension(R.dimen.right_count_y_offset), //
                                paintCommon);
                    }

                }

                if (drawYDegree) {
                    histogram_text_whole_ll.setVisibility(View.GONE);
                    paintCommon.reset();
                    paintCommon.setAntiAlias(true);// 抗锯齿
                    paintCommon.setColor(Color.parseColor("#E4E4E4"));
                    paintCommon.setStrokeWidth(resources.getDimension(R.dimen.accidentXYLineWidth));
                    canvas.drawLine(gridX - resources.getDimension(R.dimen.line_space), //
                            gridY, //
                            gridX - resources.getDimension(R.dimen.line_space), //
                            gridY + y_space * (lineCount - 1) + 2 * resources.getDimension(R.dimen.y_padding), //
                            paintCommon);// 最左方的竖线
                    // 绘制Y轴刻度
                    paintCommon.reset();
                    paintCommon.setAntiAlias(true);// 抗锯齿
                    paintCommon.setColor(Color.parseColor("#D5D5D5"));
                    paintCommon.setTextSize(resources.getDimension(R.dimen.pile_no_textsize));
                    paintCommon.setTextAlign(Align.RIGHT);
                    // 绘制Y轴刻度
                    for (int i = 0; i < lineCount; i++) {

                        int acc_index;

                        if (i < 6) {
                            acc_index = 5 - i;
                        } else {
                            acc_index = i - 5;
                        }

                        canvas.drawText(String.valueOf(accident_count[acc_index]), gridX - resources.getDimension(R.dimen.accidentYTextXOffset), gridY + y_space * i + resources.getDimension(R.dimen.accidentYTextYOffset) + resources.getDimension(R.dimen.y_padding), paintCommon);

                    }

                } else {
                    histogram_text_whole_ll.setVisibility(View.VISIBLE);

                    LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, (int) (y_space * (lineCount - 1) + 2 * resources.getDimension(R.dimen.y_padding)));
                    histogram_text_ll.setLayoutParams(lp);

                    LayoutParams lp_text_legend = new LayoutParams(LayoutParams.WRAP_CONTENT, (int) (y_space));
                    lp_text_legend.gravity = Gravity.RIGHT;

                    LayoutParams lp_text_legend0 = new LayoutParams(LayoutParams.WRAP_CONTENT, (int) (resources.getDimension(R.dimen.y_padding) + 2 * resources.getDimension(R.dimen.line_space)));
                    lp_text_legend0.gravity = Gravity.RIGHT;

                    histogram_text_ll.removeAllViews();

                    // 绘制Y轴刻度
                    for (int i = 0; i < lineCount; i++) {
                        int acc_index;

                        if (i < 6) {
                            acc_index = 5 - i;
                        } else {
                            acc_index = i - 5;
                        }

                        TextView tv = new TextView(this.getContext());
                        tv.setText(String.valueOf(accident_count[acc_index]));
                        tv.setTextColor(Color.parseColor("#D5D5D5"));
                        tv.setTextSize(resources.getDimension(R.dimen.time_accident_y_textsize));
                        tv.setSingleLine(true);
                        tv.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
                        if (i == 0) {
                            tv.setLayoutParams(lp_text_legend0);
                        } else {
                            tv.setLayoutParams(lp_text_legend);
                        }

                        histogram_text_ll.addView(tv);

                    }

                }

            }
        }
        isRefresh = false;
    }
}
