package com.stevesoft.smartfinances.ui;


import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.PercentFormatter;
import com.stevesoft.smartfinances.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment implements OnChartValueSelectedListener{

    private PieChart mChart;    // chart for showing expenses of current month by category
    private Float[] yData;      // used for values of each category
    private String[] xData;     // used for category names

    protected HorizontalBarChart mBarChart;     // chart for showing income/expenses
    //private SeekBar mSeekBarX, mSeekBarY;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        TextView txtNetWorth = (TextView) view.findViewById(R.id.textViewNetWorth);
        TextView txtThisMonth = (TextView) view.findViewById(R.id.textViewThisMonth);
        TextView txtThisMonthBalance = (TextView) view.findViewById(R.id.textViewThisMonthBalance);
        mChart = (PieChart) view.findViewById(R.id.chart);
        mBarChart = (HorizontalBarChart) view.findViewById(R.id.barChart);

        // Get current month balance
        Calendar cal= Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(cal.getTime());
        txtThisMonth.setText(month_name);   // set month name
        txtThisMonthBalance.setText(MainActivity.myDb.getThisMonthBalance() +""); // set month balance

        // display net worth to textview
        Cursor cur = MainActivity.myDb.getNetWorth();
        if (cur.moveToFirst()){
            do {
                txtNetWorth.setText(txtNetWorth.getText() +""+
                        cur.getDouble(cur.getColumnIndex("BALANCE")) +" "
                        + cur.getString(cur.getColumnIndex("CURRENCY"))
                        + "\n");
            } while (cur.moveToNext());
        }

        // get current month expenses by category from db
        ArrayList<String> categories = new ArrayList<String>();
        ArrayList<Float> amount = new ArrayList<Float>();
        Cursor c = MainActivity.myDb.getThisMonthExpenses();

        if (c.moveToFirst()){
            do{
                categories.add(c.getString(c.getColumnIndex("CATEGORY")));
                amount.add(c.getFloat(c.getColumnIndex("PRICE")));
            } while (c.moveToNext());
        }


//        c.moveToFirst();

//        while (!c.isAfterLast()){
//            // add cursor data to arraylists
//            categories.add(c.getString(c.getColumnIndex("CATEGORY")));
//            //Log.e("CATEGORY_added:", c.getString(c.getColumnIndex("CATEGORY")));
//            amount.add(c.getFloat(c.getColumnIndex("PRICE")));
//            //Log.e("PRICE_added:", ""+c.getFloat(c.getColumnIndex("PRICE")));
//            c.moveToNext();
//        }

        //convert arraylists to arrays
        xData = categories.toArray(new String[categories.size()]);
        yData = amount.toArray(new Float[amount.size()]);

        setUpPieGraph();
        setUpBarGraph();
        return view;
    }

    private void setUpPieGraph(){
        mChart.setUsePercentValues(true);
        mChart.setDescription("");
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColorTransparent(true);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);

        mChart.setCenterText("Expenses\nThis month");

        addData();

        mChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);

        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                // display message when value selected
                if (e == null)
                    return;

                Toast.makeText(getActivity(), xData[e.getXIndex()] + " = " + e.getVal() + "EUR",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    // adds data to Pie Chart
    private void addData(){
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        for (int i=0; i<yData.length; i++) {
            yVals1.add(new Entry(yData[i], i));
            Log.e("yDATA: ", yData[i].toString());
        }

        ArrayList<String> xVals = new ArrayList<String>();

        for (int i=0; i<xData.length; i++)
            xVals.add(xData[i]);

        // create pie data set
        PieDataSet dataset = new PieDataSet(yVals1, "Expenses");
        dataset.setSliceSpace(3);
        dataset.setSelectionShift(5);

        // add many colors
        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c: ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c: ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c: ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c: ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());
        dataset.setColors(colors);


        // instantiate pie data object
        PieData data = new PieData(xVals, dataset);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.GRAY);

        mChart.setData(data);

        // update pie chart
        mChart.invalidate();
    }

    private void setUpBarGraph(){
        //mBarChart.setOnChartValueSelectedListener(this);
        // mChart.setHighlightEnabled(false);

        mBarChart.setDrawBarShadow(false);

        mBarChart.setDrawValueAboveBar(false);

        mBarChart.setDescription("");

        // if more than 2 entries are displayed in the chart, no values will be
        // drawn
        mBarChart.setMaxVisibleValueCount(2);

        // scaling can now only be done on x- and y-axis separately
        mBarChart.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // mBarChart.setDrawBarShadow(true);

         //mBarChart.setDrawXLabels(false);

        mBarChart.setDrawGridBackground(false);

        // mBarChart.setDrawYLabels(false);

        //tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        XAxis xl = mBarChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xl.setTypeface(tf);
        xl.setDrawAxisLine(false);
        xl.setDrawGridLines(false);
        xl.setGridLineWidth(0.3f);

        YAxis yl = mBarChart.getAxisLeft();
        //yl.setTypeface(tf);
        yl.setDrawAxisLine(false);
        yl.setDrawGridLines(false);
        //yl.setDrawTopYLabelEntry(false);
        yl.setGridLineWidth(0.3f);

//        yl.setInverted(true);

        YAxis yr = mBarChart.getAxisRight();
        //yr.setTypeface(tf);
        yr.setDrawAxisLine(false);
        yr.setDrawTopYLabelEntry(false);
        yr.setDrawGridLines(false);
//        yr.setInverted(true);

        setBarChartData(2, 50);
        mBarChart.animateY(2500);

        // setting data
//        mSeekBarY.setProgress(50);
//        mSeekBarX.setProgress(12);

       // mSeekBarY.setOnSeekBarChangeListener(this);
       // mSeekBarX.setOnSeekBarChangeListener(this);

//        Legend l = mBarChart.getLegend();
//        l.setEnabled(false);
        mBarChart.getLegend().setEnabled(false);
    }

    private void setBarChartData(int count, float range){
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<String> xVals = new ArrayList<String>();

//        for (int i = 0; i < count; i++) {
//            xVals.add(mMonths[i % 12]);
//            yVals1.add(new BarEntry((float) (Math.random() * range), i));
//        }

        xVals.add(0, "Income");
        xVals.add(1, "Expenses");
        yVals1.add(new BarEntry(MainActivity.myDb.getThisMonthIncome(), 0));
        yVals1.add(new BarEntry(MainActivity.myDb.getThisMonthExpense(), 1));

        BarDataSet set1 = new BarDataSet(yVals1, "");

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10f);
        //data.setValueTypeface(tf);

        int[] colors = {getResources().getColor(R.color.green), getResources().getColor(R.color.red)};
        set1.setColors(colors);
        mBarChart.setData(data);

    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
