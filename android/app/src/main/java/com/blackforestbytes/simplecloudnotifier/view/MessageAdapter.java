package com.blackforestbytes.simplecloudnotifier.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackforestbytes.simplecloudnotifier.R;
import com.blackforestbytes.simplecloudnotifier.model.CMessage;
import com.blackforestbytes.simplecloudnotifier.model.CMessageList;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MessageAdapter extends RecyclerView.Adapter
{
    private final View vNoElements;
    private final LinearLayoutManager manLayout;
    private final RecyclerView viewRecycler;

    private WeakHashMap<MessagePresenter, Boolean> viewHolders = new WeakHashMap<>();

    public MessageAdapter(View noElementsView, LinearLayoutManager layout, RecyclerView recycler)
    {
        vNoElements  = noElementsView;
        manLayout    = layout;
        viewRecycler = recycler;
        CMessageList.inst().register(this);

        vNoElements.setVisibility(getItemCount()>0 ? View.GONE : View.VISIBLE);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View myView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_card, parent, false);
        return new MessagePresenter(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        CMessage msg = CMessageList.inst().tryGetFromBack(position);
        MessagePresenter view = (MessagePresenter) holder;
        view.setMessage(msg);

        viewHolders.put(view, true);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder)
    {
        if (holder instanceof MessagePresenter) viewHolders.remove(holder);
    }

    @Override
    public int getItemCount()
    {
        return CMessageList.inst().size();
    }

    public void customNotifyItemInserted(int idx)
    {
        notifyItemInserted(idx);
        vNoElements.setVisibility(getItemCount()>0 ? View.GONE : View.VISIBLE);
    }

    public void customNotifyDataSetChanged()
    {
        notifyDataSetChanged();
        vNoElements.setVisibility(getItemCount()>0 ? View.GONE : View.VISIBLE);
    }

    public void scrollToTop()
    {
        manLayout.smoothScrollToPosition(viewRecycler, null, 0);
    }

    public CMessage removeItem(int position)
    {
        CMessage i = CMessageList.inst().removeFromBack(position);
        notifyDataSetChanged();
        return i;
    }

    public void restoreItem(CMessage item, int position)
    {
        CMessageList.inst().insert(position, item);
        notifyDataSetChanged();
    }

    public class MessagePresenter extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private TextView tvTimestamp;
        private TextView tvTitle;
        private TextView tvMessage;
        private ImageView ivPriority;

        public RelativeLayout viewForeground;
        public RelativeLayout viewBackground;

        private CMessage data;

        MessagePresenter(View itemView)
        {
            super(itemView);
            tvTimestamp    = itemView.findViewById(R.id.tvTimestamp);
            tvTitle        = itemView.findViewById(R.id.tvTitle);
            tvMessage      = itemView.findViewById(R.id.tvMessage);
            ivPriority     = itemView.findViewById(R.id.ivPriority);
            viewForeground = itemView.findViewById(R.id.layoutFront);
            viewBackground = itemView.findViewById(R.id.layoutBack);

            itemView.setOnClickListener(this);
            tvTimestamp.setOnClickListener(this);
            tvTitle.setOnClickListener(this);
            tvMessage.setOnClickListener(this);
            ivPriority.setOnClickListener(this);
            viewForeground.setOnClickListener(this);
        }

        void setMessage(CMessage msg)
        {
            tvTimestamp.setText(msg.formatTimestamp());
            tvTitle.setText(msg.Title);
            tvMessage.setText(msg.Content);

            switch (msg.Priority)
            {
                case LOW:
                    ivPriority.setVisibility(View.VISIBLE);
                    ivPriority.setImageResource(R.drawable.priority_low);
                    ivPriority.setColorFilter(Color.rgb(176, 176, 176));
                    break;
                case NORMAL:
                    ivPriority.setVisibility(View.GONE);
                    ivPriority.setColorFilter(Color.rgb(176, 176, 176));
                    break;
                case HIGH:
                    ivPriority.setVisibility(View.VISIBLE);
                    ivPriority.setImageResource(R.drawable.priority_high);
                    ivPriority.setColorFilter(Color.rgb(200, 0, 0));
                    break;
            }

            data = msg;
        }

        @Override
        public void onClick(View v)
        {
            for (MessagePresenter holder : MessageAdapter.this.viewHolders.keySet())
            {
                if (holder == null) continue;
                if (holder == this) continue;
                if (holder.tvMessage == null) continue;
                if (holder.tvMessage.getMaxLines() == 6) continue;
                holder.tvMessage.setMaxLines(6);
            }

            tvMessage.setMaxLines(9999);
        }
    }
}
