package com.lanqiDoctor.demo.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.app.AppAdapter;
import com.lanqiDoctor.demo.http.api.ChatMessage;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * 聊天记录适配器
 */
public final class ChatAdapter extends AppAdapter<ChatMessage> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;

    public ChatAdapter(Context context) {
        super(context);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = getItem(position);
        return "user".equals(message.getRole()) ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            return new ChatViewHolder(R.layout.chat_user_item);
        } else {
            return new ChatViewHolder(R.layout.chat_ai_item);
        }
    }

    /**
     * 聊天消息ViewHolder实现类
     */
    private final class ChatViewHolder extends ViewHolder {

        private TextView tvMessage;

        public ChatViewHolder(int layoutId) {
            super(layoutId);
            tvMessage = findViewById(R.id.tv_message);
        }

        @Override
        public void onBindView(int position) {
            ChatMessage message = getItem(position);
            if ("assistant".equals(message.getRole())) {
                // 用 commonmark 渲染 Markdown
                Parser parser = Parser.builder().build();
                Node document = parser.parse(message.getContent() == null ? "" : message.getContent());
                HtmlRenderer renderer = HtmlRenderer.builder().build();
                String html = renderer.render(document);

                Spanned spanned;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
                } else {
                    spanned = Html.fromHtml(html);
                }
                tvMessage.setText(spanned);
            } else {
                // 用户消息直接显示
                tvMessage.setText(message.getContent());
            }
        }
    }
}