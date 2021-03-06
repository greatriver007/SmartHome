package com.gdgl.activity;


import java.util.ArrayList;
import java.util.List;

import com.gdgl.activity.BaseControlFragment.UpdateDevice;
import com.gdgl.activity.DevicesListFragment.refreshData;
import com.gdgl.activity.DevicesListFragment.setData;
import com.gdgl.adapter.AllDevicesAdapter;
import com.gdgl.adapter.AllDevicesAdapter.AddChecked;
import com.gdgl.adapter.DevicesBaseAdapter;
import com.gdgl.adapter.DevicesBaseAdapter.DevicesObserver;
import com.gdgl.manager.CGIManager;
import com.gdgl.model.DevicesModel;
import com.gdgl.model.SimpleDevicesModel;
import com.gdgl.mydata.Constants;
import com.gdgl.mydata.DataHelper;
import com.gdgl.mydata.DataUtil;
import com.gdgl.mydata.getFromSharedPreferences;
import com.gdgl.smarthome.R;
import com.gdgl.util.MyOkCancleDlg;
import com.gdgl.util.UiUtils;
import com.gdgl.util.EditDevicesDlg.EditDialogcallback;
import com.gdgl.util.MyOkCancleDlg.Dialogcallback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/***
 * 区域设备列表
 * @author Trice
 *
 */
public class RegionDevicesActivity extends Activity implements DevicesObserver,
		AddChecked, refreshData, UpdateDevice, EditDialogcallback,
		Dialogcallback,setData{
	public static final String REGION_NAME = "region_name";
	public static final String REGION_ID="region_id";
	
	public static final int INLIST=1;
	public static final int INCONTROL=2;
	public static final int INADD=3;

	private String mRoomname = "";
	private String mRoomid = "";

	//String where = " device_region=? ";

	List<SimpleDevicesModel> mList;

	List<SimpleDevicesModel> mAddList;
	
	List<SimpleDevicesModel> mAddToRegionList;

	DataHelper mDh;

	FragmentManager fragmentManager;

	DevicesListFragment mDevicesListFragment;
	
	AllDevicesFragment mAllDevicesFragment;

	DevicesBaseAdapter mDevicesBaseAdapter;

	TextView mNoDevices,region_name;
	Button mAdd,delete;
	
	DataHelper mDataHelper;
	private SimpleDevicesModel getModel;
	private boolean deleteType=false;
	private boolean isAdd=false;
	
	private int currentState=INLIST;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_region);

		Intent i = getIntent();
		if (null != i) {
			Bundle extras = i.getExtras();
			if (null != extras) {
				mRoomname = extras.getString(REGION_NAME, "");
				mRoomid=Integer.toString(extras.getInt(REGION_ID));
			}
		}
		mDataHelper=new DataHelper(RegionDevicesActivity.this);
		initData();
		initView();
	}
	
	
	private void initRegionDevicesList(){
		mList=null;
		String[] args = { mRoomid };
		String where = " rid=? ";
		if (!mRoomid.trim().equals("")) {
			mDh = new DataHelper(RegionDevicesActivity.this);
			mList = DataUtil.getDevices(RegionDevicesActivity.this, mDh, args,
					where);
		}
	}
	
	private void initAddFragmentDevicesList(){
		mAddList=null;
		List<SimpleDevicesModel> mTempList = DataUtil.getDevices(
				RegionDevicesActivity.this, mDh, null, null);
//		if (null == mList || mList.size() == 0) {
//			mAddList = mTempList;
//		} else {
			mAddList = new ArrayList<SimpleDevicesModel>();
			for (SimpleDevicesModel simpleDevicesModel : mTempList) {
//				if (!isInList(simpleDevicesModel)&&TextUtils.isEmpty(simpleDevicesModel.getmDeviceRegion())) {
				if(simpleDevicesModel.getmRid().equals("-1")) {
						mAddList.add(simpleDevicesModel);
				}
			}
//		}
	}

//	private boolean isInList(SimpleDevicesModel simpleDevicesModel) {
//
//		for (SimpleDevicesModel msimpleDevicesModel : mList) {
//			if (msimpleDevicesModel.getmIeee().equals(
//					simpleDevicesModel.getmIeee())) {
//				return true;
//			}
//		}
//		return false;
//	}
	
	private void initAddToRegionDevicesList(){
		mAddToRegionList=new ArrayList<SimpleDevicesModel>();
	}
	
	private void initView() {
		// TODO Auto-generated method stub
		mNoDevices = (TextView) findViewById(R.id.no_devices);
		mAdd = (Button) findViewById(R.id.add_devices);
		delete = (Button) findViewById(R.id.delete);
		region_name=(TextView) findViewById(R.id.region_name);
		
		region_name.setText(mRoomname);
		
		if(null!=mList && mList.size()>0){
			mNoDevices.setVisibility(View.GONE);
			initDevicesListFragment();
		}
		
		mAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(currentState==INLIST){
					isAdd=true;
					mAdd.setText("添加");
					mAdd.setTextColor(Color.RED);
					//initRegionDevicesList();
					initAddFragmentDevicesList();
					mAllDevicesFragment = new AllDevicesFragment();
					AllDevicesAdapter mAllDevicesAdapter = new AllDevicesAdapter(
							RegionDevicesActivity.this, mAddList,
							RegionDevicesActivity.this);
					mAllDevicesFragment.setAdapter(mAllDevicesAdapter);
					setFragment(mAllDevicesFragment, -1);
					currentState=INADD;
				}else if(currentState==INADD){
					isAdd=false;
					ContentValues c = new ContentValues();
					c.put(DevicesModel.DEVICE_REGION, mRoomname);
					c.put(DevicesModel.R_ID, mRoomid);
					for (SimpleDevicesModel s : mAddToRegionList) {
						CGIManager.getInstance().ModifyDeviceRoomId(s, mRoomid);
						s.setmDeviceRegion(mRoomname);
						s.setmRid(mRoomid);
						updateDevices(s, c);
						mList.add(s);
					}
					mAddToRegionList.clear();
					mDevicesBaseAdapter.setList(mList);
					mDevicesBaseAdapter.notifyDataSetChanged();
					fragmentManager.popBackStack();
					initDevicesListFragment();
					currentState=INLIST;
				}else if(currentState==INCONTROL){
					fragmentManager.popBackStack();
					initDevicesListFragment();
					currentState=INLIST;
				}
			}
		});

		LinearLayout mBack = (LinearLayout) findViewById(R.id.goback);
		mBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MyOkCancleDlg mMyOkCancleDlg = new MyOkCancleDlg(
						RegionDevicesActivity.this);
				mMyOkCancleDlg.setDialogCallback(RegionDevicesActivity.this);
				mMyOkCancleDlg.setContent("确定要删除区域  " + mRoomname + " 吗?");
				mMyOkCancleDlg.show();
				deleteType=true;
			}
		});
		
	}

	private void initData() {
		// TODO Auto-generated method stub
		
		fragmentManager = getFragmentManager();
		initRegionDevicesList();
		initAddToRegionDevicesList();
		mDevicesBaseAdapter = new DevicesBaseAdapter(
				RegionDevicesActivity.this, this);
		mDevicesBaseAdapter.setList(mList);
	}

	private void initDevicesListFragment() {
		// TODO Auto-generated method stub
		mAdd.setText("添加设备");
		mAdd.setTextColor(Color.BLACK);
		if(null==mList || mList.size()==0){
			mNoDevices.setVisibility(View.VISIBLE);
		}else{
			mNoDevices.setVisibility(View.GONE);
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			mDevicesListFragment = new DevicesListFragment();
			Bundle extras = new Bundle();
			extras.putInt(Constants.OPERATOR, DevicesListFragment.WITH_OPERATE);
			mDevicesListFragment.setArguments(extras);
			fragmentTransaction.replace(R.id.devices_control_fragment,
					mDevicesListFragment, "LightsControlFragment");
			mDevicesListFragment.setAdapter(mDevicesBaseAdapter);
			fragmentTransaction.commit();
		}
	}

	@Override
	public void setLayout() {
		// TODO Auto-generated method stub
		mDevicesListFragment.setLayout();
	}

	@Override
	public void deleteDevices(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void AddCheckedDevices(int postion) {
		// TODO Auto-generated method stub
		SimpleDevicesModel s=mAddList.get(postion);
		if(!mAddToRegionList.contains(s)){
			mAddToRegionList.add(s);
		}
	}

	@Override
	public void DeletedCheckedDevices(int postion) {
		// TODO Auto-generated method stub
		SimpleDevicesModel s=mAddList.get(postion);
		if(mAddToRegionList.contains(s)){
			mAddToRegionList.remove(s);
		}
	}

	@Override
	public void refreshListData() {
		// TODO Auto-generated method stub
		new GetDataTask().execute();
	}

	@Override
	public SimpleDevicesModel getDeviceModle(int postion) {
		// TODO Auto-generated method stub
		if (null != mList) {
			return mList.get(postion);
		}
		return null;
	}

	@Override
	public void setFragment(Fragment mFragment, int postion) {
		// TODO Auto-generated method stub
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(R.id.devices_control_fragment, mFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
		
		if(postion!=-1){
			mAdd.setText("返回");
			mAdd.setTextColor(Color.BLACK);
			currentState=INCONTROL;
		}
	}

	@Override
	public void setDevicesId(SimpleDevicesModel simpleDevicesModel) {
		// TODO Auto-generated method stub
		getModel=simpleDevicesModel;
	}

	private class GetDataTask extends
			AsyncTask<Void, Void, List<SimpleDevicesModel>> {

		@Override
		protected List<SimpleDevicesModel> doInBackground(Void... params) {
			// Simulates a background job.
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			return mList;
		}

		@Override
		protected void onPostExecute(List<SimpleDevicesModel> result) {
			super.onPostExecute(result);
			if(currentState==INADD){
				mAllDevicesFragment.stopRefresh();
			}else if(currentState==INLIST){
				mDevicesListFragment.stopRefresh();
			}
			
		}
	}

	@Override
	public void saveDevicesName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean updateDevices(SimpleDevicesModel sd, ContentValues c) {
		// TODO Auto-generated method stub
		String ep=sd.getmEP().trim();
		int result=0;
		String[] eps={ep};
		if(ep.contains(",")){
			eps=ep.trim().split(",");
		} 
		for (String string : eps) {
			String wheres = " ieee = ? and ep = ?";
			String[] args = { sd.getmIeee() ,string };
			SQLiteDatabase mSQLiteDatabase = mDataHelper.getSQLiteDatabase();
			int temp = mDataHelper.update(mSQLiteDatabase,
					DataHelper.DEVICES_TABLE, c, wheres, args);
			result=temp>result?temp:result;
		}
		if (result >= 0) {
			return true;
		}
		return false;
	}


	@Override
	public void saveedit(String ieee, String ep, String name) {
		// TODO Auto-generated method stub
		String where = " ieee = ? ";
		String[] args = { ieee };

		ContentValues c = new ContentValues();
		c.put(DevicesModel.USER_DEFINE_NAME, name);
//		c.put(DevicesModel.DEVICE_REGION, region);
		
		SQLiteDatabase mSQLiteDatabase = mDataHelper.getSQLiteDatabase();
		int result = mDataHelper.update(mSQLiteDatabase,
				DataHelper.DEVICES_TABLE, c, where, args);
		if (result >= 0) {
			initRegionDevicesList();
			mDevicesBaseAdapter.setList(mList);
			mDevicesBaseAdapter.notifyDataSetChanged();
			if(null==mList || mList.size()==0){
				mNoDevices.setVisibility(View.VISIBLE);
			}
		}
	}


	@Override
	public void dialogdo() {
		// TODO Auto-generated method stub
		if(deleteType){
			//删除设备中的区域信息
			String where = " ieee = ? ";
			SQLiteDatabase mSQLiteDatabase = mDataHelper.getSQLiteDatabase();
			for (SimpleDevicesModel s : mList) {
				ContentValues c = new ContentValues();
				c.put(DevicesModel.DEVICE_REGION, "");
				c.put(DevicesModel.R_ID, "-1");
				String[] args = { s.getmIeee() };
				mDataHelper.update(mSQLiteDatabase, DataHelper.DEVICES_TABLE,
						c, where, args);
			}
			//删除所选区域
			CGIManager.getInstance().ZBDeleteRoomDataMainByID(mRoomid);
			String[] strings=new String[] {mRoomid};
			mDataHelper.delete(mSQLiteDatabase, DataHelper.ROOMINFO_TABLE, " room_id = ? ", strings);
			//删除常用中对应的区域名称
			getFromSharedPreferences.setsharedPreferences(RegionDevicesActivity.this);
			
			List<String> mreg=new ArrayList<String>();
 			String comm = getFromSharedPreferences.getCommonUsed();
			if (null != comm && !comm.trim().equals("")) {
				String[] result = comm.split("@@");
				for (String string : result) {
					if (!string.trim().equals("")) {
						mreg.add(string);
					}
				}
			}
			if(mreg.contains(UiUtils.REGION_FLAG+mRoomname)){
				mreg.remove(UiUtils.REGION_FLAG+mRoomname);
				StringBuilder sb=new StringBuilder();
				if(null!=mreg && mreg.size()>0){
					for (String s : mreg) {
						sb.append(s+"@@");
					}
				}else{
					sb.append("");
				}
				getFromSharedPreferences.setCommonUsed(sb.toString());
			}
			this.finish();
		}else{
			String where = " ieee = ? ";
			String[] args = { getModel.getmIeee() };

			ContentValues c = new ContentValues();
			c.put(DevicesModel.DEVICE_REGION, "");
			c.put(DevicesModel.R_ID, "-1");
			SQLiteDatabase mSQLiteDatabase = mDataHelper.getSQLiteDatabase();
			CGIManager.getInstance().ModifyDeviceRoomId(getModel, "-1");
			int result = mDataHelper.update(mSQLiteDatabase,
					DataHelper.DEVICES_TABLE, c, where, args);
			if (result >= 0) {
				initRegionDevicesList();
				mDevicesBaseAdapter.setList(mList);
				mDevicesBaseAdapter.notifyDataSetChanged();
				if(null==mList || mList.size()==0){
					mNoDevices.setVisibility(View.VISIBLE);
				}
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(isAdd){
			isAdd=false;
			fragmentManager.popBackStack();
			initDevicesListFragment();
		}else{
			if(fragmentManager.getBackStackEntryCount()>0){
				fragmentManager.popBackStack();
			}else{
				finish();
			}
		}
	}


	@Override
	public void setdata(List<SimpleDevicesModel> list) {
		// TODO Auto-generated method stub
		
	}
}
