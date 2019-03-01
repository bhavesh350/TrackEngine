package net.mzi.trackengine.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import net.mzi.trackengine.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Poonam on 8/17/2017.
 */

public class FilterListFragment extends Fragment {
    ImageView ivRemoveFragment,ivFilter;
    ListView listView,listViewSecond;
    ArrayAdapter<String> adapter,adapterSecond;
    SQLiteDatabase sql;
    Cursor cquery;
    String sIsAccepted;
    ArrayList<String> selectedItems = new ArrayList<String>();
    OnDataPass dataPasser;
    public void FilterListFragment(){

    }
    List<String> lStatusName= new ArrayList<String>();
    List<String> lMainStatusName= new ArrayList<String>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.filterlayout, container, false);
        sql = getActivity().openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE,null);
        cquery=sql.rawQuery("select StatusName from Issue_Status", null);
        lMainStatusName.clear();
        lMainStatusName.add("Pending");
        lMainStatusName.add("Attended");
        lMainStatusName.add("Resolved");

        for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
            lStatusName.add(cquery.getString(0));
        }
        ivRemoveFragment= view.findViewById(R.id.removeFragementID);
        listView = view.findViewById(R.id.list);
        listViewSecond = view.findViewById(R.id.listsecond);
        ivFilter = view.findViewById(R.id.filterImage);
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_multiple_choice, lStatusName);
        adapterSecond = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_multiple_choice, lMainStatusName);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listViewSecond.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        listViewSecond.setAdapter(adapterSecond);

        listViewSecond.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i = 0;i<lStatusName.size();i++){
                    listView.setItemChecked(i,false);
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i = 0;i<lMainStatusName.size();i++){
                    listViewSecond.setItemChecked(i,false);
                }
            }
        });
        ivRemoveFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFragment(getActivity());

            }
        });

        ivFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                SparseBooleanArray checkedSecond= listViewSecond.getCheckedItemPositions();
                selectedItems.clear();
                for (int i = 0; i < checkedSecond.size(); i++) {
                    // Item position in adapter
                    int position = checkedSecond.keyAt(i);
                    // Add sport if it is checked i.e.) == TRUE!
                    if (checkedSecond.valueAt(i)) {
                        selectedItems.add(String.valueOf(position+1));
                        sIsAccepted="1";

                        /*cquery = sql.rawQuery("select StatusId from Issue_Status where StatusName = '" + adapterSecond.getItem(position) + "'", null);
                        if (cquery.getCount() > 0) {
                            cquery.moveToFirst();
                            selectedItems.add(cquery.getString(0));
                        }*/
                    }
                }

                for (int i = 0; i < checked.size(); i++) {
                    // Item position in adapter
                    int position = checked.keyAt(i);
                    // Add sport if it is checked i.e.) == TRUE!
                    if (checked.valueAt(i)) {
                        sIsAccepted="0";
                        cquery = sql.rawQuery("select StatusId from Issue_Status where StatusName = '" + adapter.getItem(position) + "'", null);
                        if (cquery.getCount() > 0) {
                            cquery.moveToFirst();
                            selectedItems.add(cquery.getString(0));
                        }
                    }
                }

                passData(selectedItems,sIsAccepted);
                removeFragment(getActivity());
                // Create a bundle object
            }
        });

        return view;
    }
    public interface OnDataPass {
        void onDataPass(List<String> data, String sIsAccepted);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }
    public void passData(List<String> data,String sIsAccepted) {
        dataPasser.onDataPass(data,sIsAccepted);
    }
    public void removeFragment(Context ctx){
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
}

