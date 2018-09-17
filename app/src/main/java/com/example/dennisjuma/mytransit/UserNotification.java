package com.example.dennisjuma.mytransit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dennisjuma.mytransit.model.Getter;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserNotification extends Fragment {

    ListView listView;
    DatabaseReference databaseReference;
    FirebaseListAdapter<Getter> getterFirebaseListAdapter;
    String uid;
    Button deleteNotification;
    private static final int SECOND_MILLIS = 60;
    private static final int MINUTE_MILLIS = 1 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview = inflater.inflate(R.layout.activity_user_notification, container, false);

        listView = (ListView) rootview.findViewById(R.id.listView);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        deleteNotification = (Button) rootview.findViewById(R.id.buttonDeleteNotification);

        deleteNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Delete Request?");
                alert.setCancelable(false);
                alert.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("notifications");
                        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                        Toast.makeText(getActivity(), "Notifications Deleted", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog dialog = alert.create();
                dialog.show();
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference().child("notifications").child(uid);

        getterFirebaseListAdapter = new FirebaseListAdapter<Getter>(
                getActivity(),
                Getter.class,
                R.layout.user_notification_layout,
                databaseReference
        ) {
            @Override
            protected void populateView(View v, final Getter model, int position) {
                TextView message = (TextView) v.findViewById(R.id.textViewMessage);
                TextView date = (TextView) v.findViewById(R.id.textViewDate);
                TextView placeAddress = (TextView) v.findViewById(R.id.textViewPlaceAddress);

                message.setText(model.getFull_names()+" matatus heading to your area at "+model.getHname());
                placeAddress.setText(model.getPlaceAddress());

                long time = model.getTimestamp();
                long now = System.currentTimeMillis() / 1000;
                long diff = now - time;
                if (diff < MINUTE_MILLIS) {
                    date.setText(" just now");
                } else if (diff < 2 * MINUTE_MILLIS) {
                    date.setText(" a minute ago");
                } else if (diff < 50 * MINUTE_MILLIS) {
                    date.setText(" "+diff / MINUTE_MILLIS + " minutes ago");
                } else if (diff < 90 * MINUTE_MILLIS) {
                    date.setText(" an hour ago");
                } else if (diff < 24 * HOUR_MILLIS) {
                    date.setText(" "+diff / HOUR_MILLIS + " hours ago");
                } else if (diff < 48 * HOUR_MILLIS) {
                    date.setText(" yesterday");
                } else {
                    date.setText(" "+diff / DAY_MILLIS + " days ago");
                }

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), UserAcceptActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putLong("date", model.getTimestamp());
                        bundle.putString("reason", model.getReason());
                        bundle.putString("names", model.getFull_names());
                        bundle.putString("hname", model.getHname());
                        bundle.putString("place", model.getPlaceAddress());
                        bundle.putString("gender", model.getGender());
                        bundle.putString("route", model.getRoute());
                        bundle.putString("placeId", model.getPlaceId());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });

            }
        };

        listView.setAdapter(getterFirebaseListAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listView.setNestedScrollingEnabled(true);
        }

        return  rootview;
    }

}