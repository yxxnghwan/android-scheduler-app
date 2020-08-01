package com.example.scheduler;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateLongClickListener;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    MaterialCalendarView cal;
    MyDBHelper dbHelper;
    SQLiteDatabase sqlDB;
    Cursor cur;
    RecyclerView scheduleList;
    RecyclerAdapter adapter;
    Data data;
    CircleImageView plusBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cal = findViewById(R.id.calendarView);

        scheduleList = findViewById(R.id.scheduleList);
        plusBtn = findViewById(R.id.plusBtn);

        dbHelper = new MyDBHelper(this);
        sqlDB = dbHelper.getReadableDatabase();

        cur = sqlDB.rawQuery("select * from tbl_scheduler;", null);

        cal.addDecorators(new SundayDecorator(), new SaturdayDecorator(), new DefaultDecorator()); // 데코레이터 장착!
        while(cur.moveToNext()) {
            String [] eventDateInfo = cur.getString(cur.getColumnIndex("date")).split("-");
            int eventYear = Integer.parseInt(eventDateInfo[0]);
            int eventMonth = Integer.parseInt(eventDateInfo[1]);
            int eventDay = Integer.parseInt(eventDateInfo[2]);
            cal.addDecorator(new EventDecorator(eventYear, eventMonth, eventDay));
        }
        sqlDB.close();
        dbHelper.close();
        cur.close();

        // 날짜를 클릭하면 해당 날짜에대한 스케줄들이 간략하게 하단에 표시됨
        cal.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int selectedYear = date.getYear();
                int selectedMonth = date.getMonth()+1;
                int selectedDay = date.getDay();
                String seletedDate = selectedYear + "-" + selectedMonth + "-" + selectedDay;
                dbHelper = new MyDBHelper(MainActivity.this);
                sqlDB = dbHelper.getReadableDatabase();
                cur = sqlDB.rawQuery("select * from tbl_scheduler where date='" + seletedDate + "';", null);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                scheduleList.setLayoutManager(linearLayoutManager);

                adapter = new RecyclerAdapter();
                scheduleList.setAdapter(adapter);


                while(cur.moveToNext()) {
                    data = new Data();

                    data.setId(cur.getInt(cur.getColumnIndex("id")));
                    data.setDate(cur.getString(cur.getColumnIndex("date")));
                    data.setStartTime(cur.getString(cur.getColumnIndex("startTime")));
                    data.setEndTime(cur.getString(cur.getColumnIndex("endTime")));
                    data.setSchedule(cur.getString(cur.getColumnIndex("schedule")));

                    adapter.addItem(data);
                }
                cur.close();
                dbHelper.close();
                sqlDB.close();
            }
        });



        plusBtn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();


                if(action==MotionEvent.ACTION_DOWN) {
                    plusBtn.setImageResource(R.drawable.plus_btn_pressed);
                }
                else if(action==MotionEvent.ACTION_UP){
                    plusBtn.setImageResource(R.drawable.plus_btn);
                    int selectedYear = cal.getSelectedDate().getYear();
                    int selectedMonth = cal.getSelectedDate().getMonth()+1;  // 0~11월로 찍힘
                    int selectedDay = cal.getSelectedDate().getDay();

                    Intent intent = new Intent(MainActivity.this, AddScheduleActivity.class);
                    intent.putExtra("year", selectedYear);
                    intent.putExtra("month", selectedMonth);
                    intent.putExtra("day", selectedDay);
                    finish();
                    startActivity(intent);
                }

                return false;
            }


        });
        // 켜지자 마자 오늘 리스트 불러오기
        todaySelected();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void todaySelected() {
        // 켜지자 마자 오늘 리스트 불러오기
        dbHelper = new MyDBHelper(MainActivity.this);
        sqlDB = dbHelper.getReadableDatabase();
        int selectedYear = CalendarDay.today().getYear();
        int selectedMonth = CalendarDay.today().getMonth()+1;
        int selectedDay = CalendarDay.today().getDay();
        String seletedDate = selectedYear+"-"+selectedMonth+"-"+selectedDay;
        cur = sqlDB.rawQuery("select * from tbl_scheduler where date='" + seletedDate + "';", null);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        scheduleList.setLayoutManager(linearLayoutManager);

        adapter = new RecyclerAdapter();
        scheduleList.setAdapter(adapter);


        while(cur.moveToNext()) {
            data = new Data();

            data.setId(cur.getInt(cur.getColumnIndex("id")));
            data.setDate(cur.getString(cur.getColumnIndex("date")));
            data.setStartTime(cur.getString(cur.getColumnIndex("startTime")));
            data.setEndTime(cur.getString(cur.getColumnIndex("endTime")));
            data.setSchedule(cur.getString(cur.getColumnIndex("schedule")));

            adapter.addItem(data);
        }
        cal.setSelectedDate(CalendarDay.today());
        cur.close();
        sqlDB.close();
        dbHelper.close();
    }

    public class SundayDecorator implements DayViewDecorator {

        private final Calendar calendar = Calendar.getInstance();

        public SundayDecorator() {}

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            return weekDay == Calendar.SUNDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.RED));
        }
    }

    public class SaturdayDecorator implements DayViewDecorator {

        private final Calendar calendar = Calendar.getInstance();

        public SaturdayDecorator() {}

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            return weekDay == Calendar.SATURDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.BLUE));
        }
    }

    public class DefaultDecorator implements DayViewDecorator {

        private final Calendar calendar = Calendar.getInstance();

        public DefaultDecorator() {}

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            return weekDay == Calendar.MONDAY || weekDay == Calendar.TUESDAY || weekDay == Calendar.WEDNESDAY ||
                    weekDay == Calendar.THURSDAY || weekDay == Calendar.FRIDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.WHITE));
        }
    }

    public class EventDecorator implements DayViewDecorator {

        private final Calendar calendar = Calendar.getInstance();
        int decoratedYear;
        int decoratedMonth;
        int decoratedDay;

        public EventDecorator(int year, int month, int day) {
            decoratedYear = year;
            decoratedMonth = month;
            decoratedDay = day;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);

            return decoratedYear == day.getYear() && decoratedMonth == day.getMonth()+1 && decoratedDay == day.getDay();
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(7, Color.rgb(0,192,192)));
        }
    }
}
