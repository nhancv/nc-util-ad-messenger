package cvnhan.android.messenger;

/**
 * Created by cvnhan on 29-May-15.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserInfo> userInfos;

    public UserAdapter(List<UserInfo> userInfos) {
        this.userInfos = userInfos;
    }


    public void addMessage(String time, String msg){

        String user=UserInfo.getAuthor(msg);
        String message=UserInfo.getMessage(msg);
        UserInfo ui = new UserInfo(time,user,message);
        userInfos.add(ui);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return userInfos.size();
    }

    @Override
    public void onBindViewHolder(UserViewHolder userViewHolder, int i) {
        UserInfo ui = userInfos.get(i);

        userViewHolder.tvStatus.setVisibility(View.GONE);

        userViewHolder.tvTime.setText(ui.time);
        if(ui.name.equals("merchant")){
            userViewHolder.userView.setVisibility(View.GONE);

            userViewHolder.merchantView.setVisibility(View.VISIBLE);
            userViewHolder.merchantMessage.setText(ui.message);

        }else if(ui.name.equals("user")){
            userViewHolder.userView.setVisibility(View.VISIBLE);
            userViewHolder.merchantView.setVisibility(View.GONE);
            userViewHolder.userMessage.setText(ui.message);
        }else{
            userViewHolder.userView.setVisibility(View.GONE);
            userViewHolder.merchantView.setVisibility(View.GONE);
            userViewHolder.tvStatus.setVisibility(View.VISIBLE);
            userViewHolder.tvStatus.setText(ui.name+": "+ui.message);
        }
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.userinfo_layout, viewGroup, false);

        return new UserViewHolder(itemView);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.card_view)
        LinearLayout cardView;

        @InjectView(R.id.merchantView)
        LinearLayout merchantView;
        @InjectView(R.id.userView)
        LinearLayout userView;
        @InjectView(R.id.merchantContentView)
        LinearLayout merchantContentView;
        @InjectView(R.id.userContentView)
        LinearLayout userContentView;
        @InjectView(R.id.merchantImgContent)
        ImageView merchantImgContent;
        @InjectView(R.id.userImgContent)
        ImageView userImgContent;

        @InjectView(R.id.merchantImgIcon)
        ImageView merchantImgIcon;
        @InjectView(R.id.userImgIcon)
        ImageView userImgIcon;

        @InjectView(R.id.merchantMessage)
        TextView merchantMessage;
        @InjectView(R.id.userMessage)
        TextView userMessage;

        @InjectView(R.id.tvStatus)
        TextView tvStatus;
        @InjectView(R.id.tvTime)
        TextView tvTime;

        public UserViewHolder(View v) {
            super(v);
            ButterKnife.inject(this, v);
        }
    }
}