package com.blackforestbytes.simplecloudnotifier.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackforestbytes.simplecloudnotifier.R;
import com.blackforestbytes.simplecloudnotifier.SCNApp;
import com.blackforestbytes.simplecloudnotifier.model.CMessageList;
import com.blackforestbytes.simplecloudnotifier.model.SCNSettings;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import static android.content.Context.CLIPBOARD_SERVICE;

public class AccountFragment extends Fragment
{
    public AccountFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        updateUI(v);

        v.findViewById(R.id.btnCopyUserID).setOnClickListener(cv ->
        {
            ClipboardManager clipboard = (ClipboardManager) cv.getContext().getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("UserID", String.valueOf(SCNSettings.inst().user_id)));
            SCNApp.showToast("Copied userID to clipboard", 1000);
        });

        v.findViewById(R.id.btnCopyUserKey).setOnClickListener(cv ->
        {
            ClipboardManager clipboard = (ClipboardManager) cv.getContext().getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("UserKey", String.valueOf(SCNSettings.inst().user_key)));
            SCNApp.showToast("Copied key to clipboard", 1000);
        });

        v.findViewById(R.id.btnAccountReset).setOnClickListener(cv ->
        {
            Activity a = getActivity();
            if (a == null) return;

            AlertDialog.Builder builder = new AlertDialog.Builder(a);

            builder.setTitle("Confirm");
            builder.setMessage("Reset account key?");

            builder.setPositiveButton("YES", (dialog, which) -> {
                View lpnl = v.findViewById(R.id.loadingPanel);
                lpnl.setVisibility(View.VISIBLE);
                SCNSettings.inst().reset(lpnl);
                dialog.dismiss();
            });

            builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());

            AlertDialog alert = builder.create();
            alert.show();
        });

        v.findViewById(R.id.btnClearLocalStorage).setOnClickListener(cv ->
        {
            Activity a = getActivity();
            if (a == null) return;

            AlertDialog.Builder builder = new AlertDialog.Builder(a);

            builder.setTitle("Confirm");
            builder.setMessage("Clear local messages?");

            builder.setPositiveButton("YES", (dialog, which) -> {
                CMessageList.inst().clear();
                SCNApp.showToast("Messages cleared", 1000);
                dialog.dismiss();
            });

            builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());

            AlertDialog alert = builder.create();
            alert.show();
        });

        v.findViewById(R.id.btnQR).setOnClickListener(cv ->
        {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SCNSettings.inst().createOnlineURL(true)));
            startActivity(browserIntent);
        });

        v.findViewById(R.id.btnRefresh).setOnClickListener(cv ->
        {
            View lpnl = v.findViewById(R.id.loadingPanel);
            lpnl.setVisibility(View.VISIBLE);
            SCNSettings.inst().refresh(lpnl, getActivity());
        });

        return v;
    }

    public void updateUI()
    {
        updateUI(getView());
    }

    @SuppressLint("DefaultLocale")
    public void updateUI(View v)
    {
        if (v == null) return;
        TextView    tvUserID  = v.findViewById(R.id.tvUserID);
        TextView    tvUserKey = v.findViewById(R.id.tvUserKey);
        TextView    tvQuota   = v.findViewById(R.id.tvQuota);
        ImageView   ivQuota   = v.findViewById(R.id.ic_img_quota);
        ImageButton btnQR     = v.findViewById(R.id.btnQR);

        SCNSettings s = SCNSettings.inst();

        if (s.isConnected())
        {
            tvUserID.setText(String.valueOf(s.user_id));
            tvUserKey.setText(s.user_key);
            tvQuota.setText(String.format("%d / %d", s.quota_curr, s.quota_max));
            btnQR.setImageBitmap(QRCode.from(s.createOnlineURL(false)).to(ImageType.PNG).withSize(512, 512).bitmap());
            ivQuota.setColorFilter(s.quota_curr>=s.quota_max ? Color.rgb(200, 0, 0) : Color.rgb(128, 128, 128));
        }
        else
        {
            tvUserID.setText(R.string.str_not_connected);
            tvUserKey.setText(R.string.str_not_connected);
            tvQuota.setText(R.string.str_not_connected);
            btnQR.setImageResource(R.drawable.qr_default);
            ivQuota.setColorFilter(0x80_80_80);
        }
    }
}
