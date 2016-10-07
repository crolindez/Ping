package es.carlosrolindez.ping;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import org.jsoup.Jsoup;

// TODO Two speeds: Normal & Fast
// TODO check retation when start playing:
// - How to close gameRunnable when closing fragment (but not when rotating)


public class PingActivity extends FragmentActivity  implements  LaunchFragment.OnLaunchUpdate,
                                                                SelectFragment.OnDeviceSelected,
                                                                Handler.Callback/*,
                                                                GameFragment.OnGameFragmentInteractionListener */{

    private final String TAG = "PingActivity";

    private BluetoothAdapter mBluetoothAdapter = null;

    private ServerSocketHandler serviceThread;
    private GameCommManager gameRunnable;
    private Handler handler;

    private GameFragment gameFragment;

 //   private static boolean paused;

    private SharedPreferences preferences;
    private String playerName;

    private FragmentTransaction ft;

    private int mLevel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        preferences = getSharedPreferences("Name", MODE_PRIVATE);
        playerName = preferences.getString("username", getResources().getString(R.string.player));

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getString(R.string.bt_not_available), Toast.LENGTH_LONG).show();
            finish();
        }
        if (savedInstanceState == null) {
            handler = new Handler(this);
            new GetVersionCode().execute();
            LaunchFragment launchFragment = new LaunchFragment();
            launchFragment.setLaunchFragment(playerName);
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame_container, launchFragment, "launch");
            ft.commit();
        } else {

            gameFragment = (GameFragment) getSupportFragmentManager().getFragment(savedInstanceState, "game");
            if (gameFragment==null) {
                handler = new Handler(this);

            } else {
                handler = gameFragment.getHandler();
                gameRunnable = gameFragment.getGameCommManager();
            }
        }



    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        if (gameFragment!=null) {
            getSupportFragmentManager().putFragment(outState, "game", gameFragment);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.bt_not_enabled,Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        } /*else {

            Set<BluetoothDevice> set = mBluetoothAdapter.getBondedDevices();
            if (set!=null) {
                mPairedDevices = set.toArray(new BluetoothDevice[MAX_NUM_DEVICES]);
                numDevices = set.size();
            }
        }*/


    }
     @Override
    protected void onResume() {
        super.onResume();
   //      paused = false;
         startServiceThread();
    }


    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
   //     paused = true;
        closeConnection();
        if (!isChangingConfigurations()) {
            if (gameRunnable!=null) {
                gameRunnable.cancel();
                gameRunnable = null;
            }
        }
    }




    private void startServiceThread() {
        if (serviceThread!=null) {
            serviceThread.cancel();
            serviceThread = null;
        }
        serviceThread = new ServerSocketHandler(handler,mBluetoothAdapter);
        serviceThread.start();
    }


    public void closeConnection() {
        if (serviceThread!=null) {
            serviceThread.cancel();
            serviceThread =  null;
        }
    }



    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case Constants.MESSAGE:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                gameFragment.pushMessage(readMessage);
                break;

            case Constants.MY_CLOSE:
                finish();
                break;

            case Constants.MY_HANDLE:
                if (serviceThread!=null) {
                    serviceThread.cancel();
                    serviceThread = null;
                }

                gameRunnable = (GameCommManager) msg.obj;
                gameFragment = new GameFragment();
                gameFragment.setGameFragment(gameRunnable, playerName, handler, mLevel);
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_container, gameFragment, "game");
                ft.commit();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;

        }
        return true;
    }

    public void savePlayerName(String name) {
        playerName = name;
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("username", playerName);
        edit.apply();
    }

    public void startSelection(int level) {
        mLevel = level;
        SelectFragment selectFragment = new SelectFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_container, selectFragment, "select");
        ft.commit();
    }

    public void connectDeviceItem(BluetoothDevice device) {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        connectDevice(device);
    }

    private void connectDevice(BluetoothDevice device) {

        new ClientSocketHandler(handler,device).start();
    }


    private class GetVersionCode extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {

            String newVersion = null;
            try {
                newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + PingActivity.this.getPackageName() + "&hl=it")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div[itemprop=softwareVersion]")
                        .first()
                        .ownText();
                return newVersion;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String onlineVersion) {
            super.onPostExecute(onlineVersion);

            String currentVersion = null;

            PackageInfo pInfo = null;
            try {
                pInfo =PingActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0);
                currentVersion = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


            if (onlineVersion != null && currentVersion != null && !onlineVersion.equals(currentVersion)) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + PingActivity.this.getPackageName()));
                String title = getResources().getString(R.string.there_is_an_update);

                Intent chooser = Intent.createChooser(intent, title);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
            }


        }
    }
}
