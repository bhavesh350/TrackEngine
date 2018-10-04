package net.mzi.trackengine;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


//{"CompanyId":2,"LocationId":2,
// "CategoryId":1,"Subject":"Test 22"
// ,"ComplainText":"Create Complain for test 22",
// "CreatedBy":5064,"CreationSource":"M"}
public class RaiseTicket extends AppCompatActivity {
    long totalSize = 0;
    String sTicketId;
    String sFinalImagePath_create = null;
    Spinner categoryNameSP;
    List<String> catName = new ArrayList<String>();
    List<String> catId = new ArrayList<String>();
    String API_URL;
    RadioButton vCamera, vGallery;
    Map<String, String> ticketCreationInfo = new HashMap<String, String>();
    SharedPreferences pref;
    ImageView iImageIcon;
    EditText eSubject, eDescription;
    Button submit;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public Uri fileUri;
    String selectedPath;
    String sUserId, companyID, sParentCompanyId, sCategoryId;
    public static final int MULTIPLE_PERMISSIONS = 1;
    String[] permissions = new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raise_ticket);
        pref = getSharedPreferences("login", 0);
        //editor = pref.edit();
        sUserId = pref.getString("userid", "userid");//CreatedBy
        companyID = pref.getString("CompanyId", "CompanyId");//locationId
        sParentCompanyId = pref.getString("ParentCompanyId", "ParentCompanyId");//companyId
        //DepartmentId=pref.getString("DepartmentId","DepartmentId");
        categoryNameSP = (Spinner) findViewById(R.id.categorySpinner);
        vCamera = (RadioButton) findViewById(R.id.vCam);
        iImageIcon = (ImageView) findViewById(R.id.imageuplaodicon);
        vGallery = (RadioButton) findViewById(R.id.vGal);
        submit = (Button) findViewById(R.id.submitButton);
        eSubject = (EditText) findViewById(R.id.subjectEditText);
        eDescription = (EditText) findViewById(R.id.descEditText);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //API_URL=PostUrl.sUrl+"GetCategoryForComplain";
        API_URL = PostUrl.sUrl + "GetCategoryForComplain";
        new fetchCategoryName().execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RaiseTicket.this, InternalIssueListing.class);
                startActivity(i);
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        categoryNameSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(RaiseTicket.this, "its working"+position, Toast.LENGTH_SHORT).show();
                sCategoryId = catId.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        vCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    call_permissions();
                    vGallery.setChecked(false);
                    captureImage();
                }
            }
        });
        vGallery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    call_permissions();
                    vCamera.setChecked(false);
                    openGallery(200);
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ticketCreationInfo.put("CreatedBy", sUserId);
                ticketCreationInfo.put("CompanyId", sParentCompanyId);
                ticketCreationInfo.put("LocationId", companyID);
                ticketCreationInfo.put("CategoryId", sCategoryId);
                ticketCreationInfo.put("CreationSource", "M");
                ticketCreationInfo.put("Subject", eSubject.getText().toString());
                ticketCreationInfo.put("ComplainText", eDescription.getText().toString());
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RaiseTicket.this);
                alertDialogBuilder.setTitle("Confirmation!!!");
                alertDialogBuilder
                        .setMessage("Once ticket is created, it can never be rollback. Want to create a ticket?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String jsonString = new Gson().toJson(ticketCreationInfo);
                                try {
                                    new submitTicket(jsonString).execute().get(1000, TimeUnit.MILLISECONDS);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (TimeoutException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
//{"CompanyId":2,"LocationId":2,
// "CategoryId":1,"Subject":"Test 22"
// ,"ComplainText":"Create Complain for test 22",
// "CreatedBy":5064,"CreationSource":"M"}
            }
        });
    }

    private class submitTicket extends AsyncTask<String, Void, String> {
        String jsonString;
        Dialog progress;

        public submitTicket(String jsonString) {
            this.jsonString = jsonString;
            Log.e("tag", jsonString);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (!((Activity) RaiseTicket.this).isFinishing()) {
                progress = ProgressDialog.show(RaiseTicket.this, "Loading data", "Please wait...");
            }
        }

        @Override
        protected String doInBackground(String... params) {
            //String uri = PostUrl.sUrl+"PostTicket";
            String uri = PostUrl.sUrl + "PostComplain";
            String result = "";
            try {
                //Connect
                HttpURLConnection urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
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
            String sStatus = null;
            if (s == null && s.equals("0\n")) {
                MyApp.showMassage(getApplicationContext(), getApplicationContext().getString(R.string.internet_error));
//                Toast.makeText(getApplicationContext(), R.string.internet_error, Toast.LENGTH_LONG).show();
            } else {
                try {
                    String msg = null;

                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONArray jdata = jsonObject.getJSONArray("lstResult");
                    for (int i = 0; i < jdata.length(); i++) {
                        JSONObject object = jdata.getJSONObject(i);
                        msg = object.getString("LastComment");
                        sStatus = object.getString("StatusName");
                        sTicketId = object.getString("Id");
                    }
                    if (msg != null) {
                        MyApp.showMassage(getApplication(), msg);
//                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    Log.e("onPostExecute: ", msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (sFinalImagePath_create == null || sTicketId.equals("0") || sTicketId.equals(""))
                    ;
                else {
                    new UploadFileToServer().execute();
                }
            }
            if (progress != null) {
                progress.dismiss();
            }
            if (sStatus != null) {
                eSubject.setText("");
                eDescription.setText("");
            }

        }
    }

    private void captureImage() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // start the image capture Intent
                startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
        // Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".net.mzi.trackengine.GenericFileProvider", createImageFile());

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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_mmss",
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

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public String getPath(Uri uri) {

        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
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
                cursor = getContentResolver()
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

    private void launchUploadActivity(final String sImagePath) {

        final AlertDialog.Builder Dialog = new AlertDialog.Builder(RaiseTicket.this);
        Dialog.setTitle("Image Selector ");
        Dialog.setCancelable(false);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = li.inflate(R.layout.imagedialogue, null);
        final ImageView upImage = (ImageView) dialogView.findViewById(R.id.imagedia);
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
                        sFinalImagePath_create = sImagePath;
                        iImageIcon.setVisibility(View.VISIBLE);
                        MyApp.showMassage(getApplicationContext(), "Image uploaded successfully!!!");
//                        Toast.makeText(getApplicationContext(), "Image uploaded successfully!!!", Toast.LENGTH_LONG).show();
                    }
                });

        Dialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // launching upload activity
                launchUploadActivity(fileUri.getPath());
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                MyApp.showMassage(this, "User cancelled image capture");
//                Toast.makeText(getApplicationContext(),
//                        "User cancelled image capture", Toast.LENGTH_SHORT)
//                        .show();
            } else {
                MyApp.showMassage(this, "Sorry! Failed to capture image");
                // failed to capture image
//                Toast.makeText(getApplicationContext(),
//                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
//                        .show();
            }
        } else {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (requestCode == 200) {
                    selectedPath = getPath(selectedImageUri);
                    launchUploadActivity(selectedPath);

                    Log.e("selectedPath1 : ", selectedPath);
                } else if (resultCode == RESULT_CANCELED) {
                    // user cancelled Image capture
                    MyApp.showMassage(this, "User cancelled image capture");
//                    Toast.makeText(getApplicationContext(),
//                            "User cancelled image capture", Toast.LENGTH_SHORT)
//                            .show();
                    //tv.setText("Selected File paths : " + selectedPath1 + "," + selectedPath2);
                }
            } else {
                MyApp.showMassage(this, "Something went wrong in Image Upload of Ticket Creation");
//                Toast.makeText(this, "Something went wrong in Image Upload of Ticket Creation", Toast.LENGTH_SHORT).show();
            }

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

    public void openGallery(int req_code) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            "Select file to upload "), req_code);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                    "Select file to upload "), req_code);
        }
    }

    private class fetchCategoryName extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            catName.clear();
            catId.clear();
            catId.add("0");
            catName.add("Select");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s == null) {
                try {
                    Toast.makeText(getApplicationContext(), R.string.internet_error, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
                //s = "THERE WAS AN ERROR";
            } else {
                Log.i("INFO", s);

                try {
                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONArray jdata = jsonObject.getJSONArray("lstComplain_Category");
                    for (int i = 0; i < jdata.length(); i++) {
                        JSONObject object = jdata.getJSONObject(i);
                        catId.add(object.getString("Id"));
                        catName.add(object.getString("CategoryName"));
                    }
                    ArrayAdapter aa = new ArrayAdapter(RaiseTicket.this, android.R.layout.simple_spinner_item, catName);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categoryNameSP.setAdapter(aa);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        Dialog progress;

        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            //progressBar.setProgress(0);
            super.onPreExecute();

            progress = ProgressDialog.show(RaiseTicket.this, "Loading data", "Please wait...");
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
            HttpPost httppost = new HttpPost(PostUrl.sUrl + "PostTicketAttachment");

            try {

                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(sFinalImagePath_create);
                String currentTime;
                Date cDate = new Date();
                currentTime = new SimpleDateFormat("MMM-dd-yyyy HH:mm:ss").format(cDate);
                // Adding file data to http body
                entity.addPart("Files", new FileBody(sourceFile));
                // Extra parameters if you want to pass to server
                entity.addPart("AttachedBy", new StringBody(sUserId));
                entity.addPart("TicketId", new StringBody(sTicketId));
                entity.addPart("Comment", new StringBody("New Ticket Created"));
                entity.addPart("ActivityDate", new StringBody(currentTime));

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
            progress.dismiss();
            // showing the server response in an alert dialog
            showAlert(result);

            super.onPostExecute(result);
        }
    }

    private void showAlert(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String msg;
        if (message.equals("")) {
            msg = "Data has been sent successfully!!!";
        } else
            msg = "Something went wrong!!!\n" + message;
        builder.setMessage(msg).setTitle("Response from Server")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                        //comment.setText("");
                        iImageIcon.setVisibility(View.GONE);
                        sFinalImagePath_create = "";
                        vCamera.setChecked(false);
                        vGallery.setChecked(false);
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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
