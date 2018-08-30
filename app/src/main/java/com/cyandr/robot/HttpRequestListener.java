package com.cyandr.robot;

public interface HttpRequestListener {
    void onSuccess(String var1);

    void onFail(int var1, String var2);
}
