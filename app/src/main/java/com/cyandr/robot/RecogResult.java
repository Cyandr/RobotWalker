package com.cyandr.robot;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fujiayi on 2017/6/24.
 */
public class RecogResult {
    private static final int ERROR_NONE = 0;

    private String origalJson;
    private String[] resultsRecognition;
    private String origalResult;
    private String sn; // 日志id， 请求有问题请提问带上sn
    private String desc;
    private String resultType;
    private int error = -1;
    private int subError = -1;
    private String result_str;

    public static RecogResult parseJson2(String jsonStr) {
        RecogResult result = new RecogResult();
        result.setOrigalJson(jsonStr);
        try {
            JSONObject json = new JSONObject(jsonStr);
            int error = json.optInt("error");

            result.setResultType(json.optString("result_type"));
            if (error == ERROR_NONE) {
                if (result.isFinalResult()) {
                    result.setResult(json.optString("best_result"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static RecogResult parseJson(String jsonStr) {
        RecogResult result = new RecogResult();
        result.setOrigalJson(jsonStr);
        try {
            JSONObject json = new JSONObject(jsonStr);
            int error = json.optInt("error");
            int subError = json.optInt("sub_error");
            result.setError(error);
            result.setDesc(json.optString("desc"));
            result.setResultType(json.optString("result_type"));
            if (error == ERROR_NONE) {
                result.setOrigalResult(json.getString("origin_result"));
                JSONArray arr = json.optJSONArray("results_recognition");
                if (arr != null) {
                    int size = arr.length();
                    String[] recogs = new String[size];
                    for (int i = 0; i < size; i++) {
                        recogs[i] = arr.getString(i);
                    }
                    result.setResultsRecognition(recogs);
                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void setResult(String desc) {
        this.result_str = desc;
    }

    public String getBestResult() {
        return result_str;
    }

    public boolean hasError() {
        return error != ERROR_NONE;
    }

    private boolean isFinalResult() {
        return "final_result".equals(resultType);
    }


    public boolean isPartialResult() {
        return "partial_result".equals(resultType);
    }

    public boolean isNluResult() {
        return "nlu_result".equals(resultType);
    }

    public String getOrigalJson() {
        return origalJson;
    }

    private void setOrigalJson(String origalJson) {
        this.origalJson = origalJson;
    }

    public String[] getResultsRecognition() {
        return resultsRecognition;
    }

    private void setResultsRecognition(String[] resultsRecognition) {
        this.resultsRecognition = resultsRecognition;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getError() {
        return error;
    }

    private void setError(int error) {
        this.error = error;
    }

    public String getDesc() {
        return desc;
    }

    private void setDesc(String desc) {
        this.desc = desc;
    }

    public String getOrigalResult() {
        return origalResult;
    }

    private void setOrigalResult(String origalResult) {
        this.origalResult = origalResult;
    }

    public String getResultType() {
        return resultType;
    }

    private void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public int getSubError() {
        return subError;
    }

    public void setSubError(int subError) {
        this.subError = subError;
    }
}
