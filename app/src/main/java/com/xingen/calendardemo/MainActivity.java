package com.xingen.calendardemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by ${xingen} on 2017/9/19.
 * blog:http://blog.csdn.net/hexingen
 */
public class MainActivity extends AppCompatActivity {
   private final  String tag=MainActivity.class.getSimpleName();
    private DateTableView dateTableView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.dateTableView=(DateTableView) findViewById(R.id.date_table_view);

        this.dateTableView.addData(getCurrentTime());
    }
    private String getCurrentTime(){
        Calendar calendar=Calendar.getInstance(Locale.getDefault());
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(calendar.get(Calendar.YEAR));
        stringBuilder.append("-");
        int month=calendar.get(Calendar.DAY_OF_MONTH);
        stringBuilder.append(month+1>9?month+1:"0"+(month+1));
        return stringBuilder.toString();
    }
}
