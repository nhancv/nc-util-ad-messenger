package cvnhan.android.messenger;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends Activity {
    public static final String TAG = "MESSENGER";
    public static final String SERVER_HOSTNAME = "192.168.1.28";
    public static final int SERVER_PORT = 2222;
    public static String User = null;
    BufferedReader in = null;
    PrintWriter out = null;
    Handler handler;
    @InjectView(R.id.tvContent)
    TextView tvContent;
    @InjectView(R.id.etInput)
    EditText etInput;
    UserAdapter userAdapter;
    RecyclerView recList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        userAdapter = new UserAdapter(createList(2));
        recList.setAdapter(userAdapter);
        ButterKnife.inject(this);
        connectServer();

    }

    private List<UserInfo> createList(int size) {

        List<UserInfo> result = new ArrayList<UserInfo>();
        for (int i = 1; i <= size; i++) {
            UserInfo ci = new UserInfo(UserInfo.getTimeSystem(),((i % 2) == 0) ? "user" : "merchant", "hello");
            result.add(ci);
        }
        return result;
    }

    private void connectServer() {
        try {
            // Connect to Chat Server
            Socket socket = new Socket(SERVER_HOSTNAME, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            Log("Connected to server " + SERVER_HOSTNAME + ":" + SERVER_PORT);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                String strmsg = bundle.getString("msg");
                if (UserInfo.getAuthor(strmsg).equals("server"))
                    tvContent.setText(strmsg);
                else
                    tvContent.setText("");
                userAdapter.addMessage(UserInfo.getTimeSystem(), strmsg);
                recList.scrollToPosition(userAdapter.getItemCount() - 1);
            }
        };
        // Create and start Sender thread
        Sender sender = new Sender(in, out, handler);
        sender.setDaemon(true);
        sender.start();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    @OnClick(R.id.btSend)
    public void sendMsg() {
        if (out != null) {
            String message = etInput.getText().toString();
            out.println(message);
            etInput.selectAll();
        }
    }

    private void Log(String msg) {
        Log.e(TAG, msg);
    }
}

class Sender extends Thread {
    private PrintWriter out;
    private BufferedReader in;
    private Handler handler;

    public Sender(BufferedReader input, PrintWriter output, Handler handler) {
        this.out = output;
        this.in = input;
        this.handler = handler;
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
                if (MainActivity.User == null) MainActivity.User = message;
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("msg", message);
                msg.setData(bundle);
                handler.sendMessage(msg);
                Log.e(MainActivity.TAG, "Client receive: " + message);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}