package cn.com.auxdio.protocol.net;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.com.auxdio.protocol.bean.AuxNetRadioEntity;
import cn.com.auxdio.protocol.bean.AuxNetRadioTypeEntity;
import cn.com.auxdio.protocol.util.AuxLog;

/**
 * Created by Auxdio on 2017/3/20 0020.
 */

class RadioRequest {
    /**
     * 从服务器上请求网络电台数据
     * @param url
     * @return
     */
    private static InputStream request(String url){
        URL radioUrl = null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            radioUrl = new URL(url);
            connection = (HttpURLConnection) radioUrl.openConnection();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                return inputStream;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            /*
            try {
                if (inputStream != null)
                    inputStream.close();
                if (connection != null)
                    connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        }
        return null;
    }

    private static void ParseRadioData(InputStream stream) throws IOException, XmlPullParserException {
        if (stream == null)
            return;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser xmlPullParser = factory.newPullParser();
        xmlPullParser.setInput(stream,"utf-8");
        int eventType = xmlPullParser.getEventType();
        List<AuxNetRadioTypeEntity> radioTypeEntities = new ArrayList<>();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String localName = null;
            switch(eventType) {
                case XmlPullParser.START_TAG:
                    localName = xmlPullParser.getName();
                    if(localName.equals("RadioInfo")){
                        AuxNetRadioTypeEntity auxNetRadioTypeEntity = new AuxNetRadioTypeEntity();
                        auxNetRadioTypeEntity.setRadioType(xmlPullParser.getAttributeValue(0));
                        AuxLog.i("ParseRadioData", "电台类型:"+ auxNetRadioTypeEntity.getRadioType());
                        auxNetRadioTypeEntity.setRadioCount(Integer.parseInt(xmlPullParser.getAttributeValue(1)));
                        AuxLog.i("ParseRadioData", "电台数目:"+ auxNetRadioTypeEntity.getRadioCount());

                        auxNetRadioTypeEntity.setRadioBelong(xmlPullParser.getAttributeValue(2));

                        List<AuxNetRadioEntity> radioEntityList = new ArrayList<>();
                        for (int i = 0; i < auxNetRadioTypeEntity.getRadioCount(); i++) {
                            do{
                                eventType =  xmlPullParser.next();
                            }while(eventType != XmlPullParser.START_TAG);

                            if(eventType == XmlPullParser.START_TAG) {
                                localName = xmlPullParser.getName();
                                if(localName.equals("Radio")){
                                    AuxNetRadioEntity radioEntity = new AuxNetRadioEntity();
                                    radioEntity.setRadioName(xmlPullParser.getAttributeValue(0));
                                    radioEntity.setRadioAddress(xmlPullParser.getAttributeValue(1));
                                    radioEntityList.add(radioEntity);
                                }
                            }
                        }
                        auxNetRadioTypeEntity.setNetRadioList(radioEntityList);
                        radioTypeEntities.add(auxNetRadioTypeEntity);
                    }
                    break;
            }
            eventType = xmlPullParser.next();
        }
        AuxUdpUnicast.getInstance().getRadioListener().onRadioList(radioTypeEntities);
    }
    public static void getRadioList(String url){
        try {
            ParseRadioData(request(url));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }
}
