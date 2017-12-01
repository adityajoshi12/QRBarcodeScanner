package labs.aditya.labs.qrbarcodescanner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{


    private static final int REQUEST_CAMERA = 0;
    private static int camId = Camera.CameraInfo.CAMERA_FACING_BACK;
    AwesomeInfoDialog dialog;
    boolean dialogstatus=false;
    private ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        dialog= new AwesomeInfoDialog(this);
        int currentApiVersion = Build.VERSION.SDK_INT;

        dialog.setCancelable(false);
        if (currentApiVersion >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                //Toast.makeText(getApplicationContext(), "Permission already granted!", Toast.LENGTH_LONG).show();
            } else {
                requestPermission();
            }
        }
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if (scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {

        final String[] myResult = {result.getText()};
        Log.e("QRCodeScanner", result.getText());
        Log.e("QRCodeScanner", result.getBarcodeFormat().toString());



        /////////////////////////////


        dialog
                .setTitle("Scan Result")
                .setMessage(myResult[0])
                .setColoredCircle(R.color.dialogInfoBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
                .setCancelable(true)
                .setPositiveButtonText("Visit")
                .setPositiveButtonbackgroundColor(R.color.dialogInfoBackgroundColor)
                .setPositiveButtonTextColor(R.color.white)
                .setNeutralButtonText("Copy to Clipboard")
                .setNeutralButtonbackgroundColor(R.color.dialogInfoBackgroundColor)
                .setNeutralButtonTextColor(R.color.white)
                .setNegativeButtonText("Cancel")
                .setNegativeButtonbackgroundColor(R.color.dialogInfoBackgroundColor)
                .setNegativeButtonTextColor(R.color.white)
                .setPositiveButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        Intent chooser;
                        if (!myResult[0].startsWith("http://") && !myResult[0].startsWith("https://")) {
                            myResult[0] = "http://" + myResult[0];
                            i.setData(Uri.parse(myResult[0]));
                            String title = "Open with....";
                            chooser = Intent.createChooser(i, title);
                            if (i.resolveActivity(getPackageManager()) != null) {
                                startActivity(chooser);
                            }
                        }
                    }
                })
                .setNeutralButtonClick(new Closure() {
                    @Override
                    public void exec() {

                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", myResult[0]);
                        clipboard.setPrimaryClip(clip);
                        scannerView.resumeCameraPreview(MainActivity.this);
                        Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();


                    }
                })
                .setNegativeButtonClick(new Closure() {
                    @Override
                    public void exec() {

                        scannerView.resumeCameraPreview(MainActivity.this);


                    }
                })
                .show();
        scannerView.resumeCameraPreview(this);
        dialogstatus=true;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (dialogstatus)
        {
            scannerView.stopCamera();
            scannerView.startCamera();
            dialog.hide().dismiss();

            if (scannerView == null) {
                scannerView = new ZXingScannerView(this);
                setContentView(scannerView);
            }
            scannerView.setResultHandler(this);
            scannerView.startCamera();
            Log.e("back","bloack");
            dialogstatus=false;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.gen:
                startActivity(new Intent(MainActivity.this, Generater.class));
        }
        return true;
    }
}
