package com.cyandr.robot;


import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


public class JcSegInterface {

    private String ServerAddress = "101.6.95.54";
    private String ServerPort = "10902";
    private TokenCate m_TokenCate=TokenCate.TOKENS_ENTITY;

    public class JcData {
        float took;
        ArrayList<String> keywords = new ArrayList<>();

    }

    public class JcStruct {

        int code;
        JcData data;

    }

    JcSegResult ParseResult(final String strline) {
        JcSegResult jcSegResult = null;
        switch (m_TokenCate) {


            case KEYWORDS:
                break;
            case KEYPHRASE:
                break;
            case SENTENCE:
                break;
            case SUMMARY:
                break;
            case TOKENS_ENTITY:
                try {
                    jcSegResult = new ObjectMapper().readValue(strline,TokenEntity.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

        return jcSegResult;
    }

    private int PhraseNum = 6;
    private boolean isfilterused = false;

    private String getfilter() {
        return isfilterused ? "true" : "false";

    }

    private HttpRequestListener httpRequestListener;

    private String setString(String str) {
        try {
            //一定要将其变成utf-8
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    enum TokenCate {
        KEYWORDS,
        KEYPHRASE,
        SENTENCE,
        SUMMARY,
        TOKENS_ENTITY
    }

    //key是你自己注册得到的，当然你直接用我的也行
    String GetLinkStr(final String string) {
        String strUrl = "http://" + ServerAddress + ":" + ServerPort;
        switch (m_TokenCate) {


            case KEYWORDS:
                strUrl += "/extractor/" +
                        "keyphrase" +
                        "?text=\"" + string +
                        "\"&number=" + PhraseNum;
                break;
            case KEYPHRASE:
                strUrl += "/extractor/" +
                        "keywords" +
                        "?text=\"" + string +
                        "\"&number=" + PhraseNum;
                break;
            case SENTENCE:
                strUrl += "/extractor/" +
                        "sentence" +
                        "?text=\"" + string +
                        "\"&number=" + PhraseNum;
                break;
            case SUMMARY:
                strUrl += "/extractor/" +
                        "sentence" +
                        "?text=\"" + string +
                        "\"&length=" + PhraseNum;
                break;
            case TOKENS_ENTITY:

                strUrl += "/tokenizer/" +
                        "tokenizer_instance" +
                        "?text=\"" + string;
                break;
        }
        return strUrl;
    }

    String getStr(final String string) {
        final String[] re = new String[1];
        final String data = setString(string);
        final String strUrl = GetLinkStr(string);
        Log.d("Attention", strUrl);
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL(strUrl);

                    HttpURLConnection conn = null;
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko");
                    conn.connect();
                    //打开这个页面的输入流，这个网站的内容以字节流的形式返回。如果是网页就返回html，图片就返回图片的内容。
                    InputStream inStream = conn.getInputStream();
                    byte[] buf = new byte[1024];
                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    int n = 0;
                    while ((n = inStream.read(buf)) != -1) {
                        outStream.write(buf, 0, n);
                    }

                    inStream.close();
                    outStream.close();

                    //用ByteArrayOutputStream全部缓冲好后再一次转成String，不然再间隔的地方会出现乱码问题

                    String result = outStream.toString();
                 JcSegResult res=   ParseResult(result);
                    httpRequestListener.onSuccess(res.toString());
                } catch (IOException e) {
                    httpRequestListener.onFail(e.hashCode(), e.toString());
                    e.printStackTrace();
                }
            }
        }
        ).start();

        return strUrl;
    }

    public void setHttpRequestListener(HttpRequestListener httpRequestListener) {
        this.httpRequestListener = httpRequestListener;
    }
}
