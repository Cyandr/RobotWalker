package com.cyandr.robot.hardware;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.cyandr.robot.R;
import com.cyandr.robot.RobotFace;


public class MyActivity extends Activity {
    private static final String TAG = "mytag";
    private static final String ACTION_USB_PERMISSION = "com.cyandr.robot.USB_PERMISSION";

    private String[] neededPermission;

    //  private PermissionHelper permissionHelper;
    //UI
    private TextView usbDescibetextView = null, usbdatatextView = null;
    private Button senddatabutton = null;


    private String[] permissionArray = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //  permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void InitUI() {
        usbDescibetextView = findViewById(R.id.usbDescribtextView);
        usbdatatextView = findViewById(R.id.usbdata);
        EditText senddataedittext = findViewById(R.id.senddataeditText);
        senddatabutton = findViewById(R.id.senddatabutton);
        Button turnOnface = findViewById(R.id.turnOnface);
        turnOnface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyActivity.this, RobotFace.class);
                startActivity(intent);
            }
        });
        Button btn = findViewById(R.id.turnonAccess);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyActivity.this, UsbAccesseryPage.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accessory_page);


        InitUI();


        senddatabutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // TODO Auto-generated method stub

                Toast.makeText(MyActivity.this, getString(R.string.DATA_SEND), Toast.LENGTH_SHORT).show();
            }
        });
        //permissionHelper= PermissionHelper.getInstance(this);
        // permissionHelper.setForceAccepting(false).request(permissionArray);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        Toast.makeText(this, getString(R.string.USB_OUT), Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    //@Override
    public void onPermissionGranted(@NonNull String[] permissionName) {

    }

    //@Override
    public void onPermissionDeclined(@NonNull String[] permissionName) {

    }

    // @Override
    public void onPermissionPreGranted(@NonNull String permissionsName) {

    }

    //@Override
    public void onPermissionNeedExplanation(@NonNull String permissionName) {
        Log.i("NeedExplanation", "Permission( " + permissionName + " ) needs Explanation");

        // neededPermission = PermissionHelper.declinedPermissions(this, permissionArray);
        StringBuilder builder = new StringBuilder(neededPermission.length);
        if (neededPermission.length > 0) {
            for (String permission : neededPermission) {
                builder.append(permission).append("\n");
            }
        }


    }

    //@Override
    public void onPermissionReallyDeclined(@NonNull String permissionName) {

    }

    //@Override
    public void onNoPermissionNeeded() {

    }
}
