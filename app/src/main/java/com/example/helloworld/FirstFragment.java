package com.example.helloworld;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mylibrary.IMyAidlInterface;
import com.example.mylibrary.MyService;

public class FirstFragment extends Fragment {
    private Activity mContext;
    private Button mBtnTest;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        mContext = this.getActivity();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // button "Test"
        view.findViewById(R.id.button_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext.getClass().isAssignableFrom(MainActivity.class))
                {
                    MainActivity main = (MainActivity) mContext;
                    main.RequestScreenshot();
                }
            }
        });

        // button "Next"
        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                 */
                /* approach 1: implicit Intent. Not working.
                Intent intent = new Intent();
                intent.setAction("com.example.mylibrary.MyService");
                intent.setPackage("com.example.mylibrary");
                mContext.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
                 */
                // approach 2: explicit Intent. Working.
                Intent intent = new Intent(mContext, MyService.class);
                mContext.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
            }
        });
    }

    ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            IMyAidlInterface stub = IMyAidlInterface.Stub.asInterface(iBinder);
            if (stub == null) {
                Log.e("JxdDemo", "No service available.");
                return;
            }
            try {
                int value = stub.add(1, 8); // DEMO，只提供加法服务
                Log.i("JxdDemo", "result: " + value);
            } catch (RemoteException e) {
                    e.printStackTrace();
            }
            mContext.unbindService(serviceConnection);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("JxdDemo", "service end.");
        }
    };
}