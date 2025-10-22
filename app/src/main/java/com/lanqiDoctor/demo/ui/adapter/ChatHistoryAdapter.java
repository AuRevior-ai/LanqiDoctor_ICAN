package com.lanqiDoctor.demo.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.app.AppAdapter;
import com.lanqiDoctor.demo.entity.ChatSession;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 聊天历史记录适配器
 */
public final class ChatHistoryAdapter extends AppAdapter<ChatSession> {

    private OnItemActionListener mListener;

    public interface OnItemActionListener {
        void onItemClick(ChatSession chatSession);
        void onItemDelete(ChatSession chatSession);
    }

    public ChatHistoryAdapter(Context context) {
        super(context);
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatHistoryViewHolder();
    }

    private final class ChatHistoryViewHolder extends ViewHolder {

        private TextView tvChatTitle;
        private TextView tvChatTime;
        private ImageView ivDelete;

        public ChatHistoryViewHolder() {
            super(R.layout.item_chat_history);
            tvChatTitle = findViewById(R.id.tv_chat_title);
            tvChatTime = findViewById(R.id.tv_chat_time);
            ivDelete = findViewById(R.id.iv_delete);
        }

        @Override
        public void onBindView(int position) {
            ChatSession chatSession = getItem(position);

            // 设置标题
            String title = chatSession.getTitle();
            if (title == null || title.trim().isEmpty()) {
                title = "新对话";
            }
            tvChatTitle.setText(title);

            // 设置时间
            tvChatTime.setText(formatTime(chatSession.getUpdateTime()));

            // 设置点击事件
            getItemView().setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(chatSession);
                }
            });

            // 设置删除事件
            ivDelete.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemDelete(chatSession);
                }
            });
        }
    }

    /**
     * 格式化时间显示
     */
    private String formatTime(long timestamp) {
        Date date = new Date(timestamp);
        Date now = new Date();
        
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(now);

        // 今天
        if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
            
            long diff = now.getTime() - timestamp;
            if (diff < 60 * 1000) { // 1分钟内
                return "刚刚";
            } else if (diff < 60 * 60 * 1000) { // 1小时内
                return (diff / (60 * 1000)) + "分钟前";
            } else { // 今天但超过1小时
                SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return format.format(date);
            }
        }
        // 昨天
        else if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                 cal2.get(Calendar.DAY_OF_YEAR) - cal1.get(Calendar.DAY_OF_YEAR) == 1) {
            return "昨天";
        }
        // 今年内
        else if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
            SimpleDateFormat format = new SimpleDateFormat("MM-dd", Locale.getDefault());
            return format.format(date);
        }
        // 跨年
        else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return format.format(date);
        }
    }
}
