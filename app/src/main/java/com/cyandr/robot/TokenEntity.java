package com.cyandr.robot;

import java.util.ArrayList;

public class TokenEntity extends  JcSegResult
{


    class ListContent {

        String word;    //: "哆啦a梦",            //词条内容
        int position;   //: 0,                //词条在原文中的索引位置
        int length;     //:4,                  //词条的词个数（非字节数）
        String pinyin;   //:"duo la a meng",    //词条的拼音
        String pos;     //    :"nz",                  //词条的词性标注
        String entity;  //:null                //词条的实体标注

        @Override
        public String toString() {
            return word + "<" + pos + ">" + "[" + position + "]" + "L:" + length;
        }
    }

    class TokenData {
        double took = -1.0f;
        ArrayList<ListContent> list = new ArrayList<>();

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            for (ListContent content : list) {
                stringBuilder.append(content.toString());
            }
            return stringBuilder.toString();
        }
    }


    public int code;

    TokenData data;

    @Override
    public String toString() {
        return data.toString();
    }


}
