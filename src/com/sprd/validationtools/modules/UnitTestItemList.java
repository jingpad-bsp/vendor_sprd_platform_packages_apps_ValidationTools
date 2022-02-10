package com.sprd.validationtools.modules;

import android.content.Context;

import com.sprd.validationtools.Const;
import com.sprd.validationtools.itemstest.ai.AITest;
import com.sprd.validationtools.itemstest.audio.HeadSetTest;
import com.sprd.validationtools.itemstest.audio.MelodyTest;
import com.sprd.validationtools.itemstest.audio.PhoneLoopBackTest;
import com.sprd.validationtools.itemstest.audio.SmartPATest;
import com.sprd.validationtools.itemstest.audio.SoundTriggerTestActivity;
import com.sprd.validationtools.itemstest.audio.EarpieceTestActivity;
import com.sprd.validationtools.itemstest.audio.SpeakerTestActivity;
import com.sprd.validationtools.itemstest.audio.VibrateTestActivity;
import com.sprd.validationtools.itemstest.backlight.BackLightTest;
import com.sprd.validationtools.itemstest.bt.BluetoothTestActivity;
import com.sprd.validationtools.itemstest.camera.CameraTestActivity;
import com.sprd.validationtools.itemstest.camera.ColorTemperatureTestActivty;
import com.sprd.validationtools.itemstest.camera.FrontCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.FrontSecondaryCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.MacroLensCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.SecondaryCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.SpwCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.TofCalibrationTest;
import com.sprd.validationtools.itemstest.camera.CameraFlashTestActivity;
import com.sprd.validationtools.itemstest.charger.ChargerTest;
import com.sprd.validationtools.itemstest.current.CurrentTest;
import com.sprd.validationtools.itemstest.fingerprint.FingerprintTestActivity;
import com.sprd.validationtools.itemstest.fm.FMTest;
import com.sprd.validationtools.itemstest.gps.GpsTestActivity;
import com.sprd.validationtools.itemstest.keypad.KeyTestActivity;
import com.sprd.validationtools.itemstest.keypad.PhysicalKeyboardTestActivity;
import com.sprd.validationtools.itemstest.lcd.ScreenColorTest;
import com.sprd.validationtools.itemstest.led.BlueLightTest;
import com.sprd.validationtools.itemstest.led.GreenLightTest;
import com.sprd.validationtools.itemstest.led.RedLightTest;
import com.sprd.validationtools.itemstest.nfc.NFCTestActivity;
import com.sprd.validationtools.itemstest.otg.OTGTest;
import com.sprd.validationtools.itemstest.encryptionChip.EncryptionChip;
import com.sprd.validationtools.itemstest.rtc.RTCTest;
import com.sprd.validationtools.itemstest.sensor.ASensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.LightSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.CompassTestActivity;
import com.sprd.validationtools.itemstest.sensor.GSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.ASensorTestActivity;
import com.sprd.validationtools.itemstest.sensor.GyroscopeTestActivity;
import com.sprd.validationtools.itemstest.sensor.LsensorNoiseTestActivity;
import com.sprd.validationtools.itemstest.sensor.MSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.MagneticTestActivity;
import com.sprd.validationtools.itemstest.sensor.PressureTestActivity;
import com.sprd.validationtools.itemstest.sensor.ProxSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.PsensorLightTestActivity;
import com.sprd.validationtools.itemstest.sensor.PsensorTestActivity;
import com.sprd.validationtools.itemstest.sensor.LsensorTestActivity;
import com.sprd.validationtools.itemstest.sensor.ASensorSlopeTestActivity;
import com.sprd.validationtools.itemstest.storage.SDCardTest;
import com.sprd.validationtools.itemstest.storage.RomActivity;
import com.sprd.validationtools.itemstest.sysinfo.RFCALITest;
import com.sprd.validationtools.itemstest.sysinfo.SystemVersionTest;
import com.sprd.validationtools.itemstest.telephony.PhoneCallTestActivity;
import com.sprd.validationtools.itemstest.telephony.SIMCardTestActivity;
import com.sprd.validationtools.itemstest.tp.MutiTouchTest;
import com.sprd.validationtools.itemstest.tp.SingleTouchPointTest;
import com.sprd.validationtools.itemstest.tp.SingleTouchPointTestForWacom;
import com.sprd.validationtools.itemstest.tp.TPRawdataTest;
import com.sprd.validationtools.itemstest.wifi.WifiTestActivity;
import com.sprd.validationtools.itemstest.hall.HallTestActivity;

public class UnitTestItemList extends TestItemList {
    private static final String TAG = "UnitTestItemList";

    /**
     * This array define the order of test items.
     */
    private static final String[] FILTER_CLASS_NAMES = {
			HallTestActivity.class.getName(),
            SDCardTest.class.getName(), SIMCardTestActivity.class.getName(),
            TofCalibrationTest.class.getName(),
            ColorTemperatureTestActivty.class.getName(),
            SystemVersionTest.class.getName(), RomActivity.class.getName(),RFCALITest.class.getName(),
            RTCTest.class.getName(), BackLightTest.class.getName(),
            AITest.class.getName(),
            ScreenColorTest.class.getName(),
            //SmartPATest.class.getName(),
            CameraFlashTestActivity.class.getName(),
            TPRawdataTest.class.getName(),			
            SingleTouchPointTest.class.getName(),
            SingleTouchPointTestForWacom.class.getName(),
            MutiTouchTest.class.getName(), 
            //MelodyTest.class.getName(),
            VibrateTestActivity.class.getName(),
            PhoneLoopBackTest.class.getName(),
            //PhoneCallTestActivity.class.getName(),
            //EarpieceTestActivity.class.getName(),
            SpeakerTestActivity.class.getName(),
            //ProxSensorCalibrationActivity.class.getName(),
            //PsensorLightTestActivity.class.getName(),
            PsensorTestActivity.class.getName(),
            ASensorCalibrationActivity.class.getName(),
            ASensorTestActivity.class.getName(),
            //ASensorSlopeTestActivity.class.getName(),
            MSensorCalibrationActivity.class.getName(),
            MagneticTestActivity.class.getName(),
            GSensorCalibrationActivity.class.getName(),
            GyroscopeTestActivity.class.getName(),
            //LightSensorCalibrationActivity.class.getName(),
            LsensorTestActivity.class.getName(),
            LsensorNoiseTestActivity.class.getName(),
            CompassTestActivity.class.getName(),
            PressureTestActivity.class.getName(),
            NFCTestActivity.class.getName(),
            CameraTestActivity.class.getName(),
            FrontCameraTestActivity.class.getName(),
            //SecondaryCameraTestActivity.class.getName(),
            //FrontSecondaryCameraTestActivity.class.getName(),
            //SpwCameraTestActivity.class.getName(),
            //MacroLensCameraTestActivity.class.getName(),
            FingerprintTestActivity.class.getName(),
            KeyTestActivity.class.getName(), CurrentTest.class.getName(),ChargerTest.class.getName(),
            HeadSetTest.class.getName(), FMTest.class.getName(),
            SoundTriggerTestActivity.class.getName(),
            RedLightTest.class.getName(), GreenLightTest.class.getName(),
            BlueLightTest.class.getName(),
            BluetoothTestActivity.class.getName(),
            WifiTestActivity.class.getName(), GpsTestActivity.class.getName(),
            OTGTest.class.getName(),
			EncryptionChip.class.getName(), 
            PhysicalKeyboardTestActivity.class.getName(), };

    private static final String[] SMT_FILTER_CLASS_NAMES = {
			HallTestActivity.class.getName(),	
            SDCardTest.class.getName(), SIMCardTestActivity.class.getName(),
            TofCalibrationTest.class.getName(),
            ColorTemperatureTestActivty.class.getName(),
            SystemVersionTest.class.getName(), RomActivity.class.getName(),RFCALITest.class.getName(),
            RTCTest.class.getName(), BackLightTest.class.getName(),
            AITest.class.getName(),
            ScreenColorTest.class.getName(),
            //SmartPATest.class.getName(),
            CameraFlashTestActivity.class.getName(),
            SingleTouchPointTest.class.getName(),
            SingleTouchPointTestForWacom.class.getName(),
            MutiTouchTest.class.getName(),
            //MelodyTest.class.getName(),
            VibrateTestActivity.class.getName(),
            PhoneLoopBackTest.class.getName(),
            //PhoneCallTestActivity.class.getName(),
            //EarpieceTestActivity.class.getName(),
            SpeakerTestActivity.class.getName(),
            ProxSensorCalibrationActivity.class.getName(),
            //PsensorLightTestActivity.class.getName(),
            PsensorTestActivity.class.getName(),
            //ASensorCalibrationActivity.class.getName(),
            ASensorTestActivity.class.getName(),
            //ASensorSlopeTestActivity.class.getName(),
            //MSensorCalibrationActivity.class.getName(),
            MagneticTestActivity.class.getName(),
            //GSensorCalibrationActivity.class.getName(),
            GyroscopeTestActivity.class.getName(),
            LightSensorCalibrationActivity.class.getName(),
            LsensorTestActivity.class.getName(),
            LsensorNoiseTestActivity.class.getName(),
            CompassTestActivity.class.getName(),
            PressureTestActivity.class.getName(),
            NFCTestActivity.class.getName(),
            CameraTestActivity.class.getName(),
            FrontCameraTestActivity.class.getName(),
            //SecondaryCameraTestActivity.class.getName(),
            //FrontSecondaryCameraTestActivity.class.getName(),
            //SpwCameraTestActivity.class.getName(),
            //MacroLensCameraTestActivity.class.getName(),
            FingerprintTestActivity.class.getName(),
            KeyTestActivity.class.getName(), CurrentTest.class.getName(),ChargerTest.class.getName(),
            HeadSetTest.class.getName(), FMTest.class.getName(),
            SoundTriggerTestActivity.class.getName(),
            RedLightTest.class.getName(), GreenLightTest.class.getName(),
            BlueLightTest.class.getName(),
            BluetoothTestActivity.class.getName(),
            WifiTestActivity.class.getName(), GpsTestActivity.class.getName(),
            OTGTest.class.getName(),
			EncryptionChip.class.getName(),
            PhysicalKeyboardTestActivity.class.getName(), }; 

    private static UnitTestItemList mTestItemListInstance = null;

    public static TestItemList getInstance(Context context) {
        //因为进程一直在后台不会退出(android.ui.system缘故?)，如果用单例模式会导致不同mode获取的结果都是一样。
        //if (mTestItemListInstance == null) {
        	mTestItemListInstance = new UnitTestItemList(context);
        //}
        return mTestItemListInstance;
    }

    private UnitTestItemList(Context context) {
        super(context);
    }

    @Override
    public String[] getfilterClassName() {
        if(Const.mode == 2 || Const.mode == 1){
            return SMT_FILTER_CLASS_NAMES;
        }else {
            return FILTER_CLASS_NAMES;
        }
    }

}
