package naver.rlgns1129.androiddataparsing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private TextView display;
    private Button btn;
    //페이지 번호를 저장할 변수
    int pageno = 1;

    //TextView에 출력할 데이터를 저장할 String 변수 생성
    //ListView에 출력하는 경우라면 ArrayList, ListAdapter 생성
    String msg = "";

    //데이터를 다운로드 받을 스레드 클래스
    class ThreadEx extends Thread{
        @Override
        public void run(){
            try{
                //다운로드 받을 URL을 생성
                URL url = new URL("http://192.168.0.147:9000/oracleserver/list?pageno=" + pageno);
                //연결 객체와 옵션 설정
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setConnectTimeout(30000);
                con.setUseCaches(false);

                //문자열을 읽어오기
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while(true){
                    String line = br.readLine();
                    if(line == null){
                        break;
                    }
                    sb.append(line + "\n");
                }
                //읽어온 데이터를 msg에 추가
                //msg = msg + sb.toString();

                //읽어온 데이터를 파싱하기
                JSONObject object = new JSONObject(sb.toString());
                //list 키 안의 배열을 찾아오기
                JSONArray list = object.getJSONArray("list");
                //배열을 순회
                for(int i=0; i<list.length(); i=i+1){
                    //배열에서 i번째 데이터를 가져오기
                    JSONObject item = list.getJSONObject(i);
                    //itemid 와 itemname을 가져와서 msg에 추가
                    int itemid = item.getInt("itemid");
                    String itemname = item.getString("itemname");

                    msg = msg + itemid + ":" + itemname + "\n";

                }



                //핸들러에게 출력 요청
                Message message = new Message();
                message.obj = msg;
                Log.e("message.obj = ", (String)message.obj);
                handler.sendMessage(message);
                //pageno = pageno +1;

            }catch(Exception e){
                Log.e("다운로드 예외", e.getMessage());
            }
        }
    }

    //다운로드 받은 후 데이터를 재출력하는 핸들러
    Handler handler = new Handler(Looper.getMainLooper()){
    @Override
        public void handleMessage(Message message){
            String data = (String)message.obj;
            Log.e("data.obj = ", data);
            display.setText(data);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = (TextView)findViewById(R.id.display);
        btn = (Button)findViewById(R.id.btn);

        //스레드를 생성해서 데이터를 출력
        ThreadEx th = new ThreadEx();
        th.start();

        //버튼을 클릭했을 때 다음 페이지 데이터 추가하기
        btn.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {
                //페이지 번호 증가
                pageno = pageno + 1;
                //스레드를 생성해서 데이터를 출력
                ThreadEx th = new ThreadEx();
                th.start();

            }
        });
    }
}