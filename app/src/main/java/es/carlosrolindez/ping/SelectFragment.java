package es.carlosrolindez.ping;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;


public class SelectFragment extends Fragment {

    private static final String TAG = "SelectFragment";

    private OnDeviceSelected mListener;

    private ArrayAdapter<String> deviceListAdapter = null;
    private ArrayList<String> deviceList;

    private View mContentView = null;

    private BluetoothAdapter mBluetoothAdapter = null;
    private final int MAX_NUM_DEVICES = 100;
    private BluetoothDevice[] mPairedDevices;
    private int numDevices;

    public SelectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.fragment_selector, container, false);


        ActionBar ab = getActivity().getActionBar();
        if (ab!=null) ab.show();


        int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();


        uiOptions &= (~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        if (Build.VERSION.SDK_INT >= 16)    {
            uiOptions &= (~View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= 19) {
            uiOptions &=(~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);


        ListView deviceView = (ListView) mContentView.findViewById(R.id.devicesbt);

        deviceList = new ArrayList<>();

        deviceListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, deviceList);

        deviceView.setAdapter(deviceListAdapter);
        deviceView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                mListener.connectDeviceItem(mPairedDevices[deviceList.size()-position-1]);
            }

        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> set = mBluetoothAdapter.getBondedDevices();
        if (set!=null) {
            mPairedDevices = set.toArray(new BluetoothDevice[MAX_NUM_DEVICES]);
            numDevices = set.size();
            if (numDevices > MAX_NUM_DEVICES) numDevices = MAX_NUM_DEVICES;
        }

        for (int index=0; index<numDevices; index++) {
            addDevice(mPairedDevices[index].getName());
        }

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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public void addDevice(String deviceName) {
        deviceList.add(0, deviceName);
        deviceListAdapter.notifyDataSetChanged();
    }
/*
    public void deleteDeviceList() {
        deviceList.clear();
        deviceListAdapter.notifyDataSetChanged();
    }

*/
    public interface OnDeviceSelected {
        void connectDeviceItem(BluetoothDevice device);
    }



}
