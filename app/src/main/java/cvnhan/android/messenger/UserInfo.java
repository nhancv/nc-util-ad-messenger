package cvnhan.android.messenger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by cvnhan on 29-May-15.
 */
public class UserInfo {
    protected String name;
    protected String icon;
    protected String message;
    protected int img=-1;
    protected String time;

    public UserInfo() {
    }

    public UserInfo(String time, String name, String message) {
        this.time = time;
        this.name = name;
        this.message = message;
        img=-1;
    }
    public UserInfo(String time, String name, String message, int img) {
        this.time = time;
        this.name = name;
        this.message = message;
        this.img=img;
    }

    public UserInfo(String time, String name, String message, String icon, int img) {
        this(time, name, message, img);
        this.icon = icon;
    }

    public static String getAuthor(String msg) {
        if (msg.startsWith("<")) {
            int index = msg.lastIndexOf(">");
            return msg.substring(1, index);
    }
    return "server";
}

    public static String getMessage(String msg) {
        if (msg.startsWith("<")) {
            int index = msg.lastIndexOf(">");
            return msg.substring(index + 2);
        }
        return msg;
    }

    public static String getTimeSystem(){
        SimpleDateFormat dateformat =
                new SimpleDateFormat("dd/mm/yyyy - HH:mm a", Locale.US);
        return dateformat.format(new Date());
    }

}
