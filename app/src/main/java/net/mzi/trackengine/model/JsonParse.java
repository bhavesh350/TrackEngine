package net.mzi.trackengine.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.mzi.trackengine.model.SuggestGetSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class JsonParse {
	String sCompanyID,sParentCompanyId;
	String URL;
    String  value;
	SharedPreferences pref;
	 public JsonParse(Context context,String URL,String value){
		 this.URL=URL;
         this.value=value;
         pref = context.getSharedPreferences("login", 0);
         sCompanyID=pref.getString("CompanyId","CompanyId");
         sParentCompanyId=pref.getString("ParentCompanyId","ParentCompanyId");
         //Log.e( "getParseJsonWCF: ",sParentCompanyId+sCompanyID+TicketCreation.sEnterpriseId );
     }

	/* public JsonParse(double current_latitude,double current_longitude){
		 this.current_latitude=current_latitude;
		 this.current_longitude=current_longitude;
	 }*/
	 public List<SuggestGetSet> getParseJsonWCF(String sName)
	 {

         List<SuggestGetSet> ListData = new ArrayList<SuggestGetSet>();
			 try {
				String temp=sName.replace(" ", "%20");
                 sParentCompanyId="0";
				URL js = new URL(URL+temp);
                 Log.e( "getParseJsonWCF: ",js.toString());
                 URLConnection jc = js.openConnection();
				BufferedReader reader = new BufferedReader(new InputStreamReader(jc.getInputStream()));
				String line = reader.readLine();
				JSONObject jsonResponse = new JSONObject(line);
                 if(value.equals("Asset Number")){
                     JSONArray jsonArray = jsonResponse.getJSONArray("lstSearchedAsset");
                     for(int i = 0; i < jsonArray.length(); i++){
                         JSONObject r = jsonArray.getJSONObject(i);
                         ListData.add(new SuggestGetSet(r.getString("Id"),"",r.getString("SerialNo"),r.getString("ServiceItemNo"),r.getString("CustomerName"),r.getString("CustomerId"),r.getString("Mobile1"),r.getString("Email"),r.getString("EnterpriseId"),r.getString("ParentEnterpriseId"),r.getString("DepartmentId"),r.getString("DepartmentName"),""));
                     }
                 }
                 else if(value.equals("Get Employees")){
                     JSONArray jsonArray = jsonResponse.getJSONArray("lstSearchedCustomers");
                     for(int i = 0; i < jsonArray.length(); i++){
                         JSONObject r = jsonArray.getJSONObject(i);
                         ListData.add(new SuggestGetSet("","","","",r.getString("CustomerName"),r.getString("Id"),r.getString("Mobile1"),r.getString("Email"),r.getString("EnterpriseId"),r.getString("ParentEnterpriseId"),r.getString("DepartmentId"),r.getString("DepartmentName"),r.getString("AssetCount")));
                     }
                 }
                 else {
                     JSONArray jsonArray = jsonResponse.getJSONArray("lstSearchedCustomers");
                     for(int i = 0; i < jsonArray.length(); i++){
                         JSONObject r = jsonArray.getJSONObject(i);
                         ListData.add(new SuggestGetSet("","","","",r.getString("CustomerName"),r.getString("Id"),r.getString("Mobile1"),r.getString("Email"),r.getString("EnterpriseId"),r.getString("ParentEnterpriseId"),r.getString("DepartmentId"),r.getString("DepartmentName"),r.getString("AssetCount")));
                     }
                 }

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			 return ListData;

	 }
	 
}
