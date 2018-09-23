package com.blackforestbytes.simplecloudnotifier.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.blackforestbytes.simplecloudnotifier.view.MessageAdapter;
import com.blackforestbytes.simplecloudnotifier.SCNApp;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CMessageList
{
    public ArrayList<CMessage> Messages;

    private ArrayList<WeakReference<MessageAdapter>> _listener = new ArrayList<>();

    private final static Object _lock = new Object();
    private static CMessageList _inst = null;
    public static CMessageList inst()
    {
        synchronized (_lock)
        {
            if (_inst != null) return _inst;
            return _inst = new CMessageList();
        }
    }

    private CMessageList()
    {
        Messages = new ArrayList<>();

        SharedPreferences sharedPref = SCNApp.getContext().getSharedPreferences("CMessageList", Context.MODE_PRIVATE);
        int count = sharedPref.getInt("message_count", 0);
        for (int i=0; i < count; i++)
        {
            long time      = sharedPref.getLong("message["+i+"].timestamp", 0);
            String title   = sharedPref.getString("message["+i+"].title", "");
            String content = sharedPref.getString("message["+i+"].content", "");

            Messages.add(new CMessage(time, title, content));
        }
    }

    public void add(final long time, final String title, final String content)
    {
        boolean run = SCNApp.runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                SharedPreferences sharedPref = SCNApp.getContext().getSharedPreferences("CMessageList", Context.MODE_PRIVATE);
                int count = sharedPref.getInt("message_count", 0);

                SharedPreferences.Editor e = sharedPref.edit();

                Messages.add(new CMessage(time, title, content));
                e.putInt("message_count", count+1);
                e.putLong("message["+count+"].timestamp", time);
                e.putString("message["+count+"].title", title);
                e.putString("message["+count+"].content", content);

                e.apply();

                for (WeakReference<MessageAdapter> ref : _listener)
                {
                    MessageAdapter a = ref.get();
                    if (a == null) continue;
                    a.customNotifyItemInserted(count);
                }
                CleanUpListener();
            }
        });

        if (!run)
        {
            Messages.add(new CMessage(time, title, content));
            fullSave();
        }
    }

    public void fullSave()
    {
        SharedPreferences sharedPref = SCNApp.getContext().getSharedPreferences("CMessageList", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sharedPref.edit();

        e.clear();

        e.putInt("message_count", Messages.size());

        for (int i = 0; i < Messages.size(); i++)
        {
            e.putLong("message["+i+"].timestamp", Messages.get(i).Timestamp);
            e.putString("message["+i+"].title", Messages.get(i).Title);
            e.putString("message["+i+"].content", Messages.get(i).Content);
        }

        e.apply();
    }

    public CMessage tryGet(int pos)
    {
        if (pos < 0 || pos >= Messages.size()) return null;
        return Messages.get(pos);
    }

    public int size()
    {
        return Messages.size();
    }

    public void register(MessageAdapter adp)
    {
        _listener.add(new WeakReference<>(adp));
        CleanUpListener();
    }

    private void CleanUpListener()
    {
        for (int i=_listener.size()-1; i >= 0; i--)
        {
            if (_listener.get(i).get() == null) _listener.remove(i);
        }
    }
}