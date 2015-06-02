package cvnhan.android.messenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    public static String author = null;
    BufferedReader in = null;
    PrintWriter out = null;
    Socket clientSocket = null;
    ConnectAsyncTask cn = null;
    Handler handler;
    @InjectView(R.id.tvContent)
    TextView tvContent;
    @InjectView(R.id.etInput)
    EditText etInput;
    UserAdapter userAdapter;
    RecyclerView recList;

    @InjectView(R.id.lvPic)
    LinearLayout lvPic;
    private TypedArray pictureLists;
    public static ArrayList<Integer> imgResId = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        userAdapter = new UserAdapter(createList(1));
        recList.setAdapter(userAdapter);
        recList.getItemAnimator().setSupportsChangeAnimations(true);

        double density = getResources().getDisplayMetrics().density;
        pictureLists = getResources()
                .obtainTypedArray(R.array.picture);
        for (int i = 0; i < pictureLists.length(); i++) {
            imgResId.add(pictureLists.getResourceId(i, -1));
            final ImageButton imageButton = new ImageButton(this);
            imageButton.setImageResource(pictureLists.getResourceId(i, -1));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (150 * density), (int) (100 * density));
            params.setMargins(5, 5, 5, 5);
            imageButton.setLayoutParams(params);
            imageButton.setScaleType(ImageView.ScaleType.FIT_XY);
            imageButton.setTag(i);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (out != null && author != null) {
                        String message = etInput.getText().toString();
                        message += "#img-" + imageButton.getTag() + "/#";
                        out.println(message);
                        etInput.selectAll();
                    }
                }
            });
            lvPic.addView(imageButton);
        }
        pictureLists.recycle();
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
        connectServer();
    }

    private List<UserInfo> createList(int size) {

        List<UserInfo> result = new ArrayList<UserInfo>();
        for (int i = 1; i <= size; i++) {
            UserInfo ci = new UserInfo(UserInfo.getTimeSystem(), ((i % 2) == 0) ? "user" : "merchant", "hello");
            result.add(ci);
        }
        return result;
    }

    private void connectServer() {
        cn = new ConnectAsyncTask();
        cn.execute();
    }

    public void tryAgainPopup() {
        new AlertDialog.Builder(this).setCancelable(false)
                .setTitle("Error")
                .setMessage("May be server is off, try it again!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        connectServer();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @OnClick(R.id.btHtml)
    public void sendHtmlSample(){
        if (out != null && author!=null) {
            String message = "<h1>Hello</h1><i><small><font color=\"#c5c5c5\">\"Competitor ID: \"</font></small></i>\" + \"<font color=\"#47a842\">\" + compID + \"</font><p>this is a <em>sample </em><u>about </u><span style=\"color:#FF0000;\">showing </span><strong>html</strong><img src='cry_smile'/><img src='kiss'/><img src='lightbulb'/><img src='omg_smile'/><img src='thumbs_up'/><img src='about_smile'/></p><p><a href=\"http://google.com.vn\">Link<input name=\"a\" type=\"text\" value=\"d\" /></a></p>";
            if (author == null) author = message;
            out.println(message);
            etInput.selectAll();
        }
    }
    @OnClick(R.id.btSend)
    public void sendMsg() {
        if (out != null) {
            String message = etInput.getText().toString();
            if (author == null) author = message;
            out.println(message);
            etInput.selectAll();
        }
    }

    private void Log(String msg) {
        Log.e(TAG, msg);
    }

    public class ConnectAsyncTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog dialog = null;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "Connecting...", "Connect to server on port 2222", true, false);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            cn = null;
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (result == false) {
                tryAgainPopup();
            }
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                // Connect to Chat Server
                clientSocket = new Socket(SERVER_HOSTNAME, SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
                Log("Connected to server " + SERVER_HOSTNAME + ":" + SERVER_PORT);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            if (clientSocket != null && in != null && out != null) {
                try {
                    Sender sender = null;
                    // Create and start Sender thread
                    sender = new Sender(in, out, handler);
                    sender.setDaemon(true);
                    sender.start();
                } catch (Exception e) {
                    Log("Exception : " + e.toString());
                }
            } else {
                Log("Sorry server off, try again!!!");
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                return false;
            }
            return true;
        }
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