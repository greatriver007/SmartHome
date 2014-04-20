package com.gdgl.manager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.R.integer;

import com.gdgl.activity.SeekLightsControlFragment;
import com.gdgl.model.SimpleDevicesModel;
import com.gdgl.mydata.EventType;
import com.gdgl.util.NetUtil;

/***
 * 
 * @author justek
 *DeviceID
Device Class
Device Oparation
0x0007
CombinedInterface
2.1 ZBNode Operation
0x0009
MainsPowerOutlet
2.3 MainsOutLet Operation
0x0101
DimmableLight
2.4 DimmableLight Operation
0x0100
OnOffLight
2.5 OnOffLight Operation
0x0104
DimmerSwitch
2.6 DimmerSwitch Operation
0x000A
DoorLock
2.7 DoorLock Operation
0x0403
IASWarningDevice
2.8 IASWarningDevice Operation
0x0400
IASCIE
2.9 IASCIE Operation
0x0103
OnOffLightSwitch
2.10 OnOffLightSwitch Operation
0x0002
OnOffOutput
2.11 OnOffOutput Operation
0x0000
OnOffSwitch
2.12 OnOffSwitch Operation
0x0106
LightSensor
2.13 LightSensor Operation
0x0201
ShadeController
2.14 ShadeController Operation
0x0200
Shade
2.15 Shade Operation
0x0303
Pump
2.16 Pump Operation
Reserved
LocalIASCIE
2.17 LocalIASCIE Operation
0x0402
IASZone
2.18 IASZone Operation
0x0302
TemperatureSensor
2.19 TemperatureSensor Operation
 */
public class LightManager extends Manger {

	private static LightManager instance;

	public static LightManager getInstance() {
		if (instance == null) {
			instance = new LightManager();
		}
		return instance;
	}
	/***
	 * 2.3 插座
	 */
	public void MainsOutLetOperation(SimpleDevicesModel model,int operationType,int parem1)
	{
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				"mainsOutLetOperation.cgi", param);

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
	public void dimmableLightOperation(SimpleDevicesModel model,int operationType) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				"dimmableLightOperation.cgi", param);

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

		String param = "ieee=00137A0000010AB5&ep=0A&operatortype=2&param1=1&param2=2&param3=3";
		String url = NetUtil.getInstance().getCumstomURL(
				"onOffLightSwitchOperation.cgi", param);
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
	public void doorLockOperationCommon() {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A0000011598");
		paraMap.put("ep", "01");
		paraMap.put("operatortype", "7");// 7为锁门，8为解锁
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				"doorLockOperation.cgi", param);

		simpleVolleyRequset(url, EventType.DOORLOCKOPERATION);

	}

	/***
	 *  2.8.1IASWarningDeviceOperationCommon 暂定为开始和结束警报
	 * 
StopWarning 0

StartWarningBurglar 1

StartWarningFire 2

StartWarningEmergency 3

StartWarningDoorBell 4

StartWarningTrouble 5

SetDuration 8

GetDuration 9

GetZoneState 10

GetZoneType 11

GetZoneStatus 12

GetZoneIASCIEAddress 13
	 */
	public void IASWarningDeviceOperationCommon() {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A0000011949");
		paraMap.put("ep", "01");
		paraMap.put("operatortype", "7");// 7为锁门，8为解锁
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("operatortype", "1");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				"iasWarningDeviceOperation.cgi", param);

		simpleVolleyRequset(url, EventType.IASWARNINGDEVICOPERATION);
	}

	/***
	 * 2.10 on off light switch operation
	 * http://192.168.1.184/cgi-bin/rest/network/onOffLightSwitchOperation.cgi?
	 * ieee=00137A00000031
	 * 1B&ep=01&operatortype=0&param1=1&param2=2&param3=3&callback
	 * =1234&encodemethod =NONE&sign=AAA
	 */
	public void OnOffLightSwitchOperation(SimpleDevicesModel model,int operationType,int param1) {
		
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", model.getmIeee());
		paraMap.put("ep", model.getmEP());
		paraMap.put("operatortype", String.valueOf(operationType));
		paraMap.put("param1", String.valueOf(param1));
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				"onOffLightSwitchOperation.cgi", param);
		simpleVolleyRequset(url, EventType.ONOFFLIGHTSWITCHOPERATION);
	}

	/***
	 * 2.11 无线智能阀门开关 Function Describe:Features provided to the OnOffOutput
	 * device: ON or OFF and toggle features; provides information update
	 * feature of the current state. Toggling: if a device is in its ��Off��
	 * state it shall enter its ��On�� state. Otherwise, if it is in its ��On��
	 * state it shall enter its ��Off�� state
	 * http://192.168.1.184/cgi-bin/rest/network/onOffOutputOperation.cgi?
	 * ieee=1234&ep=12&operator
	 * type=1&param1=1&param2=2&param3=3&callback=1234&encodemethod
	 * =NONE&sign=AAA
	 *
TurnOn 0

TurnOff 1

Toggle 2

GetStatus 3
	 */
	public void OnOffOutputOperation() {
		// String param =
		// "ieee=00137A0000010AB5&ep=0A&operatortype=0&param1=1&param2=2&param3=3";
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A0000010AB5");
		paraMap.put("ep", "0A");
		paraMap.put("operatortype", "0");
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				"onOffOutputOperation.cgi", param);

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
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				"onOffSwitchOperation.cgi", param);

		simpleVolleyRequset(url, EventType.ONOFFSWITCHOPERATION);
	}

	/***
	 * 2.13 LightSensor Operation ZigBee光线感应器 Function Describe:Features
	 * provided to the light sensor device: Feature that read the current light
	 * intensity value; feature that processes and stores information.
	 * 
GetBrightness 0

GetMeasuredV alue 1

GetMinMeasuredV alue 2

GetMaxMeasuredV alue 3

GetTolerance 4

GetLightSensorType 5

ChangeIlluminanceMeasuredVal ueRptTime 6

GetIlluminanceMeasuredValueR ptTime 7
	 */
	public void lightSensorOperation() {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A000001181F");
		paraMap.put("ep", "0A");
		paraMap.put("operatortype", "0");
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				"onOffOutputOperation.cgi", param);

		simpleVolleyRequset(url, EventType.ONOFFOUTPUTOPERATION);

	}

	/***
	 * 2.14ShadeController Operation
	 */
	public void shadeControllerOperation(SimpleDevicesModel model) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A0000010516");
		paraMap.put("ep", "1");
		paraMap.put("operatortype", "1");
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				"shadeControllerOperation.cgi", param);

		simpleVolleyRequset(url, EventType.SHADEcONTROLLEROPERATION);
	}

	/***
	 * 2.15 ShadeOperation
	 */
	public void shadeOperation(SimpleDevicesModel model) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A0000010516");
		paraMap.put("ep", "1");
		paraMap.put("operatortype", "1");
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL("shadeOperation.cgi",
				param);

		simpleVolleyRequset(url, EventType.SHADEOPERATION);

	}

	/***
	 * 2.18 IASZone Operation Common ZigBee动作感应器
	 * 
WriteHeartBeatPeriod 1

GetCIEADDR 2

GetZoneType 3

GetHeartBeatPeriod 4

GetBatteryLevel 5

GetZoneState 6

GetZoneStatus 7

SetIRDisableTimeAlarm 9

GetIRDisableTime 10
	 */
	public void iASZoneOperationCommon(SimpleDevicesModel model) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A00000120E3");
		paraMap.put("ep", "12");
		paraMap.put("operatortype", "01");
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				"iasZoneOperation.cgi", param);

		simpleVolleyRequset(url, EventType.IASZONEOPERATION);
	}

	/***
	 * 2.19TemperatureSensorOperation ZigBee室内型温湿度感应器
	 * 
GetTemperature 0

GetHumidity 1

GetReportTime 2

GetHumidityRptTime 3

GetTemperatureRptTime 4

GetUltraVioletRptTime 5

GetUltraViolet 6

ChangeReportTime 7

ChangeHumidityRptTime 8

ChangeTemperatureRptTime 9

ChangeUltravioletRptTime 10

GetTemperatureMeasuredValue11
	 */
	public void temperatureSensorOperation(SimpleDevicesModel model) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A00000121C2");
		paraMap.put("ep", "0A");
		paraMap.put("operatortype", "01");
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL(
				"temperatureSensorOperation.cgi", param);

		simpleVolleyRequset(url, EventType.TEMPERATURESENSOROPERATION);
	}

	/***
	 * 2.22RangeExtender Operation ZigBee红外控制器
	 * 
GetHumidity 0
	 */
	public void rangeExtenderOperation(SimpleDevicesModel model) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A0000010148");
		paraMap.put("ep", "0A");
		paraMap.put("operatortype", "01");
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL("RangeExtender.cgi",
				param);

		simpleVolleyRequset(url, EventType.RANGEEXTENDER);
	}

	/***
	 * 2.25 IAS ACE 1 ZigBee门铃按键
	 *
WriteHeartBeatPeriod 0

RefreshDeviceHeartBeat 1

GetDeviceHeartBeat 2

RefreshDeviceCIEAddr 3
	 */
	public void iASACE(SimpleDevicesModel model) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A0000011F8C");
		paraMap.put("ep", "0A");
		paraMap.put("operatortype", "01");
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL("IASACE.cgi", param);

		simpleVolleyRequset(url, EventType.IASACE);
	}

	/***
	 * 2.26 RemoteControl ZigBee多键遥控器
	 */
	public void remoteControl(SimpleDevicesModel model) {
		HashMap<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("ieee", "00137A0000010264");
		paraMap.put("ep", "0A");
		paraMap.put("operatortype", "01");
		paraMap.put("param1", "1");
		paraMap.put("param2", "2");
		paraMap.put("param3", "3");
		String param = hashMap2ParamString(paraMap);

		String url = NetUtil.getInstance().getCumstomURL("RemoteControl.cgi",
				param);

		simpleVolleyRequset(url, EventType.REMOTECONTROL);
	}

}
