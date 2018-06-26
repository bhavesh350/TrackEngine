package net.mzi.trackengine;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

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

public class ChangePasswordActivity extends AppCompatActivity {
    EditText existingPwdEditText,newPwdEditText,confrimPwdEfitText;
    Button changePwdButton;
    String nh_userid;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Map<String,String> pswdInfo=new HashMap<String, String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        pref = getSharedPreferences("login", 0);
        nh_userid=pref.getString("userid","userid");
        existingPwdEditText=(EditText)findViewById(R.id.existPwdId);
        newPwdEditText=(EditText)findViewById(R.id.newPwdId);
        confrimPwdEfitText=(EditText)findViewById(R.id.confirmPwdId);
        changePwdButton=(Button)findViewById(R.id.changePwd);
        changePwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(existingPwdEditText.getText().toString().equals(newPwdEditText.getText().toString()))) {
                    if (newPwdEditText.getText().toString().equals(confrimPwdEfitText.getText().toString())) {
                        pswdInfo.put("UserId", nh_userid);
                        pswdInfo.put("OldPassword", existingPwdEditText.getText().toString());
                        pswdInfo.put("NewPassword", newPwdEditText.getText().toString());
                        String jsonString = new Gson().toJson(pswdInfo);
                        new ChangePasswordActivity.PaswdChangeOperation(jsonString, v).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        confrimPwdEfitText.setError("Password dont match");
                    }
                }
                else {
                    existingPwdEditText.setError("Both are same");
                }
            }
        });
    }


    public class PaswdChangeOperation extends AsyncTask<String, Void, String> {
        String jsonString;
        SQLiteDatabase sql;
        Context ctx;
        View v;
        public PaswdChangeOperation(String jsonString, View v) {
            this.jsonString=jsonString;
            this.v=v;
        }
        @Override
        protected String doInBackground(String... params) {
            String uri = PostUrl.sUrl+"ChangePassword";
            String result ="";
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
                writer.write(jsonString);
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
            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
        @Override
        protected void onPostExecute(String s) {
            Log.d("TAG", "onPostExecute: "+s);
            if (s == null) {
                Toast.makeText(getApplicationContext(),R.string.internet_error,Toast.LENGTH_LONG).show();
            }
            else {
                try {
                    String msg = null;
                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONObject object = jsonObject.getJSONObject("Result");
                    msg=object.getString("Message");
                    String status=object.getString("Status");
                    if(status.equals("true")){
                        editor = pref.edit();
                        editor.putString("password", newPwdEditText.getText().toString());
                    }
                    final Snackbar snackBar = Snackbar.make(v, msg, Snackbar.LENGTH_INDEFINITE);
                    snackBar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackBar.dismiss();
                        }
                    }).show();
                    Log.i("INFO", s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
