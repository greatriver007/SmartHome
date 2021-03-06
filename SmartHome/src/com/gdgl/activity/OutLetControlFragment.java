package com.gdgl.activity;
/***
 *智能插座，墙面插座，开关模块
 */
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdgl.manager.CGIManager;
import com.gdgl.manager.Manger;
import com.gdgl.model.DevicesModel;
import com.gdgl.model.SimpleDevicesModel;
import com.gdgl.mydata.Constants;
import com.gdgl.mydata.DataHelper;
import com.gdgl.mydata.Event;
import com.gdgl.mydata.EventType;
import com.gdgl.mydata.SimpleResponseData;
import com.gdgl.mydata.Callback.CallbackResponseType2;
import com.gdgl.smarthome.R;
import com.gdgl.util.MyDlg;

public class OutLetControlFragment extends BaseControlFragment {

	int OnOffImg[];

	public static final int ON = 0;
	public static final int OFF = 1;

	View mView;
	SimpleDevicesModel mDevices;

	TextView txt_devices_name, txt_devices_region;
	RelativeLayout mError;

	ImageView on_off;

	boolean status = false;

	String Ieee = "";

	String ep = "";

	CGIManager mLightManager;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		if (!(activity instanceof UpdateDevice)) {
			throw new IllegalStateException("Activity必须实现SaveDevicesName接口");
		}
		mUpdateDevice = (UpdateDevice) activity;
		super.onAttach(activity);
	}

	private void initstate() {
		// TODO Auto-generated method stub
		if (null != mDevices) {
			if (mDevices.getmOnOffStatus().trim().equals("1")) {
				status = true;
			}
			Ieee = mDevices.getmIeee().trim();
			ep = mDevices.getmEP().trim();
		}
	}

	private void setImagRes(ImageView mSwitch, boolean b) {
		// TODO Auto-generated method stub
		if (b) {
			mSwitch.setImageResource(OnOffImg[ON]);
		} else {
			mSwitch.setImageResource(OnOffImg[OFF]);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle extras = getArguments();
		if (null != extras) {
			mDevices = (SimpleDevicesModel) extras
					.getParcelable(Constants.PASS_OBJECT);
			OnOffImg = extras.getIntArray(Constants.PASS_ONOFFIMG);
		}

		mLightManager = CGIManager.getInstance();
		mLightManager.addObserver(OutLetControlFragment.this);
		initstate();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.on_off_control, null);
		initView();
		return mView;
	}

	private void initView() {
		// TODO Auto-generated method stub
		on_off = (ImageView) mView.findViewById(R.id.devices_on_off);

		txt_devices_name = (TextView) mView.findViewById(R.id.txt_devices_name);
		txt_devices_region = (TextView) mView
				.findViewById(R.id.txt_devices_region);

		txt_devices_name.setText(mDevices.getmUserDefineName().trim());
		txt_devices_region.setText(mDevices.getmDeviceRegion().trim());

		setImagRes(on_off, status);
		
		
		mError=(RelativeLayout)mView.findViewById(R.id.error_message);
		
		on_off.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mError.setVisibility(View.GONE);
				if (null == mDialog) {
					mDialog = MyDlg.createLoadingDialog(
							(Context) getActivity(), "操作正在进行...");
					mDialog.show();
				} else {
					mDialog.show();
				}
				mLightManager.MainsOutLetOperation(mDevices,operatortype.ChangeOnOffSwitchActions,getChangeValue());
//				mLightManager.iASZoneOperationCommon(mDevices);
			}

			
		});
	}
	private int getChangeValue() {
		if (status) {
			return 0x00;
		}else {
			return 0x01;
		}
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mLightManager.deleteObserver(OutLetControlFragment.this);
	}

	@Override
	public void editDevicesName() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Manger observer, Object object) {
		// TODO Auto-generated method stub
		if (null != mDialog) {
			mDialog.dismiss();
			mDialog = null;
		}
		final Event event = (Event) object;
		if (EventType.MAINSOUTLETOPERATION == event.getType()) {
			
			if (event.isSuccess()==true) {
				// data maybe null
				SimpleResponseData data = (SimpleResponseData) event.getData();
				//  refresh UI data
				
				status = !status;
				
				setImagRes(on_off, status);
				mDevices.setmOnOffStatus(status ? "1" : "o");
				ContentValues c = new ContentValues();
				c.put(DevicesModel.ON_OFF_STATUS, status ? "1" : "o");
				mUpdateDevice.updateDevices(mDevices, c);
			}else {
				//if failed,prompt a Toast
				mError.setVisibility(View.VISIBLE);
			}
		}
		if (EventType.ON_OFF_STATUS == event.getType()) {
			if (event.isSuccess()==true) {
				// data maybe null
				CallbackResponseType2 data = (CallbackResponseType2) event.getData();
				List<DevicesModel> mList;
				DataHelper mDh = new DataHelper((Context) getActivity());
				String where = " ieee=? and ep=? ";
				String[] args = {
						mDevices.getmIeee() == null ? "" : mDevices.getmIeee().trim(),
								mDevices.getmEP() == null ? "" : mDevices.getmEP().trim() };
				mList = mDh.queryForDevicesList(mDh.getSQLiteDatabase(),
						DataHelper.DEVICES_TABLE, null, where, args, null, null, null,
						null);
				boolean result=false;
				if(null!=data.getValue()){
					result=data.getValue().trim().equals("1");
					status=result;
					mView.post(new Runnable() {
						
						@Override
						public void run() {
							setImagRes(on_off, status);
						}
					});
				}
				ProcessUpdate(data,mList);
			}else {
				//if failed,prompt a Toast
//				mError.setVisibility(View.VISIBLE);
			}
		}
	}

	public static class operatortype {
		/***
		 * 获取设备类型
		 */
		public static final int GetOnOffSwitchType = 0;
		/***
		 * 获取状态
		 */
		public static final int GetOnOffSwitchActions = 1;
		/***
		 * 当操作类型是2时，para1有以下意义 Param1: switchaction: 0x00: Off 0x01: On 0x02:
		 * Toggle
		 */
		public static final int ChangeOnOffSwitchActions = 2;
	}

}
