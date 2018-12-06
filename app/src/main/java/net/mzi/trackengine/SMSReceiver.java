package net.mzi.trackengine;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import net.mzi.trackengine.model.TicketInfoClass;

import java.util.List;

public class SMSReceiver extends BroadcastReceiver {
    private static SmsListner mListener;
    String messageBody;
    String sender;
    SQLiteDatabase sql;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            sql = context.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("SMSTRACK", "onReceive called receiver");
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            sender = smsMessage.getDisplayOriginatingAddress();
            // b=sender.endsWith("WNRCRP");  //Just to fetch otp sent from WNRCRP
            messageBody = messageBody + smsMessage.getMessageBody();
            messageBody = messageBody.replace("null", "");
        }
        if (true) {
            try {
                TicketInfoClass t = new TicketInfoClass();
                String ticket_id = messageBody.substring(0, messageBody.indexOf("|"));
                t.setIssueID(ticket_id);

                String rem1 = messageBody.substring(messageBody.indexOf("|") + 1);
                String ticket_num = rem1.substring(0, rem1.indexOf("|"));
                t.setTicketNumber(ticket_num);

                String rem2 = rem1.substring(rem1.indexOf("|") + 1);
                String creation_on = rem2.substring(0, rem2.indexOf("|"));
                t.setCreatedDate(creation_on);

                String rem3 = rem2.substring(rem2.indexOf("|") + 1);
                String status_id = rem3.substring(0, rem3.indexOf("|"));
                t.setStatusId(status_id);

                String rem4 = rem3.substring(rem3.indexOf("|") + 1);
                String prev_status_id = rem4.substring(0, rem4.indexOf("|"));
                t.setPreviousStatus(prev_status_id);

                String rem5 = rem4.substring(rem4.indexOf("|") + 1);
                String last_trans_mode = rem5.substring(0, rem5.indexOf("|"));
                t.setLastTransportMode(last_trans_mode);

                String rem6 = rem5.substring(rem5.indexOf("|") + 1);
                String assest_type = rem6.substring(0, rem6.indexOf("|"));
                t.setAssetType(assest_type);

                String rem7 = rem6.substring(rem6.indexOf("|") + 1);
                String assest_sn = rem7.substring(0, rem7.indexOf("|"));
                t.setAssetSerialNumber(assest_sn);

                String rem8 = rem7.substring(rem7.indexOf("|") + 1);
                String cat_name = rem8.substring(0, rem8.indexOf("|"));
                t.setCategoryName(cat_name);

                String rem9 = rem8.substring(rem8.indexOf("|") + 1);
                String corp_name = rem9.substring(0, rem9.indexOf("|"));
                t.setCorporateName(corp_name);

                String rem10 = rem9.substring(rem9.indexOf("|") + 1);
                String lat = rem10.substring(0, rem10.indexOf("|"));
                t.setLatitude(lat);

                String rem11 = rem10.substring(rem10.indexOf("|") + 1);
                String lng = rem11.substring(0, rem11.indexOf("|"));
                t.setLongitude(lng);

                String rem12 = rem11.substring(rem11.indexOf("|") + 1);
                String is_assest_varify = "Is Asset Verified" + " " + rem12.substring(0, rem12.indexOf("|"));
                t.setVerified(Boolean.parseBoolean(is_assest_varify));

                String impl_csat = "Implement CSAT" + " " + rem12.substring(rem12.indexOf("|") + 1);

                messageBody = ticket_id + "\n" + ticket_num + "\n" + creation_on + "\n" + status_id
                        + "\n" + prev_status_id + "\n" + last_trans_mode + "\n" + assest_type + "\n" + assest_sn + "\n" + cat_name
                        + "\n" + corp_name + "\n" + lat
                        + "\n" + lng + "\n" + is_assest_varify + "\n" + impl_csat;
                Log.e("SMSTRACK", messageBody);
//                mListener.messageReceived(t);

                {
                    Log.d("SMSTRACK", "message received");
                    List<TicketInfoClass> data = MyApp.getApplication().readMessage();

                    boolean isAvailable = false;
                    for (TicketInfoClass tt : data) {
                        if (tt.getIssueID().equals(t.getIssueID())) {
                            isAvailable = true;
                        }
                    }

                    Cursor cquery = sql.rawQuery("select * from Issue_Detail", null);
                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                        String issueId = cquery.getString(1);
                        if (issueId.equals(t.getIssueID())) {
                            isAvailable = true;
                        }
                        if (cquery.getString(12).equals("0")) ;
                        else {
                            //parentId=cquery.getString(13).toString();
                            if (cquery.getString(14).equals("-1")) {
                                // New
                            } else {
                                if (cquery.getString(14).equals("1")) {
                                    // accepted
                                } else if (cquery.getString(14).equals("2")) {
                                    // running
                                } else if (cquery.getString(14).equals("3")) {
                                    // closed
                                }
                            }
                        }
                    }

                    if (!isAvailable) {
                        data.add(t);
                        MyApp.getApplication().writeMessage(data);
                        try {
                            sql.execSQL("INSERT INTO Issue_Detail(IssueId ,CategoryName,Subject,IssueText,ServiceItemNumber,AssetSerialNumber,CreatedDate,SLADate,CorporateName,Address,Latitude,Longitude,PhoneNo,IsAccepted,StatusId,AssetType,AssetSubType,UpdatedDate,TicketHolder,TicketNumber,IsVerified,OEMNumber,AssetDetail,ContractSubTypeName,ContractName,PreviousStatus)VALUES" +
                                    "('" + t.getIssueID() + "','" +
                                    t.getCategoryName() + "','" +
                                    "" + "','" +
                                    "" + "','" +
                                    "" + "','" +
                                    t.getAssetSerialNumber() + "','" +
                                    t.getCreatedDate() + "','" +
                                    t.getCreatedDate() + "','" +
                                    "" + "','" +
                                    t.getCorporateName() + "','" +
                                    t.getLatitude() + "','" +
                                    t.getLongitude() + "','" +
                                    "" + "','-1','" +
                                    t.getStatusId() + "','" +
                                    t.getAssetType() + "','" +
                                    "" + "','" +
                                    "" + "','" +
                                    "" + "','" +
                                    t.getTicketNumber() + "','" +
                                    t.isVerified() + "','" +
                                    "" + "','" +
                                    "" + "','" +
                                    "" + "','" +
                                    "" + "','" +
                                    t.getPreviousStatus() + "')");
//                                                sendNotification("New Ticket: " + t.TicketNumber, ctx, t.TicketNumber);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.d("SMSTRACK", "Notification sent");
//                        Firstfrag f = new Firstfrag();
                        sendNotification("New Ticket: " + t.getTicketNumber(), context,
                                t.getTicketNumber());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendNotification(String sNotificationMessage, Context c, String ticketNumber) {
        String str = ticketNumber;
        str = str.replaceAll("[^\\d.]", "");
        int ticket = Integer.parseInt(str);
        Intent intent = new Intent(c, MainActivity.class);
        intent.putExtra("refresh", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        PendingIntent pIntent = PendingIntent.getActivity(c, (int) System.currentTimeMillis(), intent, 0);
        Notification noti = new Notification.Builder(c).setContentTitle("MZS Notifier")
                .setSmallIcon(R.mipmap.som)
                .setContentText(sNotificationMessage)
                .setContentIntent(pIntent)
                .setSound(Uri.parse("android.resource://" + "net.mzi.trackengine" + "/" + R.raw.message_tone))
                .addAction(R.drawable.som, "View", pIntent).build();
        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noti = new Notification.Builder(c).setContentTitle("MZS Notifier")
                    //.setContentText("New Updates in your Task Manager for "+ sIssueId)
                    .setSmallIcon(R.mipmap.som)
                    .setContentText(sNotificationMessage)
                    .setContentIntent(pIntent)
                    .setChannelId(ticket + "")
                    .setSound(Uri.parse("android.resource://" + "net.mzi.trackengine" + "/" + R.raw.message_tone))
                    .addAction(R.drawable.som, "View", pIntent).build();
            CharSequence name = c.getString(R.string.app_name);
            NotificationChannel mChannel = new NotificationChannel(ticket + "", name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(ticket, noti);
        Log.e("Notification", "Notification shown");
        //
        ++MULTIPLE_NOTIFICATION;
    }

    int MULTIPLE_NOTIFICATION = 0;

    public static void bindListener(SmsListner listener) {
        mListener = listener;

    }


}
