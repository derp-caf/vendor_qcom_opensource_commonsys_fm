/*
 * Copyright (c) 2009-2012, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *        * Redistributions of source code must retain the above copyright
 *            notice, this list of conditions and the following disclaimer.
 *        * Redistributions in binary form must reproduce the above copyright
 *            notice, this list of conditions and the following disclaimer in the
 *            documentation and/or other materials provided with the distribution.
 *        * Neither the name of The Linux Foundation nor
 *            the names of its contributors may be used to endorse or promote
 *            products derived from this software without specific prior written
 *            permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT ARE DISCLAIMED.    IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package qcom.fmradio;
import android.util.Log;
import java.util.Arrays;
import java.lang.Runnable;
import qcom.fmradio.FmReceiver;

class FmReceiverJNI {
    /**
     * General success
     */
    static final int FM_JNI_SUCCESS = 0;

    /**
     * General failure
     */
    static final int FM_JNI_FAILURE = -1;

    /**
     * native method: Open device
     * @return The file descriptor of the device
     *
     */
      private static final String TAG = "FmReceiverJNI";

    static {
        Log.e(TAG, "classinit native called");
        classInitNative();
    }
    static native void classInitNative();
    static native void initNative();
    static native void cleanupNative();

    final private FmRxEvCallbacks mCallback;
    static private final int STD_BUF_SIZE = 256;
    static private byte[] mRdsBuffer = new byte[STD_BUF_SIZE];

    public static  byte[]  getPsBuffer(byte[] buff) {
        Log.e(TAG, "getPsBuffer enter");
        buff = Arrays.copyOf(mRdsBuffer, mRdsBuffer.length);
        Log.e(TAG, "getPsBuffer exit");
        return buff;
    }

    public void AflistCallback(byte[] aflist) {
        Log.e(TAG, "AflistCallback enter " );
        if (aflist == null) {
            Log.e(TAG, "aflist null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(aflist, aflist.length);
        FmReceiver.mCallback.FmRxEvRdsAfInfo();
        Log.e(TAG, "AflistCallback exit " );
    }

    public void RtPlusCallback(byte[] rtplus) {
        Log.e(TAG, "RtPlusCallback enter " );
        if (rtplus == null) {
            Log.e(TAG, "psInfo null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(rtplus, rtplus.length);
        FmReceiver.mCallback.FmRxEvRTPlus();
        Log.e(TAG, "RtPlusCallback exit " );
    }

    public void RtCallback(byte[] rt) {
        Log.e(TAG, "RtCallback enter " );
        if (rt == null) {
            Log.e(TAG, "psInfo null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(rt, rt.length);
        FmReceiver.mCallback.FmRxEvRdsRtInfo();
        Log.e(TAG, "RtCallback exit " );
    }

    public void ErtCallback(byte[] ert) {
        Log.e(TAG, "ErtCallback enter " );
        if (ert == null) {
            Log.e(TAG, "ERT null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(ert, ert.length);
        FmReceiver.mCallback.FmRxEvERTInfo();
        Log.e(TAG, "RtCallback exit " );
    }

    public void PsInfoCallback(byte[] psInfo) {
        Log.e(TAG, "PsInfoCallback enter " );
        if (psInfo == null) {
            Log.e(TAG, "psInfo null return  ");
            return;
        }
        Log.e(TAG, "length =  " +psInfo.length);
        mRdsBuffer = Arrays.copyOf(psInfo, psInfo.length);
        FmReceiver.mCallback.FmRxEvRdsPsInfo();
        Log.e(TAG, "PsInfoCallback exit");
    }

    public void enableCallback() {
        Log.e(TAG, "enableCallback enter");
        FmTransceiver.setFMPowerState(FmTransceiver.FMState_Rx_Turned_On);
        Log.v(TAG, "RxEvtList: CURRENT-STATE : FMRxStarting ---> NEW-STATE : FMRxOn");
        FmReceiver.mCallback.FmRxEvEnableReceiver();
        Log.e(TAG, "enableCallback exit");
    }

    public void tuneCallback(int freq) {
        int state;

        Log.e(TAG, "tuneCallback enter");
        state = FmReceiver.getSearchState();
        switch(state) {
        case FmTransceiver.subSrchLevel_SrchAbort:
            Log.v(TAG, "Current state is SRCH_ABORTED");
            Log.v(TAG, "Aborting on-going search command...");
            /* intentional fall through */
        case FmTransceiver.subSrchLevel_SeekInPrg :
            Log.v(TAG, "Current state is " + state);
            FmReceiver.setSearchState(FmTransceiver.subSrchLevel_SrchComplete);
            Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
            FmReceiver.mCallback.FmRxEvSearchComplete(freq);
            break;
        default:
            if (freq > 0)
                FmReceiver.mCallback.FmRxEvRadioTuneStatus(freq);
            else
                Log.e(TAG, "get frequency command failed");
            break;
        }
        Log.e(TAG, "tuneCallback exit");
    }

    public void seekCmplCallback(int freq) {
        int state;

        Log.e(TAG, "seekCmplCallback enter");
        state = FmReceiver.getSearchState();
        switch(state) {
        case FmTransceiver.subSrchLevel_ScanInProg:
            Log.v(TAG, "Current state is " + state);
            FmReceiver.setSearchState(FmTransceiver.subSrchLevel_SrchComplete);
            Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE :FMRxOn");
            FmReceiver.mCallback.FmRxEvSearchComplete(freq);
            break;
        case FmTransceiver.subSrchLevel_SrchAbort:
            Log.v(TAG, "Current state is SRCH_ABORTED");
            Log.v(TAG, "Aborting on-going search command...");
            FmReceiver.setSearchState(FmTransceiver.subSrchLevel_SrchComplete);
            Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
            FmReceiver.mCallback.FmRxEvSearchComplete(freq);
            break;
        }
        Log.e(TAG, "seekCmplCallback exit");
    }

    public void scanNxtCallback() {
        Log.e(TAG, "scanNxtCallback enter");
        FmReceiver.mCallback.FmRxEvSearchInProgress();
        Log.e(TAG, "scanNxtCallback exit");
    }

    public void stereostsCallback(boolean stereo) {
        Log.e(TAG, "stereostsCallback enter");
        FmReceiver.mCallback.FmRxEvStereoStatus (stereo);
        Log.e(TAG, "stereostsCallback exit");
    }

    public void rdsAvlStsCallback(boolean rdsAvl) {
        Log.e(TAG, "rdsAvlStsCallback enter");
        FmReceiver.mCallback.FmRxEvRdsLockStatus(rdsAvl);
        Log.e(TAG, "rdsAvlStsCallback exit");
    }

    public void disableCallback() {
        Log.e(TAG, "disableCallback enter");
        if (FmTransceiver.getFMPowerState() == FmTransceiver.subPwrLevel_FMTurning_Off) {
                 /*Set the state as FMOff */
            FmTransceiver.setFMPowerState(FmTransceiver.FMState_Turned_Off);
            Log.v(TAG, "RxEvtList: CURRENT-STATE : FMTurningOff ---> NEW-STATE : FMOff");
            FmReceiver.mCallback.FmRxEvDisableReceiver();
        } else {
            FmTransceiver.setFMPowerState(FmTransceiver.FMState_Turned_Off);
            Log.d(TAG, "Unexpected RADIO_DISABLED recvd");
            Log.v(TAG, "RxEvtList: CURRENT-STATE : FMRxOn ---> NEW-STATE : FMOff");
            FmReceiver.mCallback.FmRxEvRadioReset();
            Log.e(TAG, "disableCallback exit");
        }
    }

    public FmReceiverJNI(FmRxEvCallbacks callback) {
        mCallback = callback;
        if (mCallback == null)
            Log.e(TAG, "mCallback is null in JNI");
        Log.e(TAG, "satish init native called");
        initNative();
    }

    static native int acquireFdNative(String path);

    /**
     * native method:
     * @param fd
     * @param control
     * @param field
     * @return
     */
    static native int audioControlNative(int fd, int control, int field);

    /**
     * native method: cancels search
     * @param fd file descriptor of device
     * @return May return
     *             {@link #FM_JNI_SUCCESS}
     *             {@link #FM_JNI_FAILURE}
     */
    static native int cancelSearchNative(int fd);

    /**
     * native method: release control of device
     * @param fd file descriptor of device
     * @return May return
     *             {@link #FM_JNI_SUCCESS}
     *             {@link #FM_JNI_FAILURE}
     */
    static native int closeFdNative(int fd);

    /**
     * native method: get frequency
     * @param fd file descriptor of device
     * @return Returns frequency in int form
     */
    static native int getFreqNative(int fd);

    /**
     * native method: set frequency
     * @param fd file descriptor of device
     * @param freq freq to be set in int form
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     *
     */
    static native int setFreqNative(int fd, int freq);

    /**
     * native method: get v4l2 control
     * @param fd file descriptor of device
     * @param id v4l2 id to be retrieved
     * @return Returns current value of the
     *         v4l2 control
     */
    static native int getControlNative (int fd, int id);

    /**
     * native method: set v4l2 control
     * @param fd file descriptor of device
     * @param id v4l2 control to be set
     * @param value value to be set
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int setControlNative (int fd, int id, int value);

    /**
     * native method: start search
     * @param fd file descriptor of device
     * @param dir search direction
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int startSearchNative (int fd, int dir);

    /**
     * native method: get buffer
     * @param fd file descriptor of device
     * @param buff[] buffer
     * @param index index of the buffer to be retrieved
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int getBufferNative (int fd, byte  buff[], int index);

    /**
     * native method: get RSSI value of the
     *                received signal
     * @param fd file descriptor of device
     * @return Returns signal strength in int form
     *         Signal value range from -120 to 10
     */
    static native int getRSSINative (int fd);

    /**
     * native method: set FM band
     * @param fd file descriptor of device
     * @param low lower band
     * @param high higher band
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int setBandNative (int fd, int low, int high);

    /**
     * native method: get lower band
     * @param fd file descriptor of device
     * @return Returns lower band in int form
     */
    static native int getLowerBandNative (int fd);

    /**
     * native method: get upper band
     * @param fd file descriptor of device
     * @return Returns upper band in int form
     */
    static native int getUpperBandNative (int fd);

    /**
     * native method: force Mono/Stereo mode
     * @param fd file descriptor of device
     * @param val force mono/stereo indicator
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int setMonoStereoNative (int fd, int val);

    /**
     * native method: get Raw RDS data
     * @param fd file descriptor of device
     * @param buff[] buffer
     * @param count number of bytes to be read
     * @return Returns number of bytes read
     */
    static native int getRawRdsNative (int fd, byte  buff[], int count);

    /**
     * native method: set v4l2 control
     * @param fd file descriptor of device
     * @param id v4l2 control to be set
     * @param value value to be set
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int setNotchFilterNative(int fd, int id, boolean value);

    /**
     * native method: enable/disable Analog Mode
     */
    static native int setAnalogModeNative(boolean value);

    /**
     * native method: Starts the RT transmission
     * @param fd file descriptor of device
     * @param buff[] buffer
     * @param count number of bytes to be read
     * @return Returns number of bytes read
     */
    static native int startRTNative(int fd, String str, int count);

    /**
     * native method: Stops the RT transmission
     * @param fd file descriptor of device
     * @param buff[] buffer
     * @param count number of bytes to be read
     * @return Returns number of bytes read
     */
    static native int stopRTNative(int fd);

    /**
     * native method: Starts the PS transmission
     * @param fd file descriptor of device
     * @param buff[] buffer
     * @param count number of bytes to be read
     * @return Returns number of bytes read
     */
    static native int startPSNative(int fd, String str, int count);

    /**
     * native method: Stops the PS transmission
     * @param fd file descriptor of device
     * @param buff[] buffer
     * @param count number of bytes to be read
     */
    static native int stopPSNative(int fd);
   /**
     * native method: Sets the Programme type for transmission
     * @param fd file descriptor of device
     * @param pty program type to be transmited
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int setPTYNative (int fd, int pty);

   /**
     * native method: Sets the Programme Id for transmission
     * @param fd file descriptor of device
     * @param pty program Id to be transmited
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int setPINative (int fd, int pi);


   /**
     * native method: Sets the repeat count for Programme service
     * transmission.
     * @param fd file descriptor of device
     * @param repeatcount  number of times PS string to be transmited
     *                     repeatedly.
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int setPSRepeatCountNative(int fd, int repeatCount);
   /**
     * native method: Sets the power level for the tramsmitter
     * transmission.
     * @param fd file descriptor of device
     * @param powLevel is the level at which transmitter operates.
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int setTxPowerLevelNative(int fd, int powLevel);
   /**
     * native method: Sets the calibration
     * @param fd file descriptor of device
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int SetCalibrationNative(int fd);

   /**
     * native method: Configures the spur table
     * @param fd file descriptor of device
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int configureSpurTable(int fd);

    /**
     * native method: Configures the new spur table
     * @param fd file descriptor of device
     * @return {@link #FM_JNI_SUCCESS}
     *         {@link #FM_JNI_FAILURE}
     */
    static native int setSpurDataNative(int fd, short  buff[], int len);
    static native void configurePerformanceParams(int fd);
}
