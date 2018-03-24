package direct.thither.onamove.receivers;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.widget.Toast;

import direct.thither.onamove.App;
import direct.thither.onamove.Globals;

public class networkStateReceiver extends BroadcastReceiver {
    private Context mCtx;

    private Globals mGlobals;
    public networkStateReceiver() {
        mGlobals = App.getInstance().globals;;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        mCtx = context;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();
        if(ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
            //if(!mGlobals.internet)
            //    Toast.makeText(context, "Internet is available!", Toast.LENGTH_LONG).show();
            mGlobals.internet = Boolean.TRUE;
            return;
        }
        if(ni != null && ni.getState() == NetworkInfo.State.CONNECTING) {
            //if(!mGlobals.internet)
            //    Toast.makeText(context, "Connecting to Internet!", Toast.LENGTH_LONG).show();
            mGlobals.internet = Boolean.TRUE;
            return;
        }
        if(ni != null && ni.getState() == NetworkInfo.State.DISCONNECTED) {
            //if(!mGlobals.internet)
            //    Toast.makeText(context, "Internet is disconnected!", Toast.LENGTH_LONG).show();
            mGlobals.internet = Boolean.FALSE;
        }
        if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
            mGlobals.internet = Boolean.FALSE;

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mCtx);
            alertDialog.setTitle("Internet settings");
            alertDialog.setMessage("Internet is not enabled. Do you want to change settings?");

            // On pressing the Settings button.
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    mCtx.startActivity(intent);
                }
            });
            // On pressing the cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(mCtx, "Stop trying to connect", Toast.LENGTH_LONG).show();
                    dialog.cancel();
                    // finish();
                }
            });
            alertDialog.show();
        }
    }
};
