package net.mzi.trackengine.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.mzi.trackengine.model.PostUrl;
import net.mzi.trackengine.R;
import net.mzi.trackengine.TicketCreation;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by Poonam on 9/19/2017.
 */

public class AddCustomerFragment extends Fragment {
    SharedPreferences pref;

    Map<String,String> mCustomerCreationInfo=new HashMap<String, String>();

    List<String> sStateName=new ArrayList<String>();
    List<String> sStateId=new ArrayList<String>();

    List<String> sCityName=new ArrayList<String>();
    List<String> sCityId=new ArrayList<String>();

/*
    List<String> sPincodeId=new ArrayList<String>();
    List<String> sPincodeName=new ArrayList<String>();
*/

    String sPincodeId;
    String sPincodeName;


    List<String> sAreaId=new ArrayList<String>();
    List<String> sAreaName=new ArrayList<String>();

    String sFinalStateId,sFinalCityId,sFinalPincodeId,sFinalAreaId;

    Spinner spState,spCity,spArea;
    EditText etCustomername,etAL1,etAL2,etAL3;
    Button bAddcustomer;
    TextView tvPincode;
    AddCustomerFragment ctx = this;
    private android.app.Activity Activity;

    public AddCustomerFragment(){

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_add_customer, container, false);
        pref = getContext().getSharedPreferences("login", 0);
        mCustomerCreationInfo.put("CompanyId",pref.getString("CompanyId","CompanyId"));
        spState=(Spinner)view.findViewById(R.id.stateSpinner);
        etCustomername=(EditText)view.findViewById(R.id.addCustomerEditText);
        spCity=(Spinner)view.findViewById(R.id.citySpinner);
        tvPincode=(TextView) view.findViewById(R.id.pincodeTextView);
        spArea=(Spinner)view.findViewById(R.id.areaSpinner);
        etAL1=(EditText)view.findViewById(R.id.addressline1EditText);
        etAL2=(EditText)view.findViewById(R.id.addressline2EditText);
        etAL3=(EditText)view.findViewById(R.id.addressline3EditText);
        bAddcustomer=(Button)view.findViewById(R.id.addCustomerButton);

        bAddcustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomerCreationInfo.put("EnterpriseName",etCustomername.getText().toString());
                mCustomerCreationInfo.put("DefaultAddress1",etAL1.getText().toString());
                mCustomerCreationInfo.put("DefaultAddress2",etAL2.getText().toString());
                mCustomerCreationInfo.put("DefaultAddress3",etAL3.getText().toString());
                mCustomerCreationInfo.put("AllowUserCreation","false");
                String jsonString = new Gson().toJson(mCustomerCreationInfo);
                new AddCustomer(jsonString,v).execute();
            }
        });
        new FetchState().execute();

        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //String sState=sStateId.get(position);
                sFinalStateId=sStateId.get(position);
                mCustomerCreationInfo.put("DefaultStateId",sStateId.get(position));
                new FetchCity(sStateId.get(position),getActivity()).execute();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sFinalCityId=sCityId.get(position);
                mCustomerCreationInfo.put("DefaultCityId",sCityId.get(position));
                new FetchArea(sCityId.get(position)).execute();
                //new FetchPincode(sCityId.get(position)).execute();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //String sState=sStateId.get(position);
                sFinalAreaId=sAreaId.get(position);
                mCustomerCreationInfo.put("DefaultAreaId",sAreaId.get(position));
                new FetchPincode(sAreaId .get(position)).execute();
                //new FetchArea(sPincodeId.get(position)).execute();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    private class AddCustomer extends AsyncTask<String, Void, String> {
        String jsonString;
        Dialog progress;
        View v;
        public AddCustomer(String jsonString, View v) {
            this.v=v;
            this.jsonString=jsonString;
            Log.e("tag",jsonString);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!((Activity) getActivity()).isFinishing()) {
                progress = ProgressDialog.show(getActivity(),
                        "Loading data", "Please wait...");
            }
        }
        @Override
        protected String doInBackground(String... params) {

            String uri = PostUrl.sUrl+"CreateCustomer";
            String result ="";
            try {
                //Connect
                HttpURLConnection urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
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
            super.onPostExecute(s);
            if (s == null) {
                Toast.makeText(getActivity(), R.string.internet_error, Toast.LENGTH_LONG).show();
            } else {
                if (progress != null) {
                    progress.dismiss();
                }
                getActivity().getSupportFragmentManager().beginTransaction().remove(ctx).commit();
                getActivity().finish();
                Intent i = new Intent(getActivity(), TicketCreation.class);
                startActivity(i);
            }
        }
    }

    private class FetchState extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sStateName.clear();
            sStateId.clear();
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(PostUrl.sUrl+"GetState");
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
                //Toast.makeText(getActivity(),R.string.internet_error, Toast.LENGTH_LONG).show();
            } else {
                Log.i("INFO", s);

                try {
                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONArray jdata = jsonObject.getJSONArray("StateList");
                    for (int i = 0; i < jdata.length(); i++) {
                        JSONObject object = jdata.getJSONObject(i);
                        sStateId.add(object.getString("Id"));
                        sStateName.add(object.getString("Name"));
                    }
                    ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, sStateName);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spState.setAdapter(aa);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class FetchCity extends AsyncTask<String, Void, String> {
        String sTempStateId;
        Context ctx;
        public FetchCity(String sStateId, Context ctx){
            this.sTempStateId=sStateId;
            this.ctx=ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sCityId.clear();
            sCityName.clear();
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(PostUrl.sUrl+"GetCity?iStateId="+sTempStateId);
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
                //Toast.makeText(getActivity(), "Internet connection is not working properly", Toast.LENGTH_LONG).show();
            } else {
                Log.i("INFO", s);

                try {
                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONArray jdata = jsonObject.getJSONArray("CityList");
                    for (int i = 0; i < jdata.length(); i++) {
                        JSONObject object = jdata.getJSONObject(i);
                        sCityId.add(object.getString("Id"));
                        sCityName.add(object.getString("Name"));
                    }
                    ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, sCityName);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spCity.setAdapter(aa);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class FetchPincode extends AsyncTask<String, Void, String> {
        String sTempAreaId;
        public FetchPincode(String sTempAreaId){
            this.sTempAreaId=sTempAreaId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();/*
            sPincodeId.clear();
            sPincodeName.clear();*/
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(PostUrl.sUrl+"GetPinCodes?iAreaId="+sTempAreaId+"&sPincode=");
                //URL url = new URL("http://trackengine.mzservices.net/api/Post/GetPinCode?iCityId="+sTempCityId);
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

            } else {
                Log.i("INFO", s);

                try {
                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONArray jdata = jsonObject.getJSONArray("PincodeList");
                    for (int i = 0; i < jdata.length(); i++) {
                        JSONObject object = jdata.getJSONObject(i);
                        sPincodeId = object.getString("Id");
                        sPincodeName = object.getString("Pincode");
                    }
                    sFinalPincodeId = sPincodeId;
                    mCustomerCreationInfo.put("DefaultPincodeId", sPincodeId);
                    tvPincode.setText(sPincodeName);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class FetchArea extends AsyncTask<String, Void, String> {
        String sTempCityId;
        public FetchArea(String sTempCityId){
            this.sTempCityId=sTempCityId;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sAreaId.clear();
            sAreaName.clear();
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(PostUrl.sUrl+"GetAreas?iCityId="+sTempCityId+"&sAreaName=acd");
                //URL url = new URL("http://trackengine.mzservices.net/api/Post/GetArea?iPincodeId="+sTempPincodeId);
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
                //Toast.makeText(getActivity(), "Internet connetion is not working properly", Toast.LENGTH_LONG).show();
            } else {
                Log.i("INFO", s);

                try {
                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONArray jdata = jsonObject.getJSONArray("AreaList");
                    for (int i = 0; i < jdata.length(); i++) {
                        JSONObject object = jdata.getJSONObject(i);
                        sAreaId.add(object.getString("Id"));
                        sAreaName.add(object.getString("AreaName"));
                    }
                    ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, sAreaName);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spArea.setAdapter(aa);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
