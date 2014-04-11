package com.suny.ocr.network;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class CommClient
{
	 
        //public  static  final   String  SERVER_IP       = "223.194.199.18:80";
        public  static  final   String  SERVER_IP       = "10.12.26.58:8080";  
        private static  final   String  WS_NAME                                 = "/ImageTest/";
        private static  final   String  MEDIA_NAME                              = "/HiWaySnsServer/media/";
       
        private static  final   String  WS_IMAGE                   = "image.jsp";
       
        //데이터 통신 Timeout 처리.
        private static  final   int             TIMEOUT_CONNECT                 = 3000;         //서버와의 연결을 기다리는 Timeout: 3 sec.
        private static  final   int             TIMEOUT_DATA                    = 5000;         //서버로부터 데이터를 기다리는 Timeout: 5 sec.
       
        /*
         * Class 및 Instance Variable 정의.
         */
        //서버 정보.
        public  static  String          mMyServer               = "";
        public  static  int                     mCountPolygon   = 0;
                           
        //클라이언트 입력정보.
        public  String  mUserID                 = "";                   //사용자 ID : MAC Address 사용.

        //서버 응답정보.
        public  static  int             mMyRoadNo               = 0;                    //자차가 주행중인 도로 번호.
        public  static  int             mMyDirection    = 0;                    //자차의 진행방향.

        public  String  mStrResponse    = "";                   //서버로부터의 응답 메시지.
        public  int             mStatusCode             = 0;                    //작업처리결과 코드.
        public  String  mStatusMsg              = "";                   //작업처리결과 메시지.
        public  String  mLocationMsg    = "";                   //Map Matching 결과 위치정보 메시지.
        public  String  mActiveID               = "";                   //사용자 Active ID.
        public  long    mTimestamp              = 0;
        public  int             mPosLat                 = 0;
        public  int             mPosLng                 = 0;
        public  int             mMemberDistance = 0;
        public  int             mTotalMessages  = 0;

        public  String  mMsgID                  = "";                   //신규 메시지 ID.
       
        //CCTV 정보.
        public  long    mCctvTimestamp  = 0;                    //CCTV URL 정보가 갱신된 최종시각.
        public  String  mUrlMotion              = "";                   //안드로이드 단말기용 동영상 URL.
        public  String  mUrlImage               = "";                   //정지영상 URL.
       
        public String mCctvUrl = "";
        public int mCctvStatus = 0;
       
        public long mVersionCode = 0;
        public String mVersionName = "";        

        /*
         * Method 정의.
         */
        //서버정보 요청 처리.
        public  void    procMyService(String[][] listInput ) throws Exception
        {
                //통신의 초기 오류조건 초기화.
                resetCommStatus();
               
                //서버와 데이터 통신 수행.
                CommClient       objCommClient   = new CommClient();
                XmlProc          objXmlGen               = new XmlProc();

                mMyServer               = "";
                mCountPolygon   = 0;
                try
                {
                        /*
                         * 서버에 Request 전달 및 Response 메시지 수신.
                         */
                        //Request에 전달하는 XML 데이터 구성.
                        objXmlGen.startXML();
                        objXmlGen.startField( "troasis" );
                       
                        long    currentTime     = getCurrentTimestamp();
                        objXmlGen.appendField_Long( "timestamp", currentTime );
                       
          /*              objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );  
                        objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );*/
                 
                        for ( int i = 0; i < listInput.length; i++ )
                        {
                                objXmlGen.appendField( listInput[i][0], listInput[i][1] );
                                if ( listInput[i][0].compareToIgnoreCase("user_id") == 0 )      mUserID = listInput[i][1];
                        }
                       
                        objXmlGen.endField( "troasis" );
                        objXmlGen.endXML();
                        String  xmlInput        = objXmlGen.getXmlData();
                       
                        //서버에 Request 전달 및 Response 수신.
                        //mStrResponse  = objCommClient.sendGet( TrOasisCommClient.WS_LOGIN, xmlInput );
                        mStrResponse    = objCommClient.sendPost( CommClient.WS_IMAGE, xmlInput );
                        //Log.e("response", mStrResponse);
                       
                       
                        /*
                         * 서버로부터 수신한 응답 메시지 파싱.
                         */
                        //응답 메시지 필드목록.
                        String[][]      listResponse    =       {
                                                                                                { "status_code", "" },
                                                                                                { "status_msg", "" },
                                                                                                { "my_server", "" },
                                                                                                { "count_polygon", "" }
                                                                                        };
                        //응답 메시지 파싱.
                        listResponse    = objXmlGen.parseInputXML( mStrResponse, listResponse );
                        //for ( int i = 0; i < listResponse.length; i++ )       Log.i( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
               
                        mStatusCode             = cnvt2intStatus( listResponse[0][1] );
                        mStatusMsg              = cnvt2string( listResponse[1][1] );
                        mMyServer               = cnvt2string( listResponse[2][1] );
                        mCountPolygon   = cnvt2intStatus( listResponse[3][1]);
                        //mMyServer     = "dogong3.hscdn.com";                          //디버깅용....
                        //Log.e("[MY SERVICE]", "mMyServer=" + mMyServer + ", mCountPolygon=" + mCountPolygon);
                        //mMyServer     = "180.182.57.152";
                        //mMyServer     = "61.106.57.234";
                }
                catch( Exception e)
                {
                        Log.e( "[MY SERVICE]", e.toString() );
                        mStatusCode     = 2;
                        mStatusMsg      = e.toString();
                        throw new Exception(e);
                }
        }

      
        //사용자 메시지의 미디어 첨부파일 등록.
        public  void    procUploadFile( String lat, String lon, List<String> fileNames,  String[] _files, int nMediaType) throws Exception
        {
                //통신의 초기 오류조건 초기화.
                resetCommStatus();
               
                //서버와 데이터 통신 수행.
                XmlProc          objXmlGen               = new XmlProc();
                try
                {
                        /*
                         * 서버에 Request 전달 및 Response 메시지 수신.
                         */
                        //Request에 전달하는 XML 데이터 구성.
                        objXmlGen.startXML();
                        objXmlGen.startField( "whereami" );                       
                        objXmlGen.appendField( "file_name", fileNames.toString() );
                        objXmlGen.appendField_Int( "media_type", nMediaType );          
                        objXmlGen.appendField( "media_path", _files.toString() );
                        objXmlGen.endField( "whereami" );
                        objXmlGen.endXML();
                        String  xmlInput        = objXmlGen.getXmlData();
                       
                        //서버에 Request 전달 및 Response 수신.

                        //mStrResponse  = objCommClient.sendPost( TrOasisCommClient.WS_MSG_UPLOAD_FILE, xmlInput );
                        //서버에 전달할 Request URL 구성.
                        String  strServerUrl    = getServerUrl( CommClient.WS_IMAGE);
                        //TODO RAYVO String     strServerUrl    = "http://2.2.14.103:8080/HiWaySnsServer/web_service/msg_upload_file.jsp";
                        ArrayList<File> files = new ArrayList<File>();
                        for(int i = 0; i<_files.length; i++) {
                        	files.add( new File(_files[i]));	
                        }
                        
                       
                        Hashtable<String, String> ht = new Hashtable<String, String>();
                        ht.put("xml", xmlInput);
                       
                        HttpData data = HttpRequest.post(strServerUrl, ht, files);
                        mStrResponse    = data.content;
                        //Log.e("response", mStrResponse);
               
                        /*
                         * 서버로부터 수신한 응답 메시지 파싱.
                         */
                        //응답 메시지 필드목록.
                        String[][]      listResponse    =       {
                                                                                                { "status_code", "" },
                                                                                                { "status_msg", "" },
                                                                                                { "active_id", "" }
                                                                                        };
                        //응답 메시지 파싱.
                        listResponse    = objXmlGen.parseInputXML( mStrResponse, listResponse );
                        for ( int i = 0; i < listResponse.length; i++ ) Log.i( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
       
                        mStatusCode     = cnvt2intStatus( listResponse[0][1] );
                        mStatusMsg      = cnvt2string( listResponse[1][1] );
                        Log.d("CODE", "RayVo" + mStatusCode);
                        //mActiveID     = "";
                        mMsgID          = "";
                        //if ( cnvt2intStatus(listResponse[0][1]) == 0 )        mActiveID = listResponse[2][1];
                }
                catch( Exception e)
                {
                        Log.e( "[UPLOAD FILE]", e.toString() );
                        mStatusCode     = 2;
                        mStatusMsg      = e.toString();
                        throw new Exception(e);
                }
        }

       
       
       


        /*
         * Implementation.
         */
        //GET 방식에 의한 서버 접근.
        public  String  sendGet( String strWsName, String xmlInput ) throws Exception
        {
                String  strResponse     = "";
                try
                {
                        //서버에 전달할 Request URL 구성.
                        String  strServerUrl    = getServerUrl( strWsName );
                        strServerUrl    = strServerUrl + "?xml=" + xmlInput;
                       
                        //서버 URL 접근: GET 방식.
                        HttpGet         strRequest      = new HttpGet( strServerUrl );
       
                        //Timeout 설정.
                        HttpParams      httpParameters          = new BasicHttpParams();
                        HttpConnectionParams.setConnectionTimeout( httpParameters, TIMEOUT_CONNECT );
                        HttpConnectionParams.setSoTimeout( httpParameters, TIMEOUT_DATA );
                       
                        //서버로부터 Response 메시지(XML 파일) 수신.
                        DefaultHttpClient       client  = new DefaultHttpClient( httpParameters );
                        //HttpClient    client  = new DefaultHttpClient();
                        HttpResponse    httpResponse = client.execute( strRequest );
                        strResponse     = EntityUtils.toString( httpResponse.getEntity() );
                       
                        //응답 메시지 Trim 처리. Trim을 하지 않으면 XML Parser에서 오류 발생.
                        strResponse     = strResponse.trim();
               
                        //Log.e("response", strResponse);
                }
                catch (Exception e)
                {
                        throw new Exception( e );
                }
               
                //서버에서 수신한 응답 메시지 전달.
                return strResponse;
        }

        //POST 방식에 의한 서버 접근.
        public  String  sendPost( String strWsName, String xmlInput ) throws Exception
        {
                String  strResponse     = "";
                try
                {
                        ///*
                        //서버에 전달할 Request URL 구성.
                        String  strServerUrl    = getServerUrl( strWsName );
                       
                        //서버 URL 접근: GET 방식.
                        HttpPost        strRequest      = new HttpPost( strServerUrl );
                        List<NameValuePair> pairParams  = new ArrayList<NameValuePair>();
                        //xmlInput      = "임꺽정";
                        //xmlInput      = "<?xml version='1.0' encoding='UTF-8'?>       <troasis>       <user_id>홍길동</user_id>                  <name_input>안녕하세요</name_input>          <name_output>안녕하세요: 홍길동!</name_output>  </troasis> ";
                        pairParams.add( new BasicNameValuePair("xml", xmlInput) );
                        strRequest.setEntity( new UrlEncodedFormEntity(pairParams, HTTP.UTF_8) );
       
                        //Timeout 설정.
                        HttpParams      httpParameters          = new BasicHttpParams();
                        HttpConnectionParams.setConnectionTimeout( httpParameters, TIMEOUT_CONNECT );
                        HttpConnectionParams.setSoTimeout( httpParameters, TIMEOUT_DATA );
                       
                        //서버로부터 Response 메시지(XML 파일) 수신.
                        DefaultHttpClient       client  = new DefaultHttpClient( httpParameters );
                        //HttpClient    client  = new DefaultHttpClient();
                        HttpResponse    httpResponse = client.execute( strRequest );
                        strResponse     = EntityUtils.toString( httpResponse.getEntity() );
                       
                        //응답 메시지 Trim 처리. Trim을 하지 않으면 XML Parser에서 오류 발생.
                        strResponse     = strResponse.trim();
               
                        //Log.e("response", strResponse);
                        // */
                        //throw new Exception( "테스트" );
                }
                catch (Exception e)
                {
                        throw new Exception( e );
                }
               
                //서버에서 수신한 응답 메시지 전달.
                return strResponse;
        }


        /*
         * Implementation 정의.
         */
        //서버 서비스에 접근하려는 URL 생성.
        private static  String  getServerUrl( String strWsName )
        {
                /*TODO Will be removed by RayVo
               
                if (strWsName.compareToIgnoreCase("national_cctv_url.jsp") == 0) {
                        return "http://2.2.14.103:8080/HiWaySnsServer/web_service/national_cctv_url.jsp";
                }
                */
                String  strServerUrl    = "http://" + SERVER_IP  + WS_NAME + strWsName;
                if ( mMyServer.length() < 1 )
                {
                        /*strServerUrl = "";
                        if ( strWsName.compareToIgnoreCase(WS_MY_SERVICE) == 0 ) {
                                strServerUrl    = "http://" + SERVER_IP + WS_NAME + strWsName;
                        } else if (strWsName.compareToIgnoreCase(WS_VERSION_REQUEST) == 0) {
                                strServerUrl    = "http://" + SERVER_IP + WS_NAME + strWsName;
                        } else if (strWsName.compareToIgnoreCase(WS_CCTV_CHANGED_REQUEST) == 0) {
                                strServerUrl    = "http://" + SERVER_IP + WS_NAME + strWsName;
                        }*/
                }
                return strServerUrl;
        }
       
        //서버 미디어에 접근하려는 URL 생성.
        public  static  String  getServerMediaUrl( String strMediaPath )
        {
                //String        strServerUrl    = "http://" + SERVER_IP + MEDIA_NAME + strMediaPath;
                String  strServerUrl    = "http://" + mMyServer + ":8080" + MEDIA_NAME + strMediaPath;
                if ( mMyServer.length() < 1 )   strServerUrl = "";
                return strServerUrl;
        }

        //통신의 초기 오류조건 초기화.
        private void    resetCommStatus()
        {
                mStrResponse    = "";
                mStatusCode             = 2;
                mStatusMsg              = "서버와의 통신연결 실패.";
        }
       
       
        /*
         * Utilities.
         */
        //DB에서 작업이 수행된 시각의 Timestamp.
        public  static  long    getCurrentTimestamp()
        {      
                //1970년 1월 1일 0시를 기준으로 1초 단위의 값을 사용한다.
                return( System.currentTimeMillis() / 1000 );
        }
       
        //시각의 Timestamp를 해독 가능한 문자열로 변환.
        //주어진 Timestamp는 1970년 1월 1일 0시를 기준으로 1초 단위의 값을 사용한다.
        public  static  String  getTimestampString( long timestamp )
        {
                if ( timestamp == 0 )   return( "" );
                SimpleDateFormat formatter = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss", Locale.KOREA );
                return formatter.format ( timestamp * 1000 );
        }
       
        //문자열을 Integer 상태정보로 변환
        public  static  int             cnvt2intStatus( String strValue )
        {
                if ( strValue == null || strValue.length() < 1 )        return 1;                               //Status = Not implemented.
                return( Integer.parseInt(strValue) );
        }
       
        //문자열을 Integer로 변환
        public  static  int             cnvt2int( String strValue )
        {
                if ( strValue == null || strValue.length() < 1 )        return 0;
                return( Integer.parseInt(strValue) );
        }
       
        //문자열을 Long으로 변환
        public  static  long            cnvt2long( String strValue )
        {
                if ( strValue == null || strValue.length() < 1 )        return 0;
                return( Long.parseLong(strValue) );
        }      
       
        //문자열을 Double로 변환
        public  static  double          cnvt2double( String strValue )
        {
                if ( strValue == null || strValue.length() < 1 )        return 0;
                return( Double.parseDouble(strValue) );
        }      
       
        //문자열에서 NULL 처리.
        public  static  String          cnvt2string( String strValue )
        {
                if ( strValue == null || strValue.length() < 1 )        return "";
                return( strValue );
        }
}
