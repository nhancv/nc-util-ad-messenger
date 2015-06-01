package cvnhan.android.messenger;

/**
 * Created by cvnhan on 29-May-15.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<UserInfo> userInfos;

    public UserAdapter() {
        userInfos = new ArrayList<>();
    }

    public UserAdapter(List<UserInfo> userInfos) {
        this.userInfos = userInfos;
    }

    public void addMessage(String time, String msg) {
        String user = UserInfo.getAuthor(msg);
        int indeximg = msg.indexOf("#img-");
        if (indeximg != -1) {
            int index = Integer.valueOf(msg.substring(indeximg + 5, msg.lastIndexOf("/#")));
            msg = msg.substring(0, indeximg);
            UserInfo ui = new UserInfo(time, user, UserInfo.getMessage(msg), index);
            userInfos.add(ui);
        } else {
            String message = UserInfo.getMessage(msg);
            UserInfo ui = new UserInfo(time, user, message);
            userInfos.add(ui);
        }
        this.notifyItemInserted(getItemCount() - 1);
    }

    @Override
    public int getItemCount() {
        return userInfos.size();
    }

    @Override
    public void onBindViewHolder(final UserViewHolder userViewHolder, final int i) {
        Animation anim_left= AnimationUtils.loadAnimation(context, R.anim.msg_left);
        Animation anim_right= AnimationUtils.loadAnimation(context, R.anim.msg_right);

        UserInfo ui = userInfos.get(i);
        userViewHolder.tvStatus.setVisibility(View.GONE);
        userViewHolder.merchantImgContent.setVisibility(View.GONE);
        userViewHolder.userImgContent.setVisibility(View.GONE);
        userViewHolder.tvTime.setText(ui.time);
        if (ui.name.equals("merchant")) {
            userViewHolder.userView.setVisibility(View.GONE);
            userViewHolder.merchantView.setVisibility(View.VISIBLE);
            userViewHolder.merchantMessage.setText(ui.message);
            if (ui.img != -1){
                userViewHolder.merchantImgContent.setImageResource(MainActivity.imgResId.get(ui.img));
                userViewHolder.merchantImgContent.setVisibility(View.VISIBLE);
            }
            if(i==getItemCount()-1) {
                userViewHolder.merchantView.setAnimation(anim_left);
                userViewHolder.merchantView.animate().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        userViewHolder.merchantView.setAlpha(0f);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        userViewHolder.merchantView.setAlpha(1f);
                    }
                }).start();
            }
        } else if (ui.name.equals("user")) {
            userViewHolder.merchantView.setVisibility(View.GONE);
            userViewHolder.userView.setVisibility(View.VISIBLE);
            userViewHolder.userMessage.setText(ui.message);
            if (ui.img != -1) {
                userViewHolder.userImgContent.setImageResource(MainActivity.imgResId.get(ui.img));
                userViewHolder.userImgContent.setVisibility(View.VISIBLE);
            }
            if(i==getItemCount()-1) {
                userViewHolder.userView.setAnimation(anim_right);
                userViewHolder.userView.animate().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        userViewHolder.userView.setAlpha(0f);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        userViewHolder.userView.setAlpha(1f);
                    }
                }).start();
            }
        } else {
            userViewHolder.userView.setVisibility(View.GONE);
            userViewHolder.merchantView.setVisibility(View.GONE);
            userViewHolder.tvStatus.setVisibility(View.VISIBLE);
            userViewHolder.tvStatus.setText(ui.name + ": " + ui.message);
        }
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        context=viewGroup.getContext();
        View itemView = LayoutInflater.
                from(context).
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
        SelectableRoundedImageView merchantImgContent;
        @InjectView(R.id.userImgContent)
        SelectableRoundedImageView userImgContent;

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