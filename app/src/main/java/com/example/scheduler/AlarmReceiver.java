package com.example.scheduler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_RESTART_SERVICE = "Restart";
    MyDBHelper dbHelper;
    SQLiteDatabase sqlDB;
    Cursor cur;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION_RESTART_SERVICE)) {
            int id = intent.getIntExtra("id", 0);   // db에 저장되는 id값으로 알람 id까지 지정할 것. 그래야 꼬일 일 없고 스케줄 삭제시 알람취소도 같이 하기 쉬움
            dbHelper = new MyDBHelper(context);
            sqlDB = dbHelper.getReadableDatabase();
            cur = sqlDB.rawQuery("select * from tbl_scheduler where id = " + id + ";", null);
            cur.moveToNext();

            String location = cur.getString(cur.getColumnIndex("location"));
            String schedule = cur.getString(cur.getColumnIndex("schedule"));
            String startTime = "";
            try {
                Date d = new SimpleDateFormat("hh:mm").parse(cur.getString(cur.getColumnIndex("startTime")));
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(d.getTime());
                startTime = (c.get(Calendar.AM_PM) == 0 ? "am" : "pm") + c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String contentText = schedule + " >> " + startTime + "에 " + location + "에서";

            //NotificationManager 안드로이드 상태바에 메세지를 던지기위한 서비스 불러오고
            NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // 오레오버전부터는 알람 채널이라는게 있어야한답니다.. 노티피케이션매니저에게 채널을 달아주어야함..
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                int importance = NotificationManager.IMPORTANCE_HIGH;
                // 이 채널이 다른 채널에서 오는 알람보다 위에 있게 만듬

                NotificationChannel mChannel = new NotificationChannel(
                        ScheduleAlarm.CHANNEL_ID, ScheduleAlarm.CHANNEL_NAME, importance);

                notificationmanager.createNotificationChannel(mChannel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ScheduleAlarm.CHANNEL_ID);
            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setSmallIcon(R.drawable.scheduler_alarm_icon).setWhen(System.currentTimeMillis())
                    .setNumber(1).setContentTitle(schedule).setContentText(contentText)
                    .setContentIntent(pendingIntent).setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL);


            notificationmanager.notify(id, builder.build());
        }
    }
}
