package net.mzi.trackengine.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import net.mzi.trackengine.model.JsonParse;
import net.mzi.trackengine.model.SuggestGetSet;

import java.util.ArrayList;
import java.util.List;

public class SuggestionAdapter extends ArrayAdapter<String> {
	public static int id;
    String value;
	Context context;
	String URL;
	protected static final String TAG = "SuggestionAdapter";
	public static List<SuggestGetSet> suggestionsEmployee,suggestionsAsset,suggestionEmployeeContact;
	public SuggestionAdapter(Activity context, String nameFilter,String URL,String vlaue) {
		super(context, android.R.layout.simple_spinner_dropdown_item);
		this.context=context;
		this.URL=URL;
        this.value=vlaue;
		suggestionsEmployee = new ArrayList<SuggestGetSet>();
		suggestionsAsset = new ArrayList<SuggestGetSet>();
        suggestionEmployeeContact=new ArrayList<SuggestGetSet>();
	}

	@Override
	public int getCount() {
		if(value.equals("Asset Number"))
			return suggestionsAsset.size();
		else if(value.equals("Get Employees")){
            return suggestionsEmployee.size();
        }
        else
			return suggestionEmployeeContact.size();

	}

	@Override
	public String getItem(int index) {
		id=index;
		if(value.equals("Asset Number"))
			return suggestionsAsset.get(index).getSerialNo();
        else if(value.equals("Get Employees")){
            return suggestionsEmployee.get(index).getCustomerName();
        }
		else
			return suggestionEmployeeContact.get(index).getMobile1();

	}

	@Override
	public Filter getFilter() {
		Filter myFilter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();
				JsonParse jp=new JsonParse(context,URL,value);
                Log.e(TAG, "performFiltering: "+URL);
                if (constraint != null) {
					// A class that queries a web API, parses the data and
					// returns an ArrayList<GoEuroGetSet>
					if(value.equals("Asset Number")) {
						List<SuggestGetSet> new_suggestions = jp.getParseJsonWCF(constraint.toString());
						suggestionsAsset.clear();
						for (int i = 0; i < new_suggestions.size(); i++) {
                            //new SuggestGetSet(r.getString("Id"),"",r.getString("SerialNo"),r.getString("ServiceItemNo"),r.getString("CustomerName"),r.getString("CustomerId"),r.getString("Mobile1"),r.getString("Email"),r.getString("EnterpriseId"),r.getString("ParentEnterpriseId"),r.getString("DepartmentId"),r.getString("DepartmentName"),""));
							suggestionsAsset.add(new SuggestGetSet(new_suggestions.get(i).getId(), new_suggestions.get(i).getName(),new_suggestions.get(i).getSerialNo(),new_suggestions.get(i).getServiceItemNo(),new_suggestions.get(i).getCustomerName(),new_suggestions.get(i).getCustomerId(),new_suggestions.get(i).getMobile1(),new_suggestions.get(i).getEmail(),new_suggestions.get(i).getEnterpriseId(),new_suggestions.get(i).getParentEnterpriseId(),new_suggestions.get(i).getDepartmentId(),new_suggestions.get(i).getDepartmentName(),new_suggestions.get(i).getAssetCount()));
						}

						// Now assign the values and count to the FilterResults
						// object
						/*suggestionEmployeeContact.clear();
						suggestionsEmployee.clear();*/
						filterResults.values = suggestionsAsset;
						filterResults.count = suggestionsAsset.size();
					}
					else if(value.equals("Get Employees")){
						List<SuggestGetSet> new_suggestions = jp.getParseJsonWCF(constraint.toString());
						suggestionsEmployee.clear();
						for (int i = 0; i < new_suggestions.size(); i++) {
							suggestionsEmployee.add(new SuggestGetSet(new_suggestions.get(i).getId(), new_suggestions.get(i).getName(),new_suggestions.get(i).getSerialNo(),new_suggestions.get(i).getServiceItemNo(),new_suggestions.get(i).getCustomerName(),new_suggestions.get(i).getCustomerId(),new_suggestions.get(i).getMobile1(),new_suggestions.get(i).getEmail(),new_suggestions.get(i).getEnterpriseId(),new_suggestions.get(i).getParentEnterpriseId(),new_suggestions.get(i).getDepartmentId(),new_suggestions.get(i).getDepartmentName(),new_suggestions.get(i).getAssetCount()));
							Log.e(TAG, "performFilteringinloop: "+suggestionsEmployee.get(i).getCustomerName());
						}

						// Now assign the values and count to the FilterResults
						// object
						/*suggestionEmployeeContact.clear();
						suggestionsAsset.clear();*/
						filterResults.values = suggestionsEmployee;
						filterResults.count = suggestionsEmployee.size();
					}
					else {
                        List<SuggestGetSet> new_suggestions = jp.getParseJsonWCF(constraint.toString());
                        suggestionEmployeeContact.clear();
                        for (int i = 0; i < new_suggestions.size(); i++) {
                            suggestionEmployeeContact.add(new SuggestGetSet(new_suggestions.get(i).getId(), new_suggestions.get(i).getName(),new_suggestions.get(i).getSerialNo(),new_suggestions.get(i).getServiceItemNo(),new_suggestions.get(i).getCustomerName(),new_suggestions.get(i).getCustomerId(),new_suggestions.get(i).getMobile1(),new_suggestions.get(i).getEmail(),new_suggestions.get(i).getEnterpriseId(),new_suggestions.get(i).getParentEnterpriseId(),new_suggestions.get(i).getDepartmentId(),new_suggestions.get(i).getDepartmentName(),new_suggestions.get(i).getAssetCount()));
                            Log.e(TAG, "performFilteringinloop: "+suggestionEmployeeContact.get(i).getName());
                        }
                        // Now assign the values and count to the FilterResults
                        // object
						/*suggestionsAsset.clear();
						suggestionsEmployee.clear();*/
                        filterResults.values = suggestionEmployeeContact;
                        filterResults.count = suggestionEmployeeContact.size();
					}
				}
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence contraint,FilterResults results) {
				if (results != null && results.count > 0) {
					notifyDataSetChanged();
//                    Log.e(TAG, "publishResults: "+suggestionsEmployee.get(0).getName() );
                } else {
					notifyDataSetInvalidated();
				}
			}
		};
		return myFilter;
	}

}
