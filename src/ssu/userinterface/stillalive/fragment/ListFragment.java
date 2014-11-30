package ssu.userinterface.stillalive.fragment;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ssu.userinterface.common.Config;
import ssu.userinterface.common.HTTPHelper;
import ssu.userinterface.common.TimeChecker;
import ssu.userinterface.common.HTTPHelper.OnResponseListener;
import ssu.userinterface.listview.CustomAdapter;
import ssu.userinterface.listview.Person;
import ssu.userinterface.stillalive.R;
import ssu.userinterface.stillalive.main.SearchFriendsActivity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class ListFragment extends Fragment {
	
	private static final String TAG = "ListFragment";
	
	private ListView friendListView;
	private CustomAdapter customAdapter;

	private ImageButton btnAlive;
	private Button btnSearchFriends;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedistanceState) {
		return inflater.inflate(R.layout.fragment_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		btnAlive = (ImageButton) getView().findViewById(R.id.btnAlive);
		btnAlive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TimeChecker.getInstance().setCurrentTime(getActivity());
				Toast.makeText(getActivity(), "Alive", Toast.LENGTH_SHORT).show();
				getListFromServer();
				updateToServer();
			}
		});
		
		btnSearchFriends = (Button) getView().findViewById(R.id.btn_search_friends);
		btnSearchFriends.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), SearchFriendsActivity.class);
				getActivity().startActivity(intent);
			}
		});
		
		friendListView = (ListView) getView().findViewById(R.id.listView);
		customAdapter = new CustomAdapter();
		//
		
		ArrayList<Person> data = new ArrayList<Person>();
		customAdapter.setData(data);
		friendListView.setAdapter(customAdapter);
		friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Toast.makeText(getActivity(), "click position : "+position, Toast.LENGTH_SHORT).show();
			}
		});
		friendListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(getActivity(), "long click position : "+position, Toast.LENGTH_SHORT).show();
				return true;
			}
		});
	    
	    getListFromServer();
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	private void prevFragment(){
		getActivity().getFragmentManager().beginTransaction().remove(ListFragment.this).commit();
	}
	
	
	//친구 상태 조회
	private void getListFromServer(){
		final ProgressDialog progressDialog = ProgressDialog.show(getActivity(),"","잠시만 기다려 주세요.",true);
		
		SharedPreferences pref = getActivity().getSharedPreferences("default", Context.MODE_PRIVATE);
		String accessToken = pref.getString("accessToken", "");

		Hashtable<String, String> parameters = new Hashtable<String, String>();
		parameters.put("access_token", accessToken);
		HTTPHelper.GET(Config.HOST + "/list", parameters, new OnResponseListener() {
			@Override
			public void OnResponse(String response) {
				Log.i(TAG, response);
				progressDialog.dismiss();
				try {
					JSONObject json = new JSONObject(response);
					if (json.getInt("result") == 1) {
						onSuccess(json);
					}else{
						onFail(json);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	//상태 업데이트
	private void updateToServer(){
		SharedPreferences pref = getActivity().getSharedPreferences("default", Context.MODE_PRIVATE);
		String accessToken = pref.getString("accessToken", "");

		Hashtable<String, String> parameters = new Hashtable<String, String>();
		parameters.put("access_token", accessToken);
		
		Log.i(TAG,"accessToken : "+accessToken);
		
		HTTPHelper.GET(Config.HOST + "/update", parameters, new OnResponseListener() {
			@Override
			public void OnResponse(String response) {
				Log.i(TAG, response);
				try {
					JSONObject json = new JSONObject(response);
					if (json.getInt("result") == 1) {
					}else{
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void onSuccess(JSONObject json) throws JSONException {
		JSONArray data = json.getJSONArray("data");
		ArrayList<Person> persons = new ArrayList<Person>();
		
		int length = data.length();
		for(int i = 0 ; i < length ; ++i){
			//{"id":2,"userID":"loki12","updatedAt":"2014-11-23T07:51:57.000Z"}
			JSONObject item = data.getJSONObject(i);
			int id = item.getInt("id");
			String userID = item.getString("userID");
			String updatedAt = item.getString("updatedAt");
			String dateString = getStringFromTimeString(updatedAt);
			
			Log.i(TAG, dateString);
			
			Person person = new Person();
			person.setId(id);
			person.setName(userID);
			person.setTime(dateString);
			
			persons.add(person);
		}
		
		customAdapter.setData(persons);
		customAdapter.notifyDataSetChanged();
	}

	private void onFail(JSONObject json) {

	}
	
	//yyyy-MM-ddTHH:mm:ss.000Z 
	private String getStringFromTimeString(String dateString){
		String year = dateString.substring(0,4);
		String month = dateString.substring(5,7);
		String day = dateString.substring(8,10); 
		String hour = dateString.substring(11,13);
		String minute = dateString.substring(14,16);
		String second = dateString.substring(17,19);
		return year+"/"+month+"/"+day+"/"+hour+":"+minute+":"+second;
	}
}