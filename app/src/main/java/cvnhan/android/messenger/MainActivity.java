package cvnhan.android.messenger;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends Activity {
    public static final String TAG="MESSENGER";
    public static final String SERVER_HOSTNAME = "192.168.1.28";
    public static final int SERVER_PORT = 1234;
    BufferedReader in = null;
    PrintWriter out = null;
    Handler handler;
    @InjectView(R.id.tvContent)
    TextView tvContent;
    @InjectView(R.id.etInput)
    EditText etInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        connectServer();

    }

    private void connectServer() {
        try {
            // Connect to Chat Server
            Socket socket = new Socket(SERVER_HOSTNAME, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            Log("Connected to server " + SERVER_HOSTNAME + ":" + SERVER_PORT);
            out.print("hello server!\n");
            out.flush();
            Log("Send hello server to Server");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                String string = bundle.getString("myKey");
                tvContent.setText(string);
            }
        };
        // Create and start Sender thread
        Sender sender = new Sender(in,out,handler);
        sender.setDaemon(true);
        sender.start();


    }

    @OnClick(R.id.btSend)
    public void sendMsg(){
        if(out!=null){
            String message = etInput.getText().toString();
            out.println(message);
            out.flush();
        }
    }

    private void Log(String msg){
        Log.e(TAG, msg);
    }
}
class Sender extends Thread {
    private PrintWriter out;
    private BufferedReader in;
    private Handler handler;
    public Sender(BufferedReader input,PrintWriter output, Handler handler) {
        this.out=output;
        this.in=input;
        this.handler=handler;
    }

    /**
     * Until interrupted reads messages from the standard input (keyboard) and
     * sends them to the chat server through the socket.
     */
    public void run() {
        try {
            // Read messages from the server and print them
            String message;
            while ((message = in.readLine()) != null) {
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                SimpleDateFormat dateformat =
                        new SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US);
                String dateString = dateformat.format(new Date())+"-"+message;
                bundle.putString("myKey", dateString);
                msg.setData(bundle);
                handler.sendMessage(msg);
                Log.e(MainActivity.TAG,"Client receive: " + message);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}