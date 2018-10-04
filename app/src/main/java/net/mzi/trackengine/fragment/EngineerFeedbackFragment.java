package net.mzi.trackengine.fragment;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RatingBar;

import com.google.gson.Gson;

import net.mzi.trackengine.MainActivity;
import net.mzi.trackengine.MyApp;
import net.mzi.trackengine.R;
import net.mzi.trackengine.SchedulingAdapter;
import net.mzi.trackengine.model.PostUrl;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by Poonam on 7/25/2017.
 */

public class EngineerFeedbackFragment extends Fragment {
    Map<String, String> postCloserTktStatus = new HashMap<String, String>();
    Map<String, String> mapCustomerLinkInfo = new HashMap<String, String>();
    RadioButton rViaEmail, rRegNumber, rNewNumber;
    EditText eNewNumber;
    SharedPreferences pref;
    RatingBar rbRateCustomer;
    EditText etRatingComment, etOTP;
    Button bSendFeedBack;
    TextInputLayout tietNewNumber;
    SQLiteDatabase sql;
    EngineerFeedbackFragment ctx = this;
    String sLoginID, sIssueId, sCloserCommemt, sCurrentTime, sStatusId, sRating, sRatingComment, sOtp, sParentCompanyId, sCardType, sSentByType;
    FloatingActionButton fabFeedback;

    public EngineerFeedbackFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_engineer_feedback, container, false);
        pref = getActivity().getSharedPreferences("login", 0);
        sParentCompanyId = pref.getString("ParentCompanyId", "ParentCompanyId");
        sSentByType = pref.getString("UserType", "UserType");
        sLoginID = getArguments().getString("LoginID");
        sIssueId = getArguments().getString("IssueID");
        sCloserCommemt = getArguments().getString("Comment");
        sCurrentTime = getArguments().getString("currentTime");
        sStatusId = getArguments().getString("StatusId");
        sCardType = getArguments().getString("CardType");
        rbRateCustomer = (RatingBar) view.findViewById(R.id.ratingBar);
        etRatingComment = (EditText) view.findViewById(R.id.feedback);
        etOTP = (EditText) view.findViewById(R.id.otp);
        fabFeedback = (FloatingActionButton) view.findViewById(R.id.fabfeedback);
        bSendFeedBack = (Button) view.findViewById(R.id.sendfb);
        fabFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.customer_info_card);
                dialog.setTitle("Resend link to customer ");
                tietNewNumber = (TextInputLayout) dialog.findViewById(R.id.tiNumber);
                rViaEmail = (RadioButton) dialog.findViewById(R.id.vEmail);
                rRegNumber = (RadioButton) dialog.findViewById(R.id.regNumber);
                rNewNumber = (RadioButton) dialog.findViewById(R.id.newNumber);
                eNewNumber = (EditText) dialog.findViewById(R.id.edieNewNumber);
                // set the custom dialog components - text, image and button
                rViaEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mapCustomerLinkInfo.put("IssueId", sIssueId);
                        mapCustomerLinkInfo.put("ParentCompanyId", sParentCompanyId);
                        mapCustomerLinkInfo.put("SentBy", sLoginID);
                        mapCustomerLinkInfo.put("SentByType", sSentByType);
                        String sendLink = new Gson().toJson(mapCustomerLinkInfo);
                        new resentCustomerLink(sendLink).execute();
                        dialog.dismiss();
                    }
                });
                rRegNumber.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            rNewNumber.setChecked(false);
                            MyApp.showMassage(getContext(),"Coming Soon!!!");
                            dialog.dismiss();
                        }
                    }
                });
                rNewNumber.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            tietNewNumber.setVisibility(View.VISIBLE);
                            rRegNumber.setChecked(false);
                            //dialog.dismiss();
                        }
                    }
                });
                eNewNumber.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        final int DRAWABLE_LEFT = 0;
                        final int DRAWABLE_TOP = 1;
                        final int DRAWABLE_RIGHT = 2;
                        final int DRAWABLE_BOTTOM = 3;

                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (event.getRawX() >= (eNewNumber.getRight() - eNewNumber.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                // your action here
                                if (rNewNumber.getText().toString().equals("")) {
                                    rNewNumber.setError("Enter Mobile Number");
                                } else if (eNewNumber.getText().length() == 10) {
                                    MyApp.showMassage(getContext(),"Coming Soon!!!");
                                    dialog.dismiss();

                                } else {
                                    rNewNumber.setError("Enter valid Mobile Number");
                                }
                                return true;
                            }
                        }
                        return false;
                    }
                });

                dialog.show();
            }
        });
        bSendFeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etOTP.getText().length() > 5) {
                    sRating = String.valueOf(rbRateCustomer.getRating());
                    sRatingComment = etRatingComment.getText().toString();
                    sOtp = etOTP.getText().toString();
                    postCloserTktStatus.put("UserId", sLoginID);
                    postCloserTktStatus.put("IssueId", sIssueId);
                    postCloserTktStatus.put("Rate", sRating.substring(0, 1));
                    postCloserTktStatus.put("RatingComment", sRatingComment);
                    postCloserTktStatus.put("OTP", sOtp);
                    postCloserTktStatus.put("ParentCompanyId", sParentCompanyId);
                    postCloserTktStatus.put("IssueStatus", sStatusId);
                    postCloserTktStatus.put("CloserComment", sCloserCommemt);
                    postCloserTktStatus.put("CloserDate", sCurrentTime);
                    String closerTkt = new Gson().toJson(postCloserTktStatus);
                    //API_URL="http://sparkonix.in/TrackEngine/api/Post/PostTicketStatus?UserId="+MainActivity.LOGINID+"&TicketId="+data.get(position).IssueID+"&StatusId=3&Comment="+commemt.getText().toString()+"&ActivityDate="+currentDateTimeString;
                    new UpdateCloserTask(closerTkt).execute();
                    //getActivity().getSupportFragmentManager().beginTransaction().remove(ctx).commit();
                    //s.removeCard(Integer.parseInt(sPosition));
                } else {
                    etOTP.setError("Please enter valid OTP");
                }

            }
        });
        return view;
    }

    private class UpdateCloserTask extends AsyncTask<String, Void, String> {
        String closeTkt;

        public UpdateCloserTask(String closerTkt) {
            this.closeTkt = closerTkt;
            Log.e("TAG", "UpdateCloserTask:" + closerTkt);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String uri = PostUrl.sUrl + "SaveUserFeedback";
            String result = "";
            try {
                //Connect
                HttpURLConnection urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();

                //Write
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                //Call parserUsuarioJson() inside write(),Make sure it is returning proper json string .
                writer.write(closeTkt);
                writer.close();
                outputStream.close();

                //Read
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();
                result = sb.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("onPostExecute: ", s);
            String sStatus = "false";
            if (s == null) {
                //s = "THERE WAS AN ERROR";
            } else {
                try {
                    String msg = null;

                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONObject jdata = jsonObject.getJSONObject("ClosedActionStatus");
                    //for (int i = 0; i < jdata.length(); i++) {
                    msg = jdata.getString("Message");
                    sStatus = jdata.getString("Status");
                    //}
                    if (sStatus != null) {
                        if (sStatus.equals("true")) {
                            MyApp.showMassage(getContext(),"Updated Successfully");
                            sql = getContext().openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
                            ContentValues newValues = new ContentValues();
                            newValues.put("StatusId", sStatusId);
                            newValues.put("IsAccepted", "3");
                            newValues.put("UpdatedDate", sCurrentTime);
                            sql.update("Issue_Detail", newValues, "IssueId=" + sIssueId, null);
                            MainActivity m = new MainActivity();
                            m.updateCounter(getContext());
                            MyApp.showMassage(getActivity(),"Status Changed Successfully");
                            SchedulingAdapter so = new SchedulingAdapter();
                            Intent i = getActivity().getIntent();
                            getActivity().finish();
                            i.putExtra("cardpos", sCardType);
                            startActivity(i);
                        } else {
                            MyApp.showMassage(getContext(),msg);
                        }
                    }
                    /*final Snackbar snackBar = Snackbar.make(v, msg, Snackbar.LENGTH_INDEFINITE);
                    snackBar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackBar.dismiss();

                        }
                    }).show();*/
                    Log.e("onPostExecute: ", msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private class resentCustomerLink extends AsyncTask<String, Void, String> {
        String sendLink;

        public resentCustomerLink(String sendLink) {
            this.sendLink = sendLink;
            Log.e("sendLink", "resentCustomerLink: " + sendLink);
        }

        @Override
        protected String doInBackground(String... params) {
            String uri = PostUrl.sUrl + "ResendCustomerLink";
            String result = "";
            try {
                //Connect
                HttpURLConnection urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();

                //Write
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                //Call parserUsuarioJson() inside write(),Make sure it is returning proper json string .
                writer.write(sendLink);
                writer.close();
                outputStream.close();

                //Read
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();
                result = sb.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MyApp.showMassage(getContext(),"Link Sent on registered mail id!!!");
            Log.e("sendlink", "onPostExecute: " + s);
        }
    }
}
