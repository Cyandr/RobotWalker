package com.cyandr.robot.VoiceSolver;


import com.cyandr.robot.HttpRequestListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class TuringInterface {
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


    //key是你自己注册得到的，当然你直接用我的也行


    public void getStr(final String string) {

        final String[] re = new String[1];
        final String data = setString(string);
        new Thread(new Runnable() {
            @Override
            public void run() {


                String strUrl =
                        "http://www.tuling123.com/openapi/api?key=c1429dd01f8c4fdea62db08f3dbebd69&info=+" + data;
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
                    //返回的JSON，弄成字符串后去掉头和尾就行
                    result = result.substring(23, result.length() - 2);
                    re[0] = result;
                    httpRequestListener.onSuccess(result);
                } catch (IOException e) {
                    httpRequestListener.onFail(e.hashCode(), e.toString());
                    e.printStackTrace();
                }
            }
        }
        ).start();
    }

    public void setHttpRequestListener(HttpRequestListener httpRequestListener) {
        this.httpRequestListener = httpRequestListener;
    }
}
