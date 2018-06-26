package net.mzi.trackengine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.mzi.trackengine.model.PostUrl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

public class TicketInfo extends AppCompatActivity {
    long totalSize = 0;
    // Camera activity request codes
    public static final int MULTIPLE_PERMISSIONS = 1;
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public Uri fileUri;
    LinearLayout lyServiceItem,lyAssetSerial;
    String selectedPath;
    static String sFinalImagePath;
    TextView tCname,tMNumber,tTIssue,tCAdrs,tTStatus,tExpectedTime,tCreatedDate,tUpdatedDate,tCAssetName,tCAssetType,tCAssetSubtype,tCAssetSerialNumber,tCorporateName,tidOEMInfo,tAssetDescription,tServiceType,tServiceSubType,tIssueText;
    RadioButton vCamera,vGallery;
    EditText comment;
    Map<String,String> postTktStatus=new HashMap<String, String>();
    Button Submit;
    String mcardType;
    ImageView iImageIcon;
    SQLiteDatabase sql;
    Cursor cquery;
    String ID;
    AppBarLayout app;
    String cname,mNumber,tIssue,cAdrs,tStatus,expectedTime,createdDate,updatedDate,cAssetName,cAssetType,cAssetSubtype,cAssetSerialNumber,corporateName,sidOEMInfo,sAssetDesc,sServiceType,sServiceSubtype,sIssueText;
    public int iFlag;
    SharedPreferences pref;
    private String nh_userid,sStatusId;
    String sParentComapnyId,DepartmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getSharedPreferences("login", 0);
        nh_userid=pref.getString("userid","userid");
        sParentComapnyId = pref.getString("ParentCompanyId", "ParentCompanyId");
        DepartmentId = pref.getString("DepartmentId", "DepartmentId");

        setContentView(R.layout.activity_ticket_info);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        app=(AppBarLayout)findViewById(R.id.app_bar);
        iImageIcon=(ImageView)findViewById(R.id.imageuplaodicon);
        tCname=(TextView)findViewById(R.id.contactpersonname);
        tCAdrs=(TextView)findViewById(R.id.adrs);
        tMNumber=(TextView)findViewById(R.id.cntctprsnmob);
        tExpectedTime=(TextView)findViewById(R.id.beontime);
        tTIssue=(TextView)findViewById(R.id.subject);
        tTStatus=(TextView)findViewById(R.id.status);
        tCreatedDate=(TextView)findViewById(R.id.createddate);
        tUpdatedDate=(TextView)findViewById(R.id.updateddate);
        tCAssetName=(TextView)findViewById(R.id.assetName);
        tCAssetType=(TextView)findViewById(R.id.assetType);
        tCAssetSubtype=(TextView)findViewById(R.id.assetSubType);
        tCAssetSerialNumber=(TextView)findViewById(R.id.assetserialNumber);
        lyAssetSerial=(LinearLayout)findViewById(R.id.idSRnumberLayout);
        lyServiceItem=(LinearLayout)findViewById(R.id.idSIlayout);
        tCorporateName=(TextView)findViewById(R.id.corporateName);
        tidOEMInfo=(TextView)findViewById(R.id.idOEMInfo);
        vCamera=(RadioButton)findViewById(R.id.vCam);
        vGallery=(RadioButton)findViewById(R.id.vGal);
        tAssetDescription=(TextView)findViewById(R.id.idAssetDescription);
        tServiceType=(TextView)findViewById(R.id.idServiceType);
        tServiceSubType=(TextView)findViewById(R.id.idServiceSubType);
        tIssueText=(TextView)findViewById(R.id.issueText);

        //this.upload=(ImageView)itemView.findViewById(R.id.uploadImage);
        comment=(EditText)findViewById(R.id.agentComment);
        Submit=(Button)findViewById(R.id.submit);
        sql = openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE,null);
        Bundle bundle = getIntent().getExtras();
        mcardType=bundle.getString("CardType");
        if(mcardType.equals("-1")){
            app.setBackgroundResource(R.color.purple);
        }else if(mcardType.equals("1")){
            app.setBackgroundResource(R.color.orange);
        }else if(mcardType.equals("2")){
            app.setBackgroundResource(R.color.blue);
        }else if(mcardType.equals("3")){
            app.setBackgroundResource(R.color.green);
        }
        ID=bundle.getString("IssueId");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Id: "+ID);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        cquery = sql.rawQuery("select * from Issue_Detail where IssueId = '"+bundle.getString("IssueId")+"'", null);
        cquery.moveToFirst();
        toolbar.setTitle("Id: "+cquery.getString(20).toString());
        if(cquery.getString(21).toString().equals("1")){
            lyServiceItem.setVisibility(View.VISIBLE);
            lyAssetSerial.setVisibility(View.VISIBLE);
        }
        sStatusId=cquery.getString(15).toString();
        Cursor cqueryForStatus=sql.rawQuery("select StatusName from Issue_Status where StatusId = '"+cquery.getString(15).toString()+"'", null);
        if(cqueryForStatus.getCount()>0) {
            cqueryForStatus.moveToFirst();
            tStatus = cqueryForStatus.getString(0).toString();
        }
        else
            tStatus = "NA";
        cname=cquery.getString(19).toString();
        mNumber=cquery.getString(13).toString();
        tIssue=cquery.getString(3).toString();
        cAdrs=cquery.getString(10).toString();
        expectedTime=cquery.getString(8);
        createdDate=cquery.getString(7).toString();
        updatedDate=cquery.getString(18).toString();
        cAssetName=cquery.getString(5).toString();
        cAssetType=cquery.getString(16).toString();
        cAssetSubtype=cquery.getString(17).toString();
        cAssetSerialNumber=cquery.getString(6).toString();
        corporateName=cquery.getString(9).toString();
        sidOEMInfo=cquery.getString(22).toString();
        sAssetDesc=cquery.getString(23).toString();
        sServiceSubtype=cquery.getString(24).toString();
        sServiceType=cquery.getString(25).toString();
        sIssueText=cquery.getString(4).toString();

        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TicketInfo.this,FollowUpHistory.class);
                i.putExtra("ID",ID);
                i.putExtra("TNumber",cquery.getString(20).toString());
                startActivity(i);
            }
        });
        tCname.setText(cname);
        tMNumber.setText(mNumber);
        tTIssue.setText(tIssue);
        tCAdrs.setText(cAdrs);
        tAssetDescription.setText(sAssetDesc);
        tTStatus.setText(tStatus);
        tIssueText.setText(sIssueText);
        //tExpectedTime.setText(expectedTime);

        if(expectedTime.equals(""))
            tExpectedTime.setText("NA!!");
        else if(mcardType.equals("3")){
            tExpectedTime.setText("--");
        }
        else {
            Date dSLAdate = null, dCurrentDate = null;
            String dtStart = expectedTime;
            Log.e("onBindViewHolder: ", dtStart);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            try {
                dSLAdate = format.parse(dtStart);
                System.out.println(dSLAdate);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Date cDate = new Date();
            String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(cDate);
            try {
                dCurrentDate = format.parse(currentDateTimeString);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            SchedulingAdapter saTemp= new SchedulingAdapter();
            long l = saTemp.printDifference(dSLAdate, dCurrentDate);
            final long secondsInMilli = 1000;
            final long minutesInMilli = secondsInMilli * 60;
            final long hoursInMilli = minutesInMilli * 60;
            new CountDownTimer(l, 1000) {
                public void onTick(long millisUntilFinished) {
                    long elapsedHours = millisUntilFinished / hoursInMilli;
                    millisUntilFinished = millisUntilFinished % hoursInMilli;

                    long elapsedMinutes = millisUntilFinished / minutesInMilli;
                    millisUntilFinished = millisUntilFinished % minutesInMilli;

                    long elapsedSeconds = millisUntilFinished / secondsInMilli;
                    tExpectedTime.setText(elapsedHours + "h " + elapsedMinutes + "m " + elapsedSeconds + "s ");
                    //here you can have your logic to set text to edittext
                }

                public void onFinish() {
                    tExpectedTime.setTextColor(Color.RED);
                    tExpectedTime.setText("SLA Breached!!");
                }

            }.start();
        }
        tCreatedDate.setText(createdDate);
        tUpdatedDate.setText(updatedDate);
        tCAssetName.setText(cAssetName);
        tCAssetType.setText(cAssetType);
        tCAssetSubtype.setText(cAssetSubtype);
        tCAssetSerialNumber.setText(cAssetSerialNumber);
        tCorporateName.setText(corporateName);
        tidOEMInfo.setText(sidOEMInfo);
        tServiceType.setText(sServiceType);
        tServiceSubType.setText(sServiceSubtype);

        vCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    call_permissions();
                    vGallery.setChecked(false);
                    captureImage();
                }
            }
        });
        vGallery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    call_permissions();
                    vCamera.setChecked(false);
                    openGallery(200);
                }
            }
        });
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sFinalImagePath==null){
                String currentTime;
                Date cDate = new Date();
                currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                ContentValues newValues = new ContentValues();
                newValues.put("UpdatedDate",currentTime);
                sql.update("Issue_Detail", newValues,"IssueId="+ID, null);

                postTktStatus.put("UserId", nh_userid);
                postTktStatus.put("ParentCompanyId", sParentComapnyId);
                postTktStatus.put("TicketId",ID);
                postTktStatus.put("StatusId", sStatusId);//cquery.getString(0).toString();
                postTktStatus.put("Comment", comment.getText().toString());
                postTktStatus.put("ActivityDate", currentTime);
                postTktStatus.put("DepartmentId", DepartmentId);
                postTktStatus.put("RealtimeUpdate", "true");

                String tktStatus = new Gson().toJson(postTktStatus);
                new UpdateTask(TicketInfo.this,tktStatus).execute();
                }
                else {
                    new UploadFileToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            }
        });
    }
    public class UpdateTask extends AsyncTask<String, Void, String> {
        String jsonString;
        SQLiteDatabase sql;
        public UpdateTask(Context ctx,String jsonString) {
            this.jsonString=jsonString;
            sql = ctx.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE,null);
            Log.e("onPostExecute: ",jsonString );

        }
        @Override
        protected String doInBackground(String... params) {
            String uri = PostUrl.sUrl+"PostTicketStatus";
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
            }catch (RejectedExecutionException e){
                e.printStackTrace();
            }
            return result;
        }
        @Override
        protected void onPostExecute(String s) {
            JSONObject jsonObject=null;
            Date cDate = new Date();
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
            super.onPostExecute(s);
            try {
                jsonObject = new JSONObject(jsonString);
                if (s == null || s.equals("0\n")||s.equals("")) {
                    sql.execSQL("INSERT INTO Issue_History(IssueId,UserId,IssueStatus,Comment,CreatedDate,SyncStatus)VALUES"+
                            "('" + jsonObject.getString("TicketId") + "','" + jsonObject.getString("UserId")+ "','" + jsonObject.getString("StatusId") + "','" + jsonObject.getString("Comment")+ "','" + jsonObject.getString("ActivityDate") + "','0')");
                }
                else {
                    Toast.makeText(getApplicationContext()," Comment Updated!!!",Toast.LENGTH_LONG).show();
                    comment.setText("");
                    tUpdatedDate.setText(currentTime);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("Post Login",s);
        }
    }
    public void openGallery(int req_code) {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select file to upload "), req_code);
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
    }
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    private void captureImage() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // start the image capture Intent
                startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    1);
        }

    }
    public Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }
    private static File getOutputMediaFile(int type) {
        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "SOM Track Engine");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("TAG", "Oops! Failed create "
                        + "Android File Upload" + " directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
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
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // launching upload activity
                launchUploadActivity(fileUri.getPath());
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else {
            Uri selectedImageUri = data.getData();
            if (requestCode == 200) {
                try {
                    selectedPath = getPath(getApplicationContext(),selectedImageUri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                launchUploadActivity(selectedPath);

                Log.e("selectedPath1 : ", selectedPath);
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
                //tv.setText("Selected File paths : " + selectedPath1 + "," + selectedPath2);
            }
        }

    }



    @SuppressLint("NewApi")
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /*public String getPath(Uri uri) {


        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;


        *//*Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getContentResolver().query(uri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }*//*

       *//* String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);*//*
    }*/
    private void launchUploadActivity(final String sImagePath){

        final AlertDialog.Builder Dialog = new AlertDialog.Builder(TicketInfo.this);
        Dialog.setTitle("Image Selector ");
        Dialog.setCancelable(false);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = li.inflate(R.layout.imagedialogue, null);
        final ImageView upImage =(ImageView) dialogView.findViewById(R.id.imagedia);
        Dialog.setView(dialogView);
        BitmapFactory.Options options = new BitmapFactory.Options();

        // down sizing image as it throws OutOfMemory Exception for larger
        // images
        options.inSampleSize = 8;

        final Bitmap bitmap = BitmapFactory.decodeFile(sImagePath, options);

        upImage.setImageBitmap(bitmap);
        Dialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        sFinalImagePath=sImagePath;
                        iImageIcon.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(),"Image uploaded successfully!!!",Toast.LENGTH_LONG).show();
                    }
                });

        Dialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        sFinalImagePath="";
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialog = Dialog.create();
        dialog.show();
        /*Intent i = new Intent(MainActivity.this, UploadActivity.class);
        i.putExtra("filePath", fileUri.getPath());
        i.putExtra("isImage", isImage);
        startActivity(i);*/
    }


    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        //Dialog progress;
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            //progressBar.setProgress(0);
            super.onPreExecute();
            if(getApplicationContext()!=null) {
                Toast.makeText(getApplicationContext(), "Loading data, Please wait...", Toast.LENGTH_LONG).show();
            }
            //progress = ProgressDialog.show(TicketInfo.this,"Loading data", "Please wait...");
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            /*progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");*/
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            //HttpPost httppost = new HttpPost("http://192.168.0.20/TrackEngine/api/Post/PostTicketAttachment");
            HttpPost httppost = new HttpPost(PostUrl.sUrl+"PostTicketAttachment");

            try {

                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(sFinalImagePath);
                String currentTime;
                Date cDate = new Date();
                currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                // Adding file data to http body
                entity.addPart("Files", new FileBody(sourceFile));
                // Extra parameters if you want to pass to server
                entity.addPart("AttachedBy",new StringBody(MainActivity.LOGINID));
                entity.addPart("TicketId",new StringBody(ID));
                entity.addPart("Comment",new StringBody(comment.getText().toString()));
                entity.addPart("ActivityDate",new StringBody(currentTime));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;
        }
        @Override
        protected void onPostExecute(String result) {
            Log.e("TAG", "Response from server: " + result);
            //progress.dismiss();
            // showing the server response in an alert dialog
            showAlert(result);
            sFinalImagePath=null;
            super.onPostExecute(result);
        }
    }
    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String msg;
        if(message.equals("")) {
            msg = "Data has been sent successfully!!!";
        }
        else
            msg="Something went wrong!!!\n"+message;
        builder.setMessage(msg).setTitle("Response from Server")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                        comment.setText("");
                        iImageIcon.setVisibility(View.GONE);
                        sFinalImagePath="";
                        vCamera.setChecked(false);
                        vGallery.setChecked(false);
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void call_permissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
        }
        return;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permissions granted.
                } else {
                    // no permissions granted.
                }
                return;
            }
        }

    }

}
