package es.carlosrolindez.ping;

import android.app.ActionBar;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;



public class LaunchFragment extends Fragment {

    private static final String TAG = "LaunchFragment";

    private OnDeviceSelected mListener;

    private ArrayAdapter<String> messageListAdapter = null;
    private ArrayList<String> messageList;
    private ArrayAdapter<String> deviceListAdapter = null;
    private ArrayList<String> deviceList;

    private View mContentView = null;

    public LaunchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.fragment_launch, container, false);
        ActionBar ab = getActivity().getActionBar();
        if (ab!=null) ab.show();

        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();

        uiOptions &= (~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        uiOptions &= (~View.SYSTEM_UI_FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= 19) {
            uiOptions &=(~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        return mContentView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDeviceSelected) {
            mListener = (OnDeviceSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDeviceSelected");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG,"Start Launch Fragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView messageView = (ListView) mContentView.findViewById(R.id.messages);
        messageList = new ArrayList<>();
        messageListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, messageList);
        messageView.setAdapter(messageListAdapter);

        ListView deviceView = (ListView) mContentView.findViewById(R.id.devicesbt);
        deviceList = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, deviceList);
        deviceView.setAdapter(deviceListAdapter);
        deviceView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                Log.e(TAG,"Selected " + (deviceList.size()-position-1));
                mListener.connectDeviceItem(deviceList.size()-position-1);
            }

        });

    }



    public void addMessage(String message) {
        Log.e(TAG,message);
        messageList.add(0, message);
        messageListAdapter.notifyDataSetChanged();
    }

    public void addDevice(String deviceName) {
        Log.e(TAG,deviceName);
        deviceList.add(0, deviceName);
        deviceListAdapter.notifyDataSetChanged();
    }

    public void deleteDeviceList() {
        Log.e(TAG,"delete list");
        deviceList.clear();
        deviceListAdapter.notifyDataSetChanged();
    }


    public interface OnDeviceSelected {
        void connectDeviceItem(int position);
    }



}
