package naver.rlgns1129.androiddataparsing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private EditText idinput, pwinput;
    private Button btnlogin;

    class ThreadEx extends Thread{
        @Override
        public void run(){
            String json = null;
            try{
                URL url = new URL("http://192.168.0.147:9000/oracleserver/login?id=" + idinput.getText().toString() + "&pw=" + pwinput.getText().toString());
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
                json = sb.toString();
                Log.e("json", json);



            }catch(Exception e){
                Log.e("다운로드 에러", e.getMessage());
            }

            try{
                JSONObject object = new JSONObject(json);
                Log.e("파싱.object : ", object.toString());
                //JSONObject result = object.getJSONObject("result");
                Message message = new Message();
                message.obj = object;
                handler.sendMessage(message);


            }catch(Exception e){
                Log.e("파싱 에러", e.getMessage());
            }
        }
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message){
            try {
                JSONObject result = (JSONObject) message.obj;

                boolean r = result.getBoolean("login");
                Log.e("핸들러.r", ""+r);
                if (r == false) {
                    Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_LONG).show();
                } else {
                    ShareData.login = true;
                    //로그인 성공했을 때 ID와 PW를 파일에 저장하기
                    try{
                        FileOutputStream fos = openFileOutput("login.txt", Context.MODE_PRIVATE);
                        fos.write(idinput.getText().toString().getBytes());
                        fos.write(":".getBytes());
                        fos.write(pwinput.getText().toString().getBytes());
                        fos.close();

                    }catch(Exception e){
                        Log.e("id,pw 저장실패", e.getMessage());
                    }
                    Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_LONG).show();
                }
            }catch(Exception e){
                Log.e("핸들러 에러", e.getMessage());
            }

        }
    };

    //onTouchEvent 여기 위치가 맞는지 아닌지 확실치 않음
    @Override
    public boolean onTouchEvent(MotionEvent event){
        //키보드 내리기
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(idinput.getWindowToken(),0);
        imm.hideSoftInputFromWindow(pwinput.getWindowToken(),0);
        return true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Toast.makeText(this,ShareData.login +"",Toast.LENGTH_LONG).show();


        idinput = (EditText)findViewById(R.id.idinput);
        pwinput = (EditText)findViewById(R.id.pwinput);
        btnlogin = (Button)findViewById(R.id.btnlogin);

        try{
            FileInputStream fis = openFileInput("login.txt");
            byte [] data = new byte[fis.available()];
            fis.read(data);

            Toast.makeText(this,new String(data), Toast.LENGTH_LONG).show();
            //기훈 혼자 테스트
            //String idpw = new String(data);
            //String [] idpwidpw = idpw.split(":");
            //idinput.setText(idpwidpw[0]);
            //pwinput.setText(idpwidpw[1]);

        }catch(Exception e){
            Toast.makeText(this,"로그인 한 적 없음", Toast.LENGTH_LONG).show();
        }



        btnlogin.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {
                new ThreadEx().start();

                //키보드 내리기
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(idinput.getWindowToken(),0);
                imm.hideSoftInputFromWindow(pwinput.getWindowToken(),0);
            }
        });
    }
}