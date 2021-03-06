package com.gdgl.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.gdgl.app.ApplicationController;
import com.gdgl.model.DevicesModel;
import com.gdgl.model.SimpleDevicesModel;
import com.gdgl.mydata.DataHelper;
import com.gdgl.mydata.DeviceLearnedParam;
import com.gdgl.mydata.Event;
import com.gdgl.mydata.EventType;
import com.gdgl.mydata.RespondDataEntity;
import com.gdgl.mydata.ResponseDataEntityForStatus;
import com.gdgl.mydata.ResponseParamsEndPoint;
import com.gdgl.mydata.Callback.CallbackBindListDevices;
import com.gdgl.mydata.Callback.CallbackBindListMessage;
import com.gdgl.mydata.Region.GetRoomInfo_response;
import com.gdgl.mydata.Region.RoomData_response_params;
import com.gdgl.mydata.bind.BindResponseData;
import com.gdgl.mydata.binding.BindingDataEntity;
import com.gdgl.mydata.getlocalcielist.LocalIASCIEOperationResponseData;
import com.gdgl.network.StringRequestChina;
import com.gdgl.network.VolleyErrorHelper;
import com.gdgl.network.VolleyOperation;
import com.gdgl.util.NetUtil;
import com.gdgl.util.UiUtils;
import com.google.gson.Gson;

/***
 * 
 * @author justek DeviceID Device Class Device Oparation 0x0007
 *         CombinedInterface 2.1 ZBNode Operation 0x0009 MainsPowerOutlet 2.3
 *         MainsOutLet Operation 0x0101 DimmableLight 2.4 DimmableLight
 *         Operation 0x0100 OnOffLight 2.5 OnOffLight Operation 0x0104
 *         DimmerSwitch 2.6 DimmerSwitch Operation 0x000A DoorLock 2.7 DoorLock
 *         Operation 0x0403 IASWarningDevice 2.8 IASWarningDevice Operation
 *         0x0400 IASCIE 2.9 IASCIE Operation 0x0103 OnOffLightSwitch 2.10
 *         OnOffLightSwitch Operation 0x0002 OnOffOutput 2.11 OnOffOutput
 *         Operation 0x0000 OnOffSwitch 2.12 OnOffSwitch Operation 0x0106
 *         LightSensor 2.13 LightSensor Operation 0x0201 ShadeController 2.14
 *         ShadeController Operation 0x0200 Shade 2.15 Shade Operation 0x0303
 *         Pump 2.16 Pump Operation Reserved LocalIASCIE 2.17 LocalIASCIE
 *         Operation 0x0402 IASZone 2.18 IASZone Operation 0x0302
 *         TemperatureSensor 2.19 TemperatureSensor Operation
 */
public class CGIManager extends Manger {

	private final static String TAG = "CGIManager";

	private static CGIManager instance;

	public static CGIManager getInstance() {
		if (instance == null) {
			instance = new CGIManager();
		}
		return instance;
	}

	/***
	 * addBindData
	 * @param bindtype  0 为正常绑定，1 为虚拟EP绑定
	 * @param devout_ieee
	 * @param devout_ep
	 * @param mDevices
	 * @param cluster_id
	 */
	public void addBindData(String bindtype,String devout_ieee,String devout_ep,
			DevicesModel mDevices,String cluster_id) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("bindtype", bindtype);
		paraMap.put("sourceieee", devout_ieee);
		paraMap.put("sourceep", devout_ep);
		paraMap.put("destieee", mDevices.getmIeee());
		paraMap.put("destep", mDevices.getmEP());
		paraMap.put("clusterid", cluster_id);
		paraMap.put("hasbind", "0");
		paraMap.put("desttype", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);
		Listener<String> responseListener = new Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = UiUtils.formatResponseString(response);
				Log.i("CGIManager bindDevice Response:%n %s", response);
				Gson gson = new Gson();
				BindResponseData statusData = gson.fromJson(
						response.toString(), BindResponseData.class);
				Event event = new Event(EventType.BINDDEVICE, true);
				event.setData(statusData);
				notifyObservers(event);
			}
		};
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "AddBindData.cgi", param);
		Log.i("CGIManager bindDevice Request:%n %s", url);
		StringRequest req = new StringRequest(url, responseListener,
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						String errorString = null;
						if (error != null && error.getMessage() != null) {
							VolleyLog.e("Error: ", error.getMessage());
							errorString = VolleyErrorHelper.getMessage(error,
									ApplicationController.getInstance());
						}
						Event event = new Event(EventType.BINDDEVICE, false);
						event.setData(errorString);
						notifyObservers(event);
					}
				});
		// add the request object to the queue to be executed
		ApplicationController.getInstance().addToRequestQueue(req);
	}

	public void delBindData(String bindtype,String devout_ieee,String devout_ep,
			DevicesModel mDevices,String cluster_id) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("bindtype", bindtype);
		paraMap.put("sourceieee", devout_ieee);
		paraMap.put("sourceep", devout_ep);
		paraMap.put("destieee", mDevices.getmIeee());
		paraMap.put("destep", mDevices.getmEP());
		paraMap.put("clusterid", cluster_id);
		paraMap.put("hasbind", "0");
		paraMap.put("desttype", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "DelBindData.cgi", param);
		simpleVolleyRequset(url, EventType.UNBINDDEVICE);
	}

	public void GetAllBindList() {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "GetAllBindList.cgi", param);
		Listener<String> responseListener = new Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.i("CGIManager getBindList Response:%n %s",response);
				if(response!=null&&response.length()>0) {
					new GetBindingTask().execute(response);
				}
			}
		};

		ErrorListener errorListener = new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				if(error!=null&&error.getMessage()!=null) {
					Log.e("getBindList Error: ", error.getMessage());
				} else {
					Log.e("getBindList Error: ", "Volley error!");
				}
				
			}
		};

		StringRequest req = new StringRequest(url, responseListener,
				errorListener);

		// add the request object to the queue to be executed
		Log.i("request url", url);
		ApplicationController.getInstance().addToRequestQueue(req);
	}

	/***
	 * 1.7 删除node
	 * 
	 * @param model
	 */
	public void deleteNode(String ieee) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", ieee);
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "manageLeaveNode.cgi", param);

		simpleVolleyRequset(url, EventType.DELETENODE);
	}

	/***
	 * 2.1 入网
	 * 
	 * @param model
	 */

	public void setPermitJoinOn(String ieee,int time) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", ieee);
		paraMap.put("second", String.valueOf(time));
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "setPermitJoinOn.cgi", param);

		simpleVolleyRequset(url, EventType.SETPERMITJOINON);
	}
	
	public void setAllPermitJoinOn(int time) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("second", String.valueOf(time));
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "SetAllPermitJoinOn.cgi", param);

		simpleVolleyRequset(url, EventType.SETPERMITJOINON);
	}

	/***
	 * 2.3 插座
	 */
	public void MainsOutLetOperation(SimpleDevicesModel model,
			int operationType, int parem1) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "mainsOutLetOperation.cgi", param);

		simpleVolleyRequset(url, EventType.MAINSOUTLETOPERATION);
	}

	/***
	 * 2.4 DimmableLightOperation ZigBee调光开关
	 * http://192.168.1.184/cgi-bin/rest/network/dimmableLightOperation.cgi?
	 * ieee=1234&ep=12&opera
	 * tortype=1&param1=1&param2=2&param3=3&callback=1234&encodemethod
	 * =NONE&sign=AA A Function Describe:Features provided to the DimmableLight
	 * device: Fully-open or fully-close, toggle and output brightness level
	 * control features; provides information update feature of the brightness
	 * level.
	 */
	public void dimmableLightOperation(SimpleDevicesModel model,
			int operationType, int param1) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", String.valueOf(param1));
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "dimmableLightOperation.cgi", param);

		simpleVolleyRequset(url, EventType.ONOFFOUTPUTOPERATION);
	}

	/***
	 * 2.5 OnOffLight Operation Function Describe:Features provided to the Light
	 * device: on, off or toggle features; provides information update feature
	 * of the current state.
	 * 
	 * http://192.168.1.184/cgi-bin/rest/network/onOffLightOperation.cgi?
	 * ieee=00137A0000008110&ep
	 * =01&operatortype=0&param1=1&param2=2&param3=3&callback
	 * =1234&encodemethod=NON E&sign=AAA
	 * 
	 */
	public void onOffLightOperation() {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A0000010AB5");
		paraMap.put("ep", "0A");
		paraMap.put("operatortype", "2");
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);
		
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "onOffLightOperation.cgi", param);
		EventType type = EventType.ONOFFLIGHTOPERATION;
		simpleVolleyRequset(url, type);

	}

	/***
	 * 2.7.1DoorLockOperationCommon 暂定为门窗感应开关的接口
	 * 
	 * GetLockState 0
	 * 
	 * GetLockType 1
	 * 
	 * GetActuatorEnabled 2
	 * 
	 * GetDoorState 3
	 * 
	 * GetDoorOpenEvents 4
	 * 
	 * GetDoorCloseEvents 5
	 * 
	 * GetOpenPeriod 6
	 * 
	 * LockDoor 7
	 * 
	 * UnLockDoor 8
	 */
	public void doorLockOperationCommon(SimpleDevicesModel model,
			int operationType) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));// 7为锁门，8为解锁
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "doorLockOperation.cgi", param);

		simpleVolleyRequset(url, EventType.DOORLOCKOPERATION);

	}

	/***
	 * 2.8.1IASWarningDeviceOperationCommon 暂定为开始和结束警报
	 * 
	 * StopWarning 0
	 * 
	 * StartWarningBurglar 1
	 * 
	 * StartWarningFire 2
	 * 
	 * StartWarningEmergency 3
	 * 
	 * StartWarningDoorBell 4
	 * 
	 * StartWarningTrouble 5
	 * 
	 * SetDuration 8
	 * 
	 * GetDuration 9
	 * 
	 * GetZoneState 10
	 * 
	 * GetZoneType 11
	 * 
	 * GetZoneStatus 12
	 * 
	 * GetZoneIASCIEAddress 13
	 */
	public void IASWarningDeviceOperationCommon(SimpleDevicesModel model,
			int operationType) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("operatortype", "1");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "iasWarningDeviceOperation.cgi",
				param);

		simpleVolleyRequset(url, EventType.IASWARNINGDEVICOPERATION);
	}

	/***
	 * 2.10 on off light switch operation
	 * http://192.168.1.184/cgi-bin/rest/network/onOffLightSwitchOperation.cgi?
	 * ieee=00137A00000031
	 * 1B&ep=01&operatortype=0&param1=1&param2=2&param3=3&callback
	 * =1234&encodemethod =NONE&sign=AAA
	 */
	public void OnOffLightSwitchOperation(SimpleDevicesModel model,
			int operationType, int param1) {

		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", String.valueOf(param1));
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "onOffLightSwitchOperation.cgi",
				param);
		simpleVolleyRequset(url, EventType.ONOFFLIGHTSWITCHOPERATION);
	}

	/***
	 * 2.11 无线智能阀门开关，开关模块 Function Describe:Features provided to the OnOffOutput
	 * device: ON or OFF and toggle features; provides information update
	 * feature of the current state. Toggling: if a device is in its ��Off��
	 * state it shall enter its ��On�� state. Otherwise, if it is in its ��On��
	 * state it shall enter its ��Off�� state
	 * http://192.168.1.184/cgi-bin/rest/network/onOffOutputOperation.cgi?
	 * ieee=1234&ep=12&operator
	 * type=1&param1=1&param2=2&param3=3&callback=1234&encodemethod
	 * =NONE&sign=AAA
	 * 
	 * TurnOn 0
	 * 
	 * TurnOff 1
	 * 
	 * Toggle 2
	 * 
	 * GetStatus 3
	 */
	public void OnOffOutputOperation(SimpleDevicesModel model, int operationType) {
		// String param =
		// "ieee=00137A0000010AB5&ep=0A&operatortype=0&param1=1&param2=2&param3=3";
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "onOffOutputOperation.cgi", param);

		simpleVolleyRequset(url, EventType.ONOFFOUTPUTOPERATION);

	}

	/***
	 * 2.12OnOffSwitch Operation ZigBee墙面开关
	 */
	public void onOffSwitchOperation() {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A0000010AB5");
		paraMap.put("ep", "01");
		paraMap.put("operatortype", "1");
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "onOffSwitchOperation.cgi", param);

		simpleVolleyRequset(url, EventType.ONOFFSWITCHOPERATION);
	}

	/***
	 * 2.13 LightSensor Operation ZigBee光线感应器 Function Describe:Features
	 * provided to the light sensor device: Feature that read the current light
	 * intensity value; feature that processes and stores information.
	 * 
	 * GetBrightness 0
	 * 
	 * GetMeasuredV alue 1
	 * 
	 * GetMinMeasuredV alue 2
	 * 
	 * GetMaxMeasuredV alue 3
	 * 
	 * GetTolerance 4
	 * 
	 * GetLightSensorType 5
	 * 
	 * ChangeIlluminanceMeasuredVal ueRptTime 6
	 * 
	 * GetIlluminanceMeasuredValueR ptTime 7
	 */
	public void lightSensorOperation(SimpleDevicesModel model, int operationType) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "lightSensorOperation.cgi", param);

		simpleVolleyRequset(url, EventType.LIGHTSENSOROPERATION);

	}

	/***
	 * 2.14ShadeController Operation
	 */
	public void shadeControllerOperation(SimpleDevicesModel model,
			int operationType) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance()
				.getCumstomURL(NetUtil.getInstance().IP,
						"shadeControllerOperation.cgi", param);

		simpleVolleyRequset(url, EventType.SHADECONTROLLEROPERATION);
	}

	/***
	 * 2.15 ShadeOperation窗帘控制
	 */
	public void shadeOperation(SimpleDevicesModel model, int operationType) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "shadeOperation.cgi", param);

		simpleVolleyRequset(url, EventType.SHADEOPERATION);

	}

	/***
	 * 2.17全局布撤防， operatortype=6 撤防 operatortype=7 布防 operatortype=5 全局布防状态
	 * 查看当前全局布防状态（通过Param1的值判断）： 0：全部撤防 1：白天布防模式 2：夜间布防模式 3：全部布防
	 * 
	 */
	public void LocalIASCIEOperation(SimpleDevicesModel model,
			final int operationType) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "localIASCIEOperation.cgi", param);

		Listener<String> responseListener = new Listener<String>() {
			@Override
			public void onResponse(String response) {
				String jsonString = UiUtils.formatResponseString(response);
				if (operationType == 5) {
					Gson gson = new Gson();
					LocalIASCIEOperationResponseData data = gson.fromJson(
							jsonString, LocalIASCIEOperationResponseData.class);
					String status = data.getResponse_params().getParam1().trim();
					Log.i(TAG,
							"LocalIASCIEOperation get status is "
									+ String.valueOf(status));
					Event event = new Event(EventType.LOCALIASCIEOPERATION, true);
					event.setData(status);
					notifyObservers(event);
				}else {
					String status=String.valueOf(operationType);
					Event event = new Event(EventType.LOCALIASCIEOPERATION, true);
					event.setData(status);
					notifyObservers(event);
				}
			}
		};

		ErrorListener errorListener = new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Event event = new Event(EventType.LOCALIASCIEOPERATION, false);
				event.setData(operationType);
				notifyObservers(event);
				// Log.e("Error: ", error.getMessage());
			}
		};

		StringRequest req = new StringRequest(url, responseListener,
				errorListener);

		// add the request object to the queue to be executed
		ApplicationController.getInstance().addToRequestQueue(req);
		// simpleVolleyRequset(url, EventType.LOCALIASCIEOPERATION);
	}

	/***
	 * 2.1安防设备布防LocalIASCIE ByPassZone
	 * 
	 */
	public void LocalIASCIEByPassZone(SimpleDevicesModel model) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("zone_ieee", model.getmIeee());
		paraMap.put("zone_ep", model.getmEP());
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "localIASCIEByPassZone.cgi", param);

		simpleVolleyRequset(url, EventType.LOCALIASCIEBYPASSZONE);
	}

	/***
	 * 2.17安防设备撤防LocalIASCIE ByPassZone
	 * 
	 */
	public void LocalIASCIEUnByPassZone(SimpleDevicesModel model) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("zone_ieee", model.getmIeee());
		paraMap.put("zone_ep", model.getmEP());
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "localIASCIEUnByPassZone.cgi", param);

		simpleVolleyRequset(url, EventType.LOCALIASCIEUNBYPASSZONE);
	}

	/***
	 * 2.18 IASZone Operation Common ZigBee查看状态
	 * 
	 * WriteHeartBeatPeriod 1
	 * 
	 * GetCIEADDR 2
	 * 
	 * GetZoneType 3
	 * 
	 * GetHeartBeatPeriod 4
	 * 
	 * GetBatteryLevel 5
	 * 
	 * GetZoneState 6
	 * 
	 * GetZoneStatus 7
	 * 
	 * SetIRDisableTimeAlarm 9
	 * 
	 * GetIRDisableTime 10
	 */
	public void iASZoneOperationCommon(SimpleDevicesModel model,
			int operationType, int param1) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", String.valueOf(param1));
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		Listener<String> responseListener = new Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = UiUtils.formatResponseString(response);
				Log.i("CGIManager iASZoneOperationCommon Response:%n %s",
						response);
				Gson gson = new Gson();
				ResponseDataEntityForStatus statusData = gson.fromJson(
						response.toString(), ResponseDataEntityForStatus.class);
				Event event = new Event(EventType.IASZONEOPERATION, true);
				event.setData(statusData);
				notifyObservers(event);
			}
		};
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "iasZoneOperation.cgi", param);
		Log.i("CGIManager iASZoneOperationCommon Request:%n %s", url);
		StringRequest req = new StringRequest(url, responseListener,
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						String errorString = null;
						if (error != null && error.getMessage() != null) {
							VolleyLog.e("Error: ", error.getMessage());
							errorString = VolleyErrorHelper.getMessage(error,
									ApplicationController.getInstance());
						}
						Event event = new Event(EventType.IASZONEOPERATION,
								false);
						event.setData(errorString);
						notifyObservers(event);
					}
				});
		// add the request object to the queue to be executed
		ApplicationController.getInstance().addToRequestQueue(req);
	}

	/***
	 * 2.19TemperatureSensorOperation ZigBee室内型温湿度感应器 operationType 0：温度
	 * operationType 1：湿度
	 * 
	 * GetTemperature 0
	 * 
	 * GetHumidity 1
	 * 
	 * GetReportTime 2
	 * 
	 * GetHumidityRptTime 3
	 * 
	 * GetTemperatureRptTime 4
	 * 
	 * GetUltraVioletRptTime 5
	 * 
	 * GetUltraViolet 6
	 * 
	 * ChangeReportTime 7
	 * 
	 * ChangeHumidityRptTime 8
	 * 
	 * ChangeTemperatureRptTime 9
	 * 
	 * ChangeUltravioletRptTime 10
	 * 
	 * GetTemperatureMeasuredValue11
	 */
	public void temperatureSensorOperation(SimpleDevicesModel model,
			int operationType) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "temperatureSensorOperation.cgi",
				param);
		EventType type;
		if (operationType == 0) {
			type = EventType.TEMPERATURESENSOROPERATION;
		} else {
			type = EventType.HUMIDITY;
		}
		simpleVolleyRequset(url, type);
	}

	/***
	 * 2.22RangeExtender Operation ZigBee红外控制器
	 * 
	 * GetHumidity 0
	 */
	public void rangeExtenderOperation(SimpleDevicesModel model,
			int operationType) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "RangeExtender.cgi", param);

		simpleVolleyRequset(url, EventType.RANGEEXTENDER);
	}

	/***
	 * 2.25 IAS ACE 1 心跳
	 * 
	 * WriteHeartBeatPeriod 0
	 * 
	 * RefreshDeviceHeartBeat 1
	 * 
	 * GetDeviceHeartBeat 2
	 * 
	 * RefreshDeviceCIEAddr 3
	 */
	public void iASACE(SimpleDevicesModel model, int operationType) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "IASACE.cgi", param);

		simpleVolleyRequset(url, EventType.IASACE);
	}

	/***
	 * 2.26 RemoteControl ZigBee多键遥控器
	 */
	public void remoteControl(SimpleDevicesModel model, int operationType) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "RemoteControl.cgi", param);

		simpleVolleyRequset(url, EventType.REMOTECONTROL);
	}

	/***
	 * 打开红外学习设备，准备学习 成功返回的数据跟跟BindResponseData一样
	 * 
	 * @param model
	 * @param index
	 * @param operation
	 */
	public void beginLearnIR(SimpleDevicesModel model, int index,
			String operation) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("hadaemonindex", String.valueOf(index));
		paraMap.put("irdisplayname", operation);
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		Listener<String> responseListener = new Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = UiUtils.formatResponseString(response);
				Log.i("CGIManager beginLearnIR Response:%n %s", response);
//				Gson gson = new Gson();
//				BindResponseData statusData = gson.fromJson(response.toString(), BindResponseData.class);
//				Event event = new Event(EventType.BEGINLEARNIR, true);
//				event.setData(statusData);
//				notifyObservers(event);
			}
		};
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "BeginLearnIR.cgi", param);
		Log.i("CGIManager beginLearnIR Request:%n %s", url);
		StringRequest req = new StringRequest(url, responseListener,
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						String errorString = null;
						if (error != null && error.getMessage() != null) {
							VolleyLog.e("Error: ", error.getMessage());
							errorString = VolleyErrorHelper.getMessage(error,
									ApplicationController.getInstance());
						}
						Event event = new Event(EventType.BEGINLEARNIR, false);
						event.setData(errorString);
						notifyObservers(event);
					}
				});
		// add the request object to the queue to be executed
		ApplicationController.getInstance().addToRequestQueue(req);

	}

	/***
	 * 红外控制
	 * 
	 * @param model
	 * @param index
	 * @param operation
	 */
	public void beginApplyIR(SimpleDevicesModel model, int index) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("hadaemonindex", String.valueOf(index));
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		Listener<String> responseListener = new Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = UiUtils.formatResponseString(response);
				Log.i("CGIManager beginApplyIR Response:%n %s", response);
				Gson gson = new Gson();
				BindResponseData statusData = gson.fromJson(
						response.toString(), BindResponseData.class);
				Event event = new Event(EventType.BEGINAPPLYIR, true);
				event.setData(statusData);
				notifyObservers(event);
			}
		};
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "BeginApplyIR.cgi", param);
		Log.i("CGIManager beginApplyIR Request:%n %s", url);
		StringRequest req = new StringRequest(url, responseListener,
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						String errorString = null;
						if (error != null && error.getMessage() != null) {
							VolleyLog.e("Error: ", error.getMessage());
							errorString = VolleyErrorHelper.getMessage(error,
									ApplicationController.getInstance());
						}
						Event event = new Event(EventType.BEGINAPPLYIR, false);
						event.setData(errorString);
						notifyObservers(event);
					}
				});
		// add the request object to the queue to be executed
		ApplicationController.getInstance().addToRequestQueue(req);

	}
	
	public void DeleteIR(SimpleDevicesModel model, int index) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("hadaemonindex", String.valueOf(index));
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);
		
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "DeleteIR.cgi", param);
		Log.i("CGIManager DeleteIR Request:%n %s", url);
		
		StringRequest req = new StringRequest(url, 
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						response = UiUtils.formatResponseString(response);
						Log.i("CGIManager DeleteIR Response:%n %s", response);
						Gson gson = new Gson();
						BindResponseData statusData = gson.fromJson(
								response.toString(), BindResponseData.class);
						Event event = new Event(EventType.DELETEIR, true);
						event.setData(statusData);
						notifyObservers(event);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						String errorString = null;
						if (error != null && error.getMessage() != null) {
							VolleyLog.e("Error: ", error.getMessage());
							errorString = VolleyErrorHelper.getMessage(error,
									ApplicationController.getInstance());
						}
						Event event = new Event(EventType.DELETEIR, false);
						event.setData(errorString);
						notifyObservers(event);
					}
				});
		// add the request object to the queue to be executed
		ApplicationController.getInstance().addToRequestQueue(req);
	}

	public void getDeviceLearnedIRDataInformation(SimpleDevicesModel model) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);

		Listener<String> responseListener = new Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.i("CGIManager GetDeviceLearnedIRDataInformation Response:%n %s",
						response);
				new GetDeviceLearnedTask().execute(response);
			}
		};
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP,
				"GetDeviceLearnedIRDataInformation.cgi", param);
		Log.i("CGIManager GetDeviceLearnedIRDataInformation Request:%n %s",
				url);
		StringRequest req = new StringRequest(url, responseListener,
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						String errorString = null;
						if (error != null && error.getMessage() != null) {
							VolleyLog.e("Error: ", error.getMessage());
							errorString = VolleyErrorHelper.getMessage(error,
									ApplicationController.getInstance());
						}
						Event event = new Event(EventType.GETDEVICELEARNED, false);
						event.setData(errorString);
						notifyObservers(event);
					}
				});
		// add the request object to the queue to be executed
		ApplicationController.getInstance().addToRequestQueue(req);
	}

	/***
	 * 区域设置：得到房间信息
	 */
	public void GetAllRoomInfo() {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);
		
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "getAllRoomInfo.cgi",param);
		
		StringRequestChina req = new StringRequestChina(url, 
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.i("CGIManager GetRoomInfo Response:%n %s", response);
						new GetAllRoomInfoTask().execute(response);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						String errorString = null;
						if (error != null && error.getMessage() != null) {
							VolleyLog.e("Error: ", error.getMessage());
							errorString = VolleyErrorHelper.getMessage(error,
									ApplicationController.getInstance());
						}
						Event event = new Event(EventType.GETALLROOM, false);
						event.setData(errorString);
						notifyObservers(event);
					}
				});
		// add the request object to the queue to be executed
		ApplicationController.getInstance().addToRequestQueue(req);
	}
	
	/***
	 * 获取指定房间的设备EP信息
	 * @param rid
	 */
	public void GetEPByRoomIndex(String rid) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("rid", rid);
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);
		
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "getEPByRoomIndex.cgi",param);
		
		StringRequest req = new StringRequest(url, 
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						new GetEPbyRoomIndexTask().execute(response);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
//						String errorString = null;
//						if (error != null && error.getMessage() != null) {
//							VolleyLog.e("Error: ", error.getMessage());
//							errorString = VolleyErrorHelper.getMessage(error,
//									ApplicationController.getInstance());
//						}
//						Event event = new Event(EventType.DELETEIR, false);
//						event.setData(errorString);
//						notifyObservers(event);
					}
				});
		// add the request object to the queue to be executed
		ApplicationController.getInstance().addToRequestQueue(req);
	}
	
	
	/**
	 * 添加房间
	 * @param rid
	 * @param roomname
	 * @param roompic
	 */
	public void ZBAddRoomDataMain(String rid, String roomname, String roompic) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("rid", rid);
		paraMap.put("roomname", roomname);
		paraMap.put("roompic", roompic);
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);
		
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "zbAddRoomDataMain.cgi",param);
		StringRequest req = new StringRequest(url, 
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						response = UiUtils.formatResponseString(response);
						Log.i("CGIManager AddRoomData Response:%n %s", response);
						Gson gson = new Gson();
						RoomData_response_params data = gson.fromJson(
								response.toString(), RoomData_response_params.class);
						String status=data.getstatus();
						Event event = new Event(EventType.ROOMDATAMAIN, true);
						event.setData(status);
						notifyObservers(event);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						String errorString = null;
						if (error != null && error.getMessage() != null) {
							VolleyLog.e("Error: ", error.getMessage());
							errorString = VolleyErrorHelper.getMessage(error,
									ApplicationController.getInstance());
						}
						Event event = new Event(EventType.ROOMDATAMAIN, false);
						event.setData(errorString);
						notifyObservers(event);
					}
				}) {
			@Override
			public Map<String, String> getHeaders()
					throws AuthFailureError {
				// TODO Auto-generated method stub
				HashMap<String, String> headers = new HashMap<String, String>();  
				//headers.put("Charset", "UTF-8");  
				headers.put("Content-Type", "application/json; charset=UTF-8");  
				headers.put("Accept-Encoding", "gzip,deflate,sdch");  
				headers.put("Accept-Language", "zh-CN,zh;q=0.8");
				return headers;  
			}
		};
		// add the request object to the queue to be executed
		ApplicationController.getInstance().addToRequestQueue(req);
	}
	
	/***
	 * 删除指定房间
	 * @param rid
	 */
	public void ZBDeleteRoomDataMainByID(String rid) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("rid", rid);
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);
		
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "zbDeleteRoomDataMainByID.cgi",param);
		
		StringRequest req = new StringRequest(url, 
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						response = UiUtils.formatResponseString(response);
						Log.i("CGIManager DeleteRoomData Response:%n %s", response);
						Gson gson = new Gson();
						RoomData_response_params data = gson.fromJson(
								response.toString(), RoomData_response_params.class);
						String status=data.getstatus();
						Event event = new Event(EventType.ROOMDATAMAIN, true);
						event.setData(status);
						notifyObservers(event);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						String errorString = null;
						if (error != null && error.getMessage() != null) {
							VolleyLog.e("Error: ", error.getMessage());
							errorString = VolleyErrorHelper.getMessage(error,
									ApplicationController.getInstance());
						}
						Event event = new Event(EventType.ROOMDATAMAIN, false);
						event.setData(errorString);
						notifyObservers(event);
					}
				});
		// add the request object to the queue to be executed
		ApplicationController.getInstance().addToRequestQueue(req);
	}
	
	/**
	 * 修改设备的room id
	 * @param model
	 * @param new_roomid
	 */
	public void ModifyDeviceRoomId(SimpleDevicesModel model, String new_roomid) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("new_roomid", new_roomid);
		
		paraMap.put("callback", "1234");
		paraMap.put("encodemethod", "NONE");
		paraMap.put("sign", "AAA");
		String param = hashMap2ParamString(paraMap);
		
		String url = NetUtil.getInstance().getCumstomURL(
				NetUtil.getInstance().IP, "ModifyDeviceRoomId.cgi",param);
		
		StringRequest req = new StringRequest(url, 
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						response = UiUtils.formatResponseString(response);
						Log.i("CGIManager ModifyDeviceRoomId Response:%n %s", response);
						Gson gson = new Gson();
						RoomData_response_params data = gson.fromJson(
								response.toString(), RoomData_response_params.class);
						String status=data.getstatus();
						Event event = new Event(EventType.MODIFYDEVICEROOMID, true);
						event.setData(status);
						notifyObservers(event);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						String errorString = null;
						if (error != null && error.getMessage() != null) {
							VolleyLog.e("Error: ", error.getMessage());
							errorString = VolleyErrorHelper.getMessage(error,
									ApplicationController.getInstance());
						}
						Event event = new Event(EventType.MODIFYDEVICEROOMID, false);
						event.setData(errorString);
						notifyObservers(event);
					}
				});
		// add the request object to the queue to be executed
		ApplicationController.getInstance().addToRequestQueue(req);
	}
	
	class GetBindingTask extends AsyncTask<String, Object, Object> {
		@Override
		protected Object doInBackground(String... params) {
			BindingDataEntity data = VolleyOperation
					.handleBindingString(params[0]);
			if(data!=null) {
				ArrayList<CallbackBindListMessage> bindingInfo=data.getResponse_paramsList();
				
				DataHelper mDateHelper = new DataHelper(
						ApplicationController.getInstance());
				SQLiteDatabase mSQLiteDatabase = mDateHelper.getSQLiteDatabase();
				
				mSQLiteDatabase.beginTransaction();
				try {
					mSQLiteDatabase.delete(DataHelper.BIND_TABLE, null, null);
					for(CallbackBindListMessage bindingParam:bindingInfo) {
						ArrayList<CallbackBindListDevices> mBindedDevicesList= bindingParam.getList();
						if(mBindedDevicesList!=null&&mBindedDevicesList.size()>0) {
							for(CallbackBindListDevices bindingDivice:mBindedDevicesList) {
								ContentValues c = new ContentValues();
								c.put(BindingDataEntity.DEVOUT_IEEE,bindingParam.getIeee());
								c.put(BindingDataEntity.DEVOUT_EP,bindingParam.getEp());
								c.put(BindingDataEntity.DEVIN_IEEE,bindingDivice.getIeee());
								c.put(BindingDataEntity.DEVIN_EP,bindingDivice.getEp());
								c.put(BindingDataEntity.CLUSTER,bindingDivice.getCid());
								
								mSQLiteDatabase.insert(DataHelper.BIND_TABLE, null, c);
							}
						}
					}
					mSQLiteDatabase.setTransactionSuccessful();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				} finally {
					mSQLiteDatabase.endTransaction();
					mSQLiteDatabase.close();
				}
			}
			return 1;
		}

		@Override
		protected void onPostExecute(Object result) {
			
		}
	}
	
	class GetDeviceLearnedTask extends AsyncTask<String, Object, Object> {

		@Override
		protected Object doInBackground(String... params) {
			RespondDataEntity<DeviceLearnedParam> dataEntity = VolleyOperation
					.handleDeviceLearnedString(params[0]);
			return dataEntity;
		}
		@Override
		protected void onPostExecute(Object result) {
			
			Event event = new Event(EventType.GETDEVICELEARNED, true);
			event.setData(result);
			notifyObservers(event);
		}
	}
	
	class GetAllRoomInfoTask extends AsyncTask<String, Object, Object> {
		@Override
		protected Object doInBackground(String... params) {
			RespondDataEntity<GetRoomInfo_response> data = VolleyOperation.handleRoomInfoString(params[0]);
			ArrayList<GetRoomInfo_response> roomList = data.getResponseparamList();
			
			DataHelper mDateHelper = new DataHelper(
					ApplicationController.getInstance());
			SQLiteDatabase mSQLiteDatabase = mDateHelper.getSQLiteDatabase();
			
			mDateHelper.emptyTable(mSQLiteDatabase, DataHelper.ROOMINFO_TABLE);
			mDateHelper.insertRoomInfoList(mSQLiteDatabase, DataHelper.ROOMINFO_TABLE, null, roomList);
			
			return roomList;
		}

		@Override
		protected void onPostExecute(Object result) {
//			Event event = new Event(EventType.GETALLROOM, true);
//			event.setData(result);
//			notifyObservers(event);
		}
	}
	
	class GetEPbyRoomIndexTask extends AsyncTask<String, Object, Object> {
		@Override
		protected Object doInBackground(String... params) {
			RespondDataEntity<ResponseParamsEndPoint> data = VolleyOperation
					.handleEndPointString(params[0]);
			ArrayList<ResponseParamsEndPoint> devDataList = data
					.getResponseparamList();

//			DataHelper mDateHelper = new DataHelper(
//					ApplicationController.getInstance());
//			SQLiteDatabase mSQLiteDatabase = mDateHelper.getSQLiteDatabase();
//			List<DevicesModel> mList = mDateHelper.queryForDevicesList(
//					mSQLiteDatabase, DataHelper.DEVICES_TABLE, null, null,
//					null, null, null, null, null);
//			
//			mDateHelper.emptyTable(mSQLiteDatabase,DataHelper.DEVICES_TABLE);
//			mDateHelper.insertEndPointList(mSQLiteDatabase,DataHelper.DEVICES_TABLE, null, devDataList);

			// mDateHelper.close(mSQLiteDatabase);
			// [TODO]transfer to SimpleDevicesModel
			return devDataList;
		}

		@Override
		protected void onPostExecute(Object result) {
			Event event = new Event(EventType.GETEPBYROOMINDEX, true);
			event.setData(result);
			notifyObservers(event);
		}

	}
}
