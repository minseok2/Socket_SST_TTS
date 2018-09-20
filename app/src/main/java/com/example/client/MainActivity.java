package com.example.client;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Intent intent;
    SpeechRecognizer mRecognizer;
    TextToSpeech tts;

    TextView textView;
    Button button1, button2, button3;
    EditText editText;
    Boolean test = true;
    RecognitionListener recognitionListener;

    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private int port = 12345;
    private final String ip = "192.168.0.83";
    private MyHandler myHandler;
    private MyThread myThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permission();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            clientSocket = new Socket(ip, port);
            socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();        }




        editText = (EditText) findViewById(R.id.editText);
        button3 = (Button) findViewById(R.id.button3);
        textView = (TextView) findViewById(R.id.textView);
        button1 = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        final Context context = getApplicationContext();

        myHandler = new MyHandler(Looper.getMainLooper(),textView);
        myThread = new MyThread();
        myThread.start();

        intent = intentSST();

        tts = declearTTS();
        mRecognizer = declareSST(context);

        recognitionListener = getListener(textView);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(test) {
                    startSST(mRecognizer,recognitionListener,intent);
                    test = false;
                    button1.setText("OFF");
                }else{
                    stopSST(mRecognizer);
                    test = true;
                    button1.setText("ON");
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakText(tts,textView.getText().toString());
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.getText().toString() != null || !editText.getText().toString().equals("")){
                    socketOut.println(editText.getText().toString());
                }
            }
        });


    }
    @Override
    protected void onStop() {
        super.onStop();
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void permission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, 1
                );
            }
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }



    class MyThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {

                    String data = socketIn.readLine();

                    Message msg = myHandler.obtainMessage();
                    msg.obj = data;
                    Log.d("data : ",data);
                    myHandler.sendMessage(msg);

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class MyHandler extends Handler {

        String data;
        TextView textView;

        public MyHandler(Looper mainLooper , TextView textView) {
            this.textView = textView;
        }

        @Override
        public void handleMessage(Message msg) {
            data = msg.obj.toString();
            Toast.makeText(getApplicationContext(),msg.obj.toString(),Toast.LENGTH_LONG).show();
            textView.setText(data);
        }

        public String getData(){
            return data;
        }


    }

    public TextToSpeech declearTTS(){

        TextToSpeech tts2 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR){
                    //tts2.setLanguage(Locale.KOREA);
                }
            }
        });
        tts2.setLanguage(Locale.KOREA);

        return tts2;

    }
    public void speakText(TextToSpeech tts, String text){
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public SpeechRecognizer declareSST(Context context){
        SpeechRecognizer recognizer = SpeechRecognizer.createSpeechRecognizer(context);


        return recognizer;
    }

    public Intent intentSST(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        return i;
    }

    public RecognitionListener getListener(final TextView textView){
        RecognitionListener listener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                String key = "";
                key = SpeechRecognizer.RESULTS_RECOGNITION;
                ArrayList<String> mResult = bundle.getStringArrayList(key);
                String[] rs = new String[mResult.size()];
                mResult.toArray(rs);
                editText.setText(rs[0]);
                Log.d("text", rs[0]);
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        };

        return listener;
    }



    public void startSST(SpeechRecognizer mRecognizer, RecognitionListener listener, Intent i){
        mRecognizer.setRecognitionListener(listener);
        mRecognizer.startListening(i);
    }

    public void stopSST(SpeechRecognizer mRecognizer){
        mRecognizer.stopListening();
    }



}
