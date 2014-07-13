package com.gdgl.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.gdgl.manager.DeviceManager;
import com.gdgl.manager.CGIManager;
import com.gdgl.manager.Manger;
import com.gdgl.manager.UIListener;
import com.gdgl.model.DevicesModel;
import com.gdgl.model.SimpleDevicesModel;
import com.gdgl.mydata.DataHelper;
import com.gdgl.mydata.DataUtil;
import com.gdgl.mydata.Event;
import com.gdgl.mydata.EventType;
import com.gdgl.mydata.ResponseParamsEndPoint;
import com.gdgl.mydata.getlocalcielist.elserec;
import com.gdgl.smarthome.R;
import com.gdgl.util.CircleProgressBar;
import com.gdgl.util.UiUtils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class JoinNetFragment extends Fragment implements UIListener {

	private View mView;

	RelativeLayout ch_pwd;

	CircleProgressBar cb;

	Button btn_scape, btn_close, btn_look;

	TextView text_result;

	private static final int SCAPE = 1;
	private static final int STOPE = 2;
	private static final int SCAPE_DEVICES = 3;

	private static final int SCAPE_TIME_DURING = 5;

	DeviceManager mDeviceManager;
	// LightManager mLightManager;
	boolean isScape = false;
	boolean finish_scape = false;
	/***
	 * 从服务器获得的所有device列表
	 */
//	ArrayList<ResponseParamsEndPoint> allList;

//	List<DevicesModel> mDevList;
//	List<DevicesModel> mNewDevList;

	/***
	 * 扫描到的设备
	 */
	List<DevicesModel> scapedDeviveList;

	/***
	 * 从数据库中读出的设备列表
	 */
//	List<SimpleDevicesModel> mInnetListFromDB;

//	List<SimpleDevicesModel> mList;

	DataHelper mDH;
	Context c;
	int totlaDevices = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		c = (Context) getActivity();
		mDH = new DataHelper(c);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.add_to_net, null);
		initView();
		return mView;
	}

	private void initView() {
		// TODO Auto-generated method stub
//		mInnetListFromDB = DataUtil.getDevices(c, mDH, null, null, false);
//		mNewDevList = new ArrayList<DevicesModel>();
		scapedDeviveList = new ArrayList<DevicesModel>();
		cb = (CircleProgressBar) mView.findViewById(R.id.seek_time);
		cb.setText("扫描完毕");
		ch_pwd = (RelativeLayout) mView.findViewById(R.id.ch_pwd);
		RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);

		ch_pwd.setLayoutParams(mLayoutParams);

		text_result = (TextView) mView.findViewById(R.id.text_result);

		mDeviceManager = DeviceManager.getInstance();
		mDeviceManager.addObserver(JoinNetFragment.this);
		CGIManager.getInstance().addObserver(this);

		btn_scape = (Button) mView.findViewById(R.id.scape);
		btn_close = (Button) mView.findViewById(R.id.close);
		btn_look = (Button) mView.findViewById(R.id.look);
		btn_look.setEnabled(false);
		// btn_look.setBackgroundColor(Color.DKGRAY);

		btn_scape.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!isScape) {
					isScape = true;
//					if (null != allList) {
//						allList.clear();
//					}
					CGIManager.getInstance().setPermitJoinOn(
							"00137A000000B657");
					text_result.setText("正在扫描...");
					text_result.setVisibility(View.VISIBLE);
					Message msg = Message.obtain();
					msg.what = SCAPE;
					msg.arg1 = 249;
					mHandler.sendMessageDelayed(msg, 1000);
					mHandler.sendEmptyMessage(SCAPE_DEVICES);
				}
			}
		});

		btn_close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(STOPE);
				cb.setProgress(250);
			}
		});

		// btn_look.setEnabled(false);
		btn_look.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ChangeFragment c = (ChangeFragment) getActivity();
				JoinNetDevicesListFragment mJoinNetDevicesListFragment = new JoinNetDevicesListFragment();
				if (null != scapedDeviveList && scapedDeviveList.size() > 0) {
					mJoinNetDevicesListFragment.setList(scapedDeviveList);
				}
				c.setFragment(mJoinNetDevicesListFragment);
			}
		});
	}

//	public boolean isInDB(DevicesModel s) {
//
//		for (SimpleDevicesModel sd : mInnetListFromDB) {
//			if (sd.getmIeee().trim().equals(s.getmIeee().trim())
//					&& sd.getmEP().trim().equals(s.getmEP().trim())) {
//				return true;
//			}
//		}
//		Log.i("new enroll device",
//				"ieee: " + s.getmIeee() + " ep:" + s.getmEP());
//		return false;
//	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mDeviceManager.deleteObserver(JoinNetFragment.this);
		CGIManager.getInstance().deleteObserver(this);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case SCAPE:
				int second = msg.arg1;
				if (second >= 0) {
					cb.setProgress(second);
					Message msg1 = Message.obtain();
					msg1.what = SCAPE;
					msg1.arg1 = second - 1;
					mHandler.sendMessageDelayed(msg1, 1000);
					if (second % SCAPE_TIME_DURING == 0) {
						if (second != 0) {
							mHandler.sendEmptyMessage(SCAPE_DEVICES);
						}
					}
				} else {
					isScape = false;
					finish_scape = true;
					// text_result.setText("未扫描到任何设备");
					// text_result.setVisibility(View.VISIBLE);
					// btn_look.setEnabled(true);
					mHandler.sendEmptyMessageDelayed(STOPE, 1000);
				}

				break;
			case STOPE:
				isScape = false;
				if (!finish_scape) {
					text_result.setVisibility(View.INVISIBLE);
				}
				if (mHandler.hasMessages(SCAPE)) {
					mHandler.removeMessages(SCAPE);
				}
				if (mHandler.hasMessages(SCAPE_DEVICES)) {
					mHandler.removeMessages(SCAPE_DEVICES);
				}
				// getData();
				if (finish_scape) {
					// if (null != mNewDevList && mNewDevList.size() > 0) {
					// updateScapeSuccessful();
					// } else {
					text_result.setText("未扫描到任何设备");
					text_result.setVisibility(View.VISIBLE);
					// }
				}
				break;
			case SCAPE_DEVICES:
				Log.i("", "zgs->begin scape");
				mDeviceManager.getDeviceList(2);
				break;
			default:
				break;
			}
		}

	};

	private void updateScapeSuccessful(ArrayList<DevicesModel> scapedList) {
		text_result.setText("扫描到" + scapedList.size() + "个设备");
		text_result.setVisibility(View.VISIBLE);
		scapedDeviveList = scapedList;
		btn_look.setEnabled(true);
		new InsertTask().execute(scapedDeviveList);
	}

	// private void getData() {
	// // TODO Auto-generated method stub
	// if (null != allList && allList.size() > 0) {
	// mDevList = DataHelper.convertToDevicesModel(allList);
	// }
	// mNewDevList.clear();
	// if (null != mDevList && mDevList.size() > 0) {
	// for (DevicesModel dm : mDevList) {
	// if (!isInDB(dm)) {
	// if (!(dm.getmIeee().trim().equals("00137A0000010264") && dm
	// .getmEP().trim().equals("0A"))) {
	// dm.setmUserDefineName(DataUtil.getDefaultUserDefinname(
	// c, dm.getmModelId()));
	// mNewDevList.add(dm);
	// scapedDeviveList.add(dm);
	// }
	// }
	// }
	// }
	// if (mNewDevList.size() > 0) {
	// totlaDevices += mNewDevList.size();
	// SimpleDevicesModel sd;
	// for (DevicesModel dm : mNewDevList) {
	// sd = new SimpleDevicesModel();
	// sd.setmIeee(dm.getmIeee());
	// sd.setmEP(dm.getmEP());
	// sd.setmDeviceId(Integer.parseInt(dm.getmDeviceId()));
	// sd.setmDeviceRegion(dm.getmDeviceRegion());
	// sd.setmUserDefineName(dm.getmUserDefineName());
	// sd.setmModelId(dm.getmModelId());
	// sd.setmName(dm.getmName());
	// sd.setmNodeENNAme(dm.getmNodeENNAme());
	// sd.setmOnOffLine(dm.getmOnOffLine());
	// mInnetListFromDB.add(sd);
	// }
	// new InsertTask().execute(mNewDevList);
	// }
	//
	// };

	public class InsertTask extends
			AsyncTask<List<DevicesModel>, Integer, Integer> {

		@Override
		protected Integer doInBackground(List<DevicesModel>... params) {
			// TODO Auto-generated method stub
			List<DevicesModel> list = params[0];
			if (null == list || list.size() == 0) {
				return 1;
			}
			mDH.insertDevList(mDH.getSQLiteDatabase(),
					DataHelper.DEVICES_TABLE, null, list);
			return 1;
		}

	}

	public interface ChangeFragment {
		public void setFragment(Fragment f);
	}

	// public boolean isInAllList(ResponseParamsEndPoint responseParamsEndPoint)
	// {
	// if (null == allList || allList.size() == 0) {
	// return false;
	// }
	// for (ResponseParamsEndPoint rp : allList) {
	// if (rp.getDevparam()
	// .getNode()
	// .getIeee()
	// .equals(responseParamsEndPoint.getDevparam().getNode()
	// .getIeee())
	// && rp.getDevparam()
	// .getEp()
	// .equals(responseParamsEndPoint.getDevparam()
	// .getEp())) {
	// return true;
	// }
	// }
	// return false;
	// }

	@Override
	public void update(Manger observer, Object object) {
		// TODO Auto-generated method stub
		final Event event = (Event) object;
		/*
		 * Log.i("zzz",
		 * "zgs-> update EventType.INTITIALDVIVCEDATA == event.getType()=" +
		 * (EventType.INTITIALDVIVCEDATA == event.getType())); if
		 * (EventType.INTITIALDVIVCEDATA == event.getType()) {
		 * ArrayList<ResponseParamsEndPoint> devDataList =
		 * (ArrayList<ResponseParamsEndPoint>) event .getData(); if (null ==
		 * allList || allList.size() == 0) { Log.i("scape devices",
		 * "SCAPDEV-> the first scape,get " + devDataList.size() + " result");
		 * allList = devDataList; } else { for (ResponseParamsEndPoint
		 * responseParamsEndPoint : devDataList) { if
		 * (!isInAllList(responseParamsEndPoint)) { Log.i("scape devices",
		 * "SCAPDEV-> get a devices not in allList,add");
		 * allList.add(responseParamsEndPoint); } } } getData(); if (null !=
		 * mNewDevList && mNewDevList.size() > 0) { updateScapeSuccessful(); }
		 * 
		 * }else
		 */
		if (EventType.SCAPEDDEVICE == event.getType()) {
			ArrayList<DevicesModel> scapedList = (ArrayList<DevicesModel>) event
					.getData();
			updateScapeSuccessful(scapedList);
		} else if (EventType.SETPERMITJOINON == event.getType()) {
			if (!event.isSuccess()) {
				Toast.makeText(getActivity(), "打开组网设备失败！", Toast.LENGTH_SHORT)
						.show();
				mHandler.sendEmptyMessage(STOPE);
				cb.setProgress(250);
			}
		}
	}

}
