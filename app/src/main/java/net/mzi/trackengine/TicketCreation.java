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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.mzi.trackengine.adapter.SuggestionAdapter;
import net.mzi.trackengine.fragment.AddCustomerFragment;
import net.mzi.trackengine.model.PostUrl;
import net.mzi.trackengine.model.TicketInfoClass;

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

public class TicketCreation extends AppCompatActivity {
    static String sFinalImagePath_create;
    long totalSize = 0;
    ImageView ivAddCustomer, ivAddLocation;
    // Camera activity request codes
    public static final int MULTIPLE_PERMISSIONS = 1;
    String[] permissions = new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE};

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public Uri fileUri;
    String selectedPath;
    static Map<String, String> ticketCreationInfo = new HashMap<String, String>();
    Spinner corporateName, categoryName, callTypeSpinner, locationSp, assetSp, subCategorySp, departmentSp;
    String isDefault, DepartmentId;
    AutoCompleteTextView employeeAutoCompleteTextView, employeeContactAutoCompleteTeaxtView;
    static AutoCompleteTextView assetSerialNumber;
    EditText subjectEditText, descEditText, OEMticketEditText;
    Button submit;
    TextInputLayout oemLayout;
    TextView assetText, locationText;
    RadioButton vCamera, vGallery;
    ImageView iImageIcon;
    LinearLayout main;
    String sTicketId;
    static String sEnterpriseId, sEnterpriseLocation, sDepartment;
    String nh_userid, companyID, sParentCompanyId;
    List<String> locationName = new ArrayList<String>();
    List<String> locationId = new ArrayList<String>();

    List<String> corName = new ArrayList<String>();
    List<String> corId = new ArrayList<String>();

    List<String> catName = new ArrayList<String>();
    List<String> catId = new ArrayList<String>();

    List<String> callType = new ArrayList<String>();

    List<String> assetListName = new ArrayList<String>();
    List<String> assetListId = new ArrayList<String>();

    List<String> lSubCategoryId = new ArrayList<String>();
    List<String> lSubCategoryName = new ArrayList<String>();

    List<String> lDepartmentId = new ArrayList<String>();
    List<String> lDepartmentName = new ArrayList<String>();

    List<String> lEngineerId = new ArrayList<String>();
    List<String> lEngineerName = new ArrayList<String>();

    String API_URL = null;
    SharedPreferences pref;
    int poss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_creation);

        callType.add("Sales");
        callType.add("Visit/FollowUp");
        callType.add("Contract");
        callType.add("Chargeable");
        callType.add("Warranty");
        callType.add("Complimentary");
        getSupportActionBar().setTitle("New Ticket");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        pref = getSharedPreferences("login", 0);

        //editor = pref.edit();

        nh_userid = pref.getString("userid", "userid");
        companyID = pref.getString("CompanyId", "CompanyId");
        sParentCompanyId = pref.getString("ParentCompanyId", "ParentCompanyId");
        DepartmentId = pref.getString("DepartmentId", "DepartmentId");
        isDefault = pref.getString("IsDefaultDepartment", "IsDefaultDepartment");

        new fetchCorporateName().execute();
        new fetchCategoryName().execute();
        new FetchEngineerList(nh_userid).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        vCamera = (RadioButton) findViewById(R.id.vCam);
        iImageIcon = (ImageView) findViewById(R.id.imageuplaodicon);
        vGallery = (RadioButton) findViewById(R.id.vGal);
        locationSp = (Spinner) findViewById(R.id.locationSpinner);
        oemLayout = (TextInputLayout) findViewById(R.id.oemTicketTIL);
        OEMticketEditText = (EditText) findViewById(R.id.oemEditText);
        callTypeSpinner = (Spinner) findViewById(R.id.callypeSpinner);
        corporateName = (Spinner) findViewById(R.id.corporateSpinner);
        categoryName = (Spinner) findViewById(R.id.categorySpinner);
        assetSp = (Spinner) findViewById(R.id.assetSpinner);
        assetSerialNumber = (AutoCompleteTextView) findViewById(R.id.assetSerialNumberAutoComplete);
        employeeAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.employeeAutoComplete);
        subjectEditText = (EditText) findViewById(R.id.subjectEditText);
        descEditText = (EditText) findViewById(R.id.descEditText);
        assetText = (TextView) findViewById(R.id.assetTextView);
        ivAddCustomer = (ImageView) findViewById(R.id.addCustomerId);
        ivAddLocation = (ImageView) findViewById(R.id.addLocationId);
        submit = (Button) findViewById(R.id.submitButton);
        subCategorySp = (Spinner) findViewById(R.id.subCategorySpinner);
        departmentSp = (Spinner) findViewById(R.id.deparmentSpinner);
        employeeContactAutoCompleteTeaxtView = (AutoCompleteTextView) findViewById(R.id.ContactAutoComplete);
        main = (LinearLayout) findViewById(R.id.linearMain);
        locationText = (TextView) findViewById(R.id.locationTextId);

        ivAddCustomer.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                Fragment hello = new AddCustomerFragment();
                FragmentManager fragmentManager = ((AppCompatActivity) TicketCreation.this).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.activity_ticket_creation, hello);
                fragmentTransaction.commit();
                main.setVisibility(View.GONE);
            }
        });

        ivAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sEnterpriseId.equals("0")) {
                    try {
                        Toast.makeText(getApplicationContext(), "Please Provide Customer name", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                    }
                } else {
                    Fragment hello = new AddCustomerLoactionFragment();
                    FragmentManager fragmentManager = ((AppCompatActivity) TicketCreation.this).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.activity_ticket_creation, hello);
                    fragmentTransaction.commit();
                    main.setVisibility(View.GONE);
                }
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
        employeeContactAutoCompleteTeaxtView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(),position,Toast.LENGTH_LONG).show();.
                if (SuggestionAdapter.suggestionEmployeeContact.size() == 0) {
                    if (ticketCreationInfo.containsKey("CId_ContactNo")) {
                        ticketCreationInfo.put("EmployeeContactNo", employeeContactAutoCompleteTeaxtView.getText().toString());
                        ticketCreationInfo.put("CId_ContactNo", "0");
                    } else {

                    }
                } else {
                    corporateName.setSelection(corId.indexOf(SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getParentEnterpriseId()));
                    sEnterpriseId = SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getParentEnterpriseId();
                    locationSp.setSelection(locationId.indexOf(SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getEnterpriseId()));
                    departmentSp.setSelection(lDepartmentId.indexOf(SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getDepartmentId()));
                    employeeAutoCompleteTextView.setText(SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getCustomerName());

                    ticketCreationInfo.put("EmployeeContactNo", SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getMobile1());
                    ticketCreationInfo.put("CId_ContactNo", SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getCustomerId());
                    ticketCreationInfo.put("DepartmentId", SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getDepartmentId());
                    ticketCreationInfo.put("DepartmentName", SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getDepartmentName());
                    ticketCreationInfo.put("DepartmentChanged", "false");
                    ticketCreationInfo.put("EmployeeName", SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getCustomerName());
                    ticketCreationInfo.put("CustomerId", SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getCustomerId());

                    Log.e("onItemClick: emploeeid", SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getId());
                    if (SuggestionAdapter.suggestionsEmployee.size() > 0) {
                        if (SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getAssetCount().equals("0")) {
                            assetSp.setVisibility(View.GONE);
                            assetText.setVisibility(View.GONE);
                            assetSerialNumber.setVisibility(View.VISIBLE);
                            assetSerialNumber.setText(SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getSerialNo());
                        } else {

                            int sAssetCountEmployeeContact = Integer.parseInt(SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getAssetCount());

                            if (sAssetCountEmployeeContact > 1) {
                                new FetchAssetList(SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getCustomerId()).execute();
                                assetSp.setVisibility(View.VISIBLE);
                                assetText.setVisibility(View.VISIBLE);
                                assetSerialNumber.setVisibility(View.GONE);

                            } else {
                                assetSp.setVisibility(View.GONE);
                                assetText.setVisibility(View.GONE);
                                assetSerialNumber.setVisibility(View.VISIBLE);
                                assetSerialNumber.setText(SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getSerialNo());
                            }
                        }
                    }

                }
            }
        });
        assetSerialNumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (SuggestionAdapter.suggestionsAsset.size() == 0) {
                    if (ticketCreationInfo.containsKey("AssetId")) {
                        ticketCreationInfo.put("AssetName", assetSerialNumber.getText().toString());
                        ticketCreationInfo.put("AssetId", "0");
                    } else {

                    }
                } else {

                    corporateName.setSelection(corId.indexOf(SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getParentEnterpriseId()));
                    sEnterpriseId = SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getParentEnterpriseId();
                    locationSp.setSelection(locationId.indexOf(SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getEnterpriseId()));
                    departmentSp.setSelection(lDepartmentId.indexOf(SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getDepartmentId()));
                    employeeContactAutoCompleteTeaxtView.setText(SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getMobile1());
                    employeeAutoCompleteTextView.setText(SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getCustomerName());

                    //corName.get(corId.indexOf(SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getEnterpriseId()));
                    ticketCreationInfo.put("AssetName", assetSerialNumber.getText().toString());
                    ticketCreationInfo.put("AssetId", SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getId());
                    ticketCreationInfo.put("EmployeeContactNo", SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getMobile1());
                    ticketCreationInfo.put("CId_ContactNo", SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getCustomerId());
                    ticketCreationInfo.put("DepartmentId", SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getDepartmentId());
                    ticketCreationInfo.put("DepartmentName", SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getDepartmentName());
                    ticketCreationInfo.put("DepartmentChanged", "false");
                    ticketCreationInfo.put("EmployeeName", SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getCustomerName());
                    ticketCreationInfo.put("CustomerId", SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getCustomerId());


                    Log.e("onItemClick: emploeeid", SuggestionAdapter.suggestionsAsset.get(SuggestionAdapter.id).getId());
               /* if(SuggestionAdapter.suggestionsAsset.size()>0){
                    assetSp.setVisibility(View.GONE);
                    assetText.setVisibility(View.GONE);
                    assetSerialNumber.setVisibility(View.VISIBLE);
                }
                else {
                    assetSp.setVisibility(View.VISIBLE);
                    assetText.setVisibility(View.VISIBLE);
                    assetSerialNumber.setVisibility(View.GONE);
                }*/
                    int sAssetCountEmployee = 0, sAssetCountEmployeeContact = 0;
                    if (SuggestionAdapter.suggestionsEmployee.size() > 0) {
                        sAssetCountEmployee = Integer.parseInt(SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getAssetCount());
                    }
                    if (SuggestionAdapter.suggestionEmployeeContact.size() > 0) {
                        sAssetCountEmployeeContact = Integer.parseInt(SuggestionAdapter.suggestionEmployeeContact.get(SuggestionAdapter.id).getAssetCount());
                    }
                    if (sAssetCountEmployee > 1 || sAssetCountEmployeeContact > 1) {
                        new FetchAssetList(SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getCustomerId()).execute();
                        assetSp.setVisibility(View.VISIBLE);
                        assetText.setVisibility(View.VISIBLE);
                        assetSerialNumber.setVisibility(View.GONE);
                    } else {
                        assetSp.setVisibility(View.GONE);
                        assetText.setVisibility(View.GONE);
                        assetSerialNumber.setVisibility(View.VISIBLE);
                    }

                }
            }
        });

        employeeAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (SuggestionAdapter.suggestionsEmployee.size() == 0) {
                    if (ticketCreationInfo.containsKey("CustomerId")) {
                        ticketCreationInfo.put("EmployeeName", employeeAutoCompleteTextView.getText().toString());
                        ticketCreationInfo.put("CustomerId", "0");
                    } else {

                    }
                } else {

                    corporateName.setSelection(corId.indexOf(SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getParentEnterpriseId()));
                    sEnterpriseId = SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getParentEnterpriseId();
                    locationSp.setSelection(locationId.indexOf(SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getEnterpriseId()));
                    departmentSp.setSelection(lDepartmentId.indexOf(SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getDepartmentId()));
                    employeeContactAutoCompleteTeaxtView.setText(SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getMobile1());
                    //corName.get(corId.indexOf(SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getEnterpriseId()));

                    ticketCreationInfo.put("EmployeeContactNo", SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getMobile1());
                    ticketCreationInfo.put("CId_ContactNo", SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getCustomerId());
                    ticketCreationInfo.put("DepartmentId", SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getDepartmentId());
                    ticketCreationInfo.put("DepartmentName", SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getDepartmentName());
                    ticketCreationInfo.put("DepartmentChanged", "false");
                    ticketCreationInfo.put("EmployeeName", SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getCustomerName());
                    ticketCreationInfo.put("CustomerId", SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getCustomerId());

                    Log.e("onItemClick: emploeeid", SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getId());
                    if (SuggestionAdapter.suggestionsEmployee.size() > 0) {
                        assetSp.setVisibility(View.GONE);
                        assetText.setVisibility(View.GONE);
                        assetSerialNumber.setVisibility(View.VISIBLE);
                    } else {
                        assetSp.setVisibility(View.VISIBLE);
                        assetText.setVisibility(View.VISIBLE);
                        assetSerialNumber.setVisibility(View.GONE);
                    }
                    int sAssetCountEmployee = Integer.parseInt(SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getAssetCount());
                    if (sAssetCountEmployee > 1) {
                        new FetchAssetList(SuggestionAdapter.suggestionsEmployee.get(SuggestionAdapter.id).getCustomerId()).execute();
                        assetSp.setVisibility(View.VISIBLE);
                        assetText.setVisibility(View.VISIBLE);
                        assetSerialNumber.setVisibility(View.GONE);

                    } else {
                        assetSp.setVisibility(View.GONE);
                        assetText.setVisibility(View.GONE);
                        assetSerialNumber.setVisibility(View.VISIBLE);
                    }

                }
            }
        });
        subCategorySp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (lSubCategoryId.size() > 0) {
                    if (lSubCategoryId.get(position).equals("0"))
                        ticketCreationInfo.put("SubCategoryIds", "");
                    else
                        ticketCreationInfo.put("SubCategoryIds", lSubCategoryId.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        departmentSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //ticketCreationInfo.put("AssetId",assetListId.get(position));
                sDepartment = lDepartmentId.get(position);
                if (sDepartment == null) {
                    sDepartment = "0";
                }
                if (ticketCreationInfo.containsKey("DepartmentId")) {
                    String sDept = ticketCreationInfo.get("DepartmentId");
                    if (lDepartmentId.equals("0")) {

                        ticketCreationInfo.put("DepartmentChanged", "false");
                        ticketCreationInfo.put("DepartmentId", lDepartmentId.get(position));
                        ticketCreationInfo.put("DepartmentName", "");
                    } else {
                        if (sDept.equals(lDepartmentId.get(position))) {
                            ticketCreationInfo.put("DepartmentChanged", "false");
                            ticketCreationInfo.put("DepartmentId", lDepartmentId.get(position));
                            ticketCreationInfo.put("DepartmentName", lDepartmentName.get(position));
                        } else {
                            ticketCreationInfo.put("DepartmentChanged", "true");
                            ticketCreationInfo.put("DepartmentId", lDepartmentId.get(position));
                            ticketCreationInfo.put("DepartmentName", lDepartmentName.get(position));
                        }
                    }

                } else {
                    ticketCreationInfo.put("DepartmentChanged", "true");
                    ticketCreationInfo.put("DepartmentId", lDepartmentId.get(position));
                    ticketCreationInfo.put("DepartmentName", lDepartmentName.get(position));
                }
                assetSerialNumber.setThreshold(1);
                assetSerialNumber.setAdapter(new SuggestionAdapter(TicketCreation.this, assetSerialNumber.getText().toString(), PostUrl.sUrl + "SearchAsset?iParentEnterpriseId=" + sEnterpriseId + "&iCompanyId=" + companyID + "&iEnterpriseId=" + sEnterpriseLocation + "&iDepartmentId=" + sDepartment + "&sSearchText=", "Asset Number"));

                employeeContactAutoCompleteTeaxtView.setThreshold(1);
                employeeContactAutoCompleteTeaxtView.setAdapter(new SuggestionAdapter(TicketCreation.this, employeeContactAutoCompleteTeaxtView.getText().toString(), PostUrl.sUrl + "GetEmployeeNameByMobile?iParentEnterpriseId=" + sEnterpriseId + "&iCompanyId=" + companyID + "&iEnterpriseId=" + sEnterpriseLocation + "&iDepartmentId=" + sDepartment + "&sSearchText=", "Contact Number"));

                employeeAutoCompleteTextView.setThreshold(1);
                employeeAutoCompleteTextView.setAdapter(new SuggestionAdapter(TicketCreation.this, employeeAutoCompleteTextView.getText().toString(), PostUrl.sUrl + "GetEmployeeName?iCompanyId=" + companyID + "&iParentEnterpriseId=" + sEnterpriseId + "&iEnterpriseId=" + sEnterpriseLocation + "&iDepartmentId=" + sDepartment + "&sSearchText=", "Get Employees"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        assetSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ticketCreationInfo.put("AssetId", assetListId.get(position));
                ticketCreationInfo.put("AssetName", assetListName.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter aa = new ArrayAdapter(TicketCreation.this, android.R.layout.simple_spinner_item, callType);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        callTypeSpinner.setAdapter(aa);
        locationSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                sEnterpriseLocation = locationId.get(position);
                new FetchDepartment().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        callTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ticketCreationInfo.put("CallTypeId", String.valueOf(position + 1));
                if (position == 4) {
                    oemLayout.setVisibility(View.VISIBLE);
                } else {
                    oemLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //ticketCreationInfo.put("DepartmentId",DepartmentId);

                ticketCreationInfo.put("CreatedBy", nh_userid);
                ticketCreationInfo.put("CreatedByType", "I");
                ticketCreationInfo.put("CreationSource", "MA");
                ticketCreationInfo.put("OEMTicet", OEMticketEditText.getText().toString());
                if (ticketCreationInfo.containsKey("AssetName")) {
                    if (ticketCreationInfo.get("AssetName").equals(TicketCreation.assetSerialNumber.getText().toString())) {

                    } else {
                        ticketCreationInfo.put("AssetName", assetSerialNumber.getText().toString());
                        ticketCreationInfo.put("AssetId", "0");
                    }
                } else {
                    ticketCreationInfo.put("AssetName", "");
                    ticketCreationInfo.put("AssetId", "0");
                }

                if (ticketCreationInfo.containsKey("EmployeeContactNo")) {
                    if (ticketCreationInfo.get("EmployeeContactNo").equals(employeeContactAutoCompleteTeaxtView.getText().toString())) {

                    } else {
                        ticketCreationInfo.put("EmployeeContactNo", employeeContactAutoCompleteTeaxtView.getText().toString());
                        ticketCreationInfo.put("CId_ContactNo", "0");
                    }
                } else {
                    ticketCreationInfo.put("EmployeeContactNo", "");
                    ticketCreationInfo.put("CId_ContactNo", "0");
                }
                if (ticketCreationInfo.containsKey("EmployeeName")) {
                    if (ticketCreationInfo.get("EmployeeName").equals(employeeAutoCompleteTextView.getText().toString())) {

                    } else {
                        ticketCreationInfo.put("EmployeeName", employeeAutoCompleteTextView.getText().toString());
                        ticketCreationInfo.put("CustomerId", "0");
                    }
                } else {
                    ticketCreationInfo.put("EmployeeName", employeeAutoCompleteTextView.getText().toString());
                    ticketCreationInfo.put("CustomerId", "0");
                }

                ticketCreationInfo.put("Subject", subjectEditText.getText().toString());
                ticketCreationInfo.put("IssueText", descEditText.getText().toString());
                ticketCreationInfo.put("CompanyId", companyID);
                ticketCreationInfo.put("ParentCompanyId", sParentCompanyId);
                ticketCreationInfo.put("EnterpriseId", sEnterpriseLocation);


                final AlertDialog.Builder Dialog = new AlertDialog.Builder(TicketCreation.this);
                Dialog.setTitle("Select Engineer");
                Dialog.setCancelable(false);
                LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = li.inflate(R.layout.option, null);
                Spinner spinnercategory = (Spinner) dialogView
                        .findViewById(R.id.viewSpin);
                final EditText commemt = (EditText) dialogView.findViewById(R.id.comment);
                commemt.setVisibility(View.GONE);
                Dialog.setView(dialogView);
                Dialog.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
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
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Boolean wantToCloseDialog = true;
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TicketCreation.this);
                            alertDialogBuilder.setTitle("Confirmation!!!");
                            alertDialogBuilder
                                    .setMessage("Once ticket is created, it can never be rollback. Want to create a ticket?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            String jsonString = new Gson().toJson(ticketCreationInfo);
                                            try {
                                                new submitTicket(jsonString, v).execute().get(1000, TimeUnit.MILLISECONDS);
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
                            dialog.cancel();
                            //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                        }
                    }
                });
                //Dialog.show();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(TicketCreation.this, android.R.layout.simple_spinner_item, lEngineerName);
                // adapter.addAll(arr);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnercategory.setAdapter(adapter);

                spinnercategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    public void onItemSelected(AdapterView<?> parent, View arg1,
                                               int arg2, long arg3) {
                        //String selItem = parent.getSelectedItem().toString();
                        poss = arg2;
                        ticketCreationInfo.put("AssignToUser", lEngineerId.get(poss));

                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub
                    }
                });

            }
        });
        corporateName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //ticketCreationInfo.put("EnterpriseId",corId.get(position));
                sEnterpriseId = corId.get(position);
                new FetchLocation(sEnterpriseId, TicketCreation.this).execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        categoryName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ticketCreationInfo.put("IssueCategoryId", catId.get(position));
                if (catId.get(position).equals("0")) {

                    ArrayAdapter aa = new ArrayAdapter(TicketCreation.this, android.R.layout.simple_spinner_item, lSubCategoryName);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    subCategorySp.setAdapter(aa);
                } else {
                    new FetchSubCategoryName(catId.get(position)).execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void captureImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
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
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        1);
            }
        else {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // start the image capture Intent
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);

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
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00ffffff")));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.imagedialogue);
        final ImageView upImage = dialog.findViewById(R.id.imagedia);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        final Bitmap bitmap = BitmapFactory.decodeFile(sImagePath, options);
        upImage.setImageBitmap(bitmap);

        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_ok = dialog.findViewById(R.id.btn_ok);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sFinalImagePath_create = sImagePath;
                iImageIcon.setVisibility(View.VISIBLE);
                try {
                    Toast.makeText(getApplicationContext(), "Image uploaded successfully!!!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.show();
//        final AlertDialog.Builder Dialog = new AlertDialog.Builder(TicketCreation.this);
//        Dialog.setTitle("Image Selector ");
//        Dialog.setCancelable(false);
//        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View dialogView = li.inflate(R.layout.imagedialogue, null);
//        final ImageView upImage = (ImageView) dialogView.findViewById(R.id.imagedia);
//        Dialog.setView(dialogView);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//
//        // down sizing image as it throws OutOfMemory Exception for larger
//        // images
//        options.inSampleSize = 8;
//
//        final Bitmap bitmap = BitmapFactory.decodeFile(sImagePath, options);
//
//        upImage.setImageBitmap(bitmap);
//        Dialog.setPositiveButton("Ok",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        sFinalImagePath_create = sImagePath;
//                        iImageIcon.setVisibility(View.VISIBLE);
//                        try {
//                            Toast.makeText(getApplicationContext(), "Image uploaded successfully!!!", Toast.LENGTH_LONG).show();
//                        } catch (Exception e) {
//                        }
//                    }
//                });
//
//        Dialog.setNegativeButton("Cancel",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        dialog.dismiss();
//                    }
//                });
//        final AlertDialog dialog = Dialog.create();
//        dialog.show();
        /*Intent i = new Intent(MainActivity.this, UploadActivity.class);
        i.putExtra("filePath", fileUri.getPath());
        i.putExtra("isImage", isImage);
        startActivity(i);*/
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
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }

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
                try {
                    Toast.makeText(getApplicationContext(),
                            "User cancelled image capture", Toast.LENGTH_SHORT)
                            .show();
                } catch (Exception e) {
                }
            } else {
                // failed to capture image
                try {
                    Toast.makeText(getApplicationContext(),
                            "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                            .show();
                } catch (Exception e) {
                }
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
                    try {
                        Toast.makeText(getApplicationContext(),
                                "User cancelled image capture", Toast.LENGTH_SHORT)
                                .show();
                    } catch (Exception e) {
                    }
                    //tv.setText("Selected File paths : " + selectedPath1 + "," + selectedPath2);
                }
            } else {
                try {
                    Toast.makeText(this, "Something went wrong in Image Upload of Ticket Creation", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                }
            }

        }

    }

    private class FetchAssetList extends AsyncTask<String, Void, String> {
        String id;

        public FetchAssetList(String id) {
            this.id = id;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(PostUrl.sUrl + "GetAssetsOfCustomer?iCustomerId=" + id);
                //URL url = new URL("http://trackengine.mzservices.net/api/Post/GetAssetsOfCustomer?iCustomerId="+id);
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
                Log.e("FollowUp CLASS,", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("askdh", "FetchLocation: " + s);
            if (s == null) {
                try {
                    Toast.makeText(getApplicationContext(), R.string.internet_error, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
            } else {
                Log.i("INFO", s);

                try {
                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONArray jdata = jsonObject.getJSONArray("AssetList");
                    for (int i = 0; i < jdata.length(); i++) {
                        JSONObject object = jdata.getJSONObject(i);
                        assetListName.add(object.getString("ServiceItemNo"));
                        assetListId.add(object.getString("Id"));
                    }
                    ArrayAdapter aa = new ArrayAdapter(TicketCreation.this, android.R.layout.simple_spinner_item, assetListName);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    assetSp.setAdapter(aa);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("onPostExecute: ", "error");
                }
            }
        }
    }

    private class FetchLocation extends AsyncTask<String, Void, String> {
        Activity ctx;
        String sEnterpriseID;

        public FetchLocation(String sEnterpriseID, Activity ctx) {
            this.sEnterpriseID = sEnterpriseID;
            this.ctx = ctx;
            Log.e("askdh", "FetchLocation: " + sEnterpriseID);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            locationName.clear();
            locationId.clear();
            if (sEnterpriseId.equals("0")) {
                locationId.add("0");
                locationName.add("Select");
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(PostUrl.sUrl + "GetLocationList?iEnterpriseId=" + sEnterpriseId + "&bShowParent=true");
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
                Log.e("FollowUp CLASS,", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("askdh", "FetchLocation: " + s);
            if (s == null) {
                s = "THERE WAS AN ERROR";
            }
            Log.i("INFO", s);

            try {
                JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                JSONArray jdata = jsonObject.getJSONArray("EnterpriseList");
                for (int i = 0; i < jdata.length(); i++) {
                    JSONObject object = jdata.getJSONObject(i);
                    locationName.add(object.getString("EnterpriseName"));
                    locationId.add(object.getString("Id"));
                }
                ArrayAdapter aa = new ArrayAdapter(TicketCreation.this, android.R.layout.simple_spinner_item, locationName);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                locationSp.setAdapter(aa);
                Log.e("onPostExecute: ", locationName.get(0));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("onPostExecute: ", "error");
            }

        }
    }

    private class fetchCorporateName extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            corName.clear();
            corId.clear();
            corId.add("0");
            corName.add("Select");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(PostUrl.sUrl + "GetEnterpriseList?iCompanyId=" + companyID);
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
                Log.e("FollowUp CLASS,", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                s = "THERE WAS AN ERROR";
            }
            Log.i("INFO", s);

            try {
                JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                JSONArray jdata = jsonObject.getJSONArray("EnterpriseList");
                for (int i = 0; i < jdata.length(); i++) {
                    JSONObject object = jdata.getJSONObject(i);
                    corId.add(object.getString("Id"));
                    corName.add(object.getString("EnterpriseName"));
                }
                ArrayAdapter aa = new ArrayAdapter(TicketCreation.this, android.R.layout.simple_spinner_item, corName);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                corporateName.setAdapter(aa);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class FetchEngineerList extends AsyncTask<String, Void, String> {
        String nh_userid;

        FetchEngineerList(String nh_userid) {
            this.nh_userid = nh_userid;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lEngineerName.clear();
            lEngineerId.clear();
            lEngineerName.add("None");
            lEngineerId.add("0");
            lEngineerId.add(nh_userid);
            lEngineerName.add("Self Assigning");
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                URL url = new URL(PostUrl.sUrl + "GetManPowerContracts?iUserId=" + nh_userid + "&dtDate=2017-10-04");
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
                Log.e("FollowUp CLASS,", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                s = "THERE WAS AN ERROR";
            }
            Log.e("INFO", s);
            try {
                JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                JSONArray jdata = jsonObject.getJSONArray("ManPower_ContractDetails");
                for (int i = 0; i < jdata.length(); i++) {
                    JSONObject object = jdata.getJSONObject(i);
                    lEngineerId.add(object.getString("UserId"));
                    lEngineerName.add(object.getString("UserName"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

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
                URL url = new URL(PostUrl.sUrl + "GetIssueCategories?iCompanyId=" + sParentCompanyId);
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
                Log.e("FollowUp CLASS,", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                s = "THERE WAS AN ERROR";
            }
            Log.i("INFO", s);
            try {
                JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                JSONArray jdata = jsonObject.getJSONArray("IssueCategoryList");
                for (int i = 0; i < jdata.length(); i++) {
                    JSONObject object = jdata.getJSONObject(i);
                    catId.add(object.getString("Id"));
                    catName.add(object.getString("CategoryName"));
                }
                ArrayAdapter aa = new ArrayAdapter(TicketCreation.this, android.R.layout.simple_spinner_item, catName);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categoryName.setAdapter(aa);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class FetchSubCategoryName extends AsyncTask<String, Void, String> {
        String sCategoryId;

        public FetchSubCategoryName(String sCategoryId) {
            this.sCategoryId = sCategoryId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lSubCategoryId.clear();
            lSubCategoryName.clear();
            lSubCategoryId.add("0");
            lSubCategoryName.add("Select");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(PostUrl.sUrl + "GetSubCategory?iCategoryId=" + sCategoryId);
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
                Log.e("FollowUp CLASS,", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                s = "THERE WAS AN ERROR";
            }
            Log.i("INFO", s);
            try {
                JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                JSONArray jdata = jsonObject.getJSONArray("SubCategoryList");
                for (int i = 0; i < jdata.length(); i++) {
                    JSONObject object = jdata.getJSONObject(i);
                    lSubCategoryId.add(object.getString("Id"));
                    lSubCategoryName.add(object.getString("CategoryName"));
                }
                ArrayAdapter aa = new ArrayAdapter(TicketCreation.this, android.R.layout.simple_spinner_item, lSubCategoryName);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCategorySp.setAdapter(aa);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class FetchDepartment extends AsyncTask<String, Void, String> {
        public FetchDepartment() {
            //this.sCategoryId=sCategoryId;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            lDepartmentId.clear();
            lDepartmentName.clear();
            if (sEnterpriseLocation.equals("0")) {
                lDepartmentId.add("0");
                lDepartmentName.add("Select");
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(PostUrl.sUrl + "SearchDepartment?iCompanyId=" + companyID + "&iParentEnterpriseId=" + sEnterpriseId + "&iEnterpriseId=" + sEnterpriseLocation + "&sSearchText=%");
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
                Log.e("FollowUp CLASS,", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                s = "THERE WAS AN ERROR";
            }
            Log.i("INFO", s);
            try {
                JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                JSONArray jdata = jsonObject.getJSONArray("lstSearchedDepartment");
                for (int i = 0; i < jdata.length(); i++) {
                    JSONObject object = jdata.getJSONObject(i);
                    lDepartmentId.add(object.getString("Id"));
                    lDepartmentName.add(object.getString("DepartmentName"));
                }
                ArrayAdapter aa = new ArrayAdapter(TicketCreation.this, android.R.layout.simple_spinner_item, lDepartmentName);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                departmentSp.setAdapter(aa);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class submitTicket extends AsyncTask<String, Void, String> {
        String jsonString;
        Dialog progress;
        View v;

        public submitTicket(String jsonString, View v) {
            this.v = v;
            this.jsonString = jsonString;
            Log.e("tag", jsonString);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!((Activity) TicketCreation.this).isFinishing()) {
                progress = ProgressDialog.show(TicketCreation.this, "Loading data", "Please wait...");
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String uri = PostUrl.sUrl + "PostTicket";
            String result = "";
            try {
                //Connect
                HttpURLConnection urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
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
            if (s == null) {
                try {
                    Toast.makeText(getApplicationContext(), R.string.internet_error, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
                //s = "THERE WAS AN ERROR";
            } else {
                try {
                    String msg = null;

                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONArray jdata = jsonObject.getJSONArray("lstResult");
                    for (int i = 0; i < jdata.length(); i++) {
                        JSONObject object = jdata.getJSONObject(i);
                        msg = object.getString("Message");
                        sStatus = object.getString("Status");
                        sTicketId = object.getString("Id");
                    }
                    if (msg != null) {
                        try {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                        }
                    }

                    Log.e("onPostExecute: ", msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (sFinalImagePath_create == null) ;
                else {
                    new UploadFileToServer().execute();
                }
            }
            if (progress != null) {
                progress.dismiss();
            }
            if (sStatus != null) {
                if (sStatus.equals("true")) {
                    employeeAutoCompleteTextView.setText("");
                    subjectEditText.setText("");
                    descEditText.setText("");
                    assetSerialNumber.setText("");
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

            progress = ProgressDialog.show(TicketCreation.this, "Loading data", "Please wait...");
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
            Map<String, TicketInfoClass> issueDetailsHistory = MyApp.getApplication().readIssueDetailsHistory();
            HttpPost httppost = null;
            if (issueDetailsHistory.containsKey(sTicketId))
                if (issueDetailsHistory.get(sTicketId).getType().equals("Ticket"))
                    httppost = new HttpPost(PostUrl.sUrl + "PostTicketAttachment");
                else
                    httppost = new HttpPost(PostUrl.sUrl + "PostTaskAttachment");


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
                entity.addPart("AttachedBy", new StringBody(nh_userid));
                if (issueDetailsHistory.get(sTicketId).getType().equals("Ticket"))
                    entity.addPart("TicketId", new StringBody(sTicketId));
                else
                    entity.addPart("TaskId", new StringBody(sTicketId));
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
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
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

