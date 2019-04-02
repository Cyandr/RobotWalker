package com.cyandr.robot;

public class Constant {
    public static final String EXTRA_KEY = "key";
    public static final String EXTRA_SECRET = "secret";
    public static final String EXTRA_SAMPLE = "sample";
    public static final String EXTRA_SOUND_START = "sound_start";
    public static final String EXTRA_SOUND_END = "sound_end";
    public static final String EXTRA_SOUND_SUCCESS = "sound_success";
    public static final String EXTRA_SOUND_ERROR = "sound_error";
    public static final String EXTRA_SOUND_CANCEL = "sound_cancel";
    public static final String EXTRA_INFILE = "infile";
    public static final String EXTRA_OUTFILE = "outfile";
    public static final String EXTRA_GRAMMAR = "grammar";
    public static final String EXTRA_RES_FILE = "res-file";
    public static final String EXTRA_KWS_FILE = "kws-file";

    public static final String EXTRA_LANGUAGE = "language";
    public static final String EXTRA_NLU = "nlu";
    public static final String EXTRA_VAD = "vad";
    public static final String EXTRA_PROP = "prop";

    public static final String EXTRA_OFFLINE_ASR_BASE_FILE_PATH = "asr-base-file-path";
    public static final String EXTRA_LICENSE_FILE_PATH = "license-file-path";
    public static final String EXTRA_OFFLINE_LM_RES_FILE_PATH = "lm-res-file-path";
    public static final String EXTRA_OFFLINE_SLOT_DATA = "slot-data";
    public static final String EXTRA_OFFLINE_SLOT_NAME = "name";
    public static final String EXTRA_OFFLINE_SLOT_SONG = "song";
    public static final String EXTRA_OFFLINE_SLOT_ARTIST = "artist";
    public static final String EXTRA_OFFLINE_SLOT_APP = "app";
    public static final String EXTRA_OFFLINE_SLOT_USERCOMMAND = "usercommand";

    public static final int SAMPLE_8K = 8000;
    public static final int SAMPLE_16K = 16000;

    public static final String VAD_SEARCH = "search";
    public static final String VAD_INPUT = "input";


    /**
     * 开始识别
     */
    static final int MSG_RECOGNIZE_RESULT = 11;
    /**
     * 开始识别
     */
    public static final int MSG_RECOGNIZE_START = 12;
    public static final int MSG_WAKEUP_SUCCESS = 16;
    /**
     * 返回结果，开始说话
     */
    public static final int MSG_WAKEING_UP_AWAITING = 15;
    public static final int MSG_SPEECH_START = 10;
    public static final int MSG_TEXT_JC_START = 19;
}
