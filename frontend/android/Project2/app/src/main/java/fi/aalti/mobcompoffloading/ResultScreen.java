package fi.aalti.mobcompoffloading;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import fi.aalti.mobilecompoffloading.R;

public class ResultScreen extends AppCompatActivity {

    private String resultText="";
    private String mode="";
    private ArrayList<String> arrayImg;
    private Uri img_URI;
    private static final int CAMERA = 1;
    private static final int GALLERY = 2;
    private String creattime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_screen);
        Toolbar tb_Main= (Toolbar) findViewById(R.id.toolbar_Main);
        setSupportActionBar(tb_Main);
        Bundle bundle = getIntent().getExtras();
        if (bundle!=null)
        {

            //arrayImg = (ArrayList<String>) bundle.get("IMAGES");
            resultText = bundle.getString("RESULTTEXT");
            mode =bundle.getString("MODE");
            creattime = bundle.getString("CREATTIME");
        }

        TextView ResultText = ((TextView)findViewById(R.id.resultText));
        ResultText.setText(resultText);
        ResultText.setMovementMethod(new ScrollingMovementMethod());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        //Date d = new Date(creattime);
        try {
            //Date date = sdf.parse(creattime);
            long l = Long.parseLong(creattime);
            //mytimeAsLong = creattime
            sdf.format(new Date(l));
            //getSupportActionBar().setTitle(dateFormat.format(date));
            getSupportActionBar().setTitle(sdf.format(new Date(l)));

           // long l = Long.parseLong(creattime);
            //mytimeAsLong = creattime
//            sdf.format(new Date(l));
//            //getSupportActionBar().setTitle(dateFormat.format(date));
//            //getSupportActionBar().setTitle(sdf.format(new Date(l)));
//
//            Date date = new Date(l * 1000L);
//            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//            //format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
//            String formatted = format.format(date);
//            getSupportActionBar().setTitle(formatted);


        }  catch (Exception e) {

            getSupportActionBar().setTitle(dateFormat.format(new Date()));
        }

        //getSupportActionBar().setTitle(dateFormat.format(date));

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ocr_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tb_Logout:
                //TODO : log out to be implemented
                LoginManager.getInstance().logOut();
                Intent i = new Intent(ResultScreen.this,LoginActivity.class);
                //i.putExtra("token", this.token);
                finish();
                startActivity(i);
                return true;
            case R.id.tb_Save:
                SaveFile();
                return true;
            case R.id.tb_Preview:
                preview();
                return true;
//            case R.id.tb_retake:
//                retake();
//                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void preview()
    {

        Intent i = new Intent(ResultScreen.this,Preview.class);
        finish();
        startActivity(i);

    }

    private void SaveFile() {
        String saveFileName = "ocr_result.txt";
        if (Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState())) {
            String storagePath = Environment.getExternalStorageDirectory().toString();
            File file = new File(storagePath, saveFileName);
            OutputStream outStream ;
            try {
                outStream = new FileOutputStream(file);
                outStream.write(resultText.getBytes());
                outStream.close();

                Toast.makeText(ResultScreen.this, "File saved at " + saveFileName, Toast.LENGTH_SHORT).show();
            }catch(Exception ex)
            {
                Toast.makeText(ResultScreen.this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(ResultScreen.this, "Permission issue", Toast.LENGTH_SHORT).show();
        }

    }

//    private void retake() {
//
//                Log.i("logging", "clicked signInButton");
//                String img_FileName = "test.jpg";
//                ContentValues cv = new ContentValues();
//                cv.put(MediaStore.Images.Media.TITLE, img_FileName);
//                cv.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
//                img_URI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
//                Intent intent = new Intent();
//                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,img_URI);
//                startActivityForResult(intent,CAMERA);
//
//    }
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d("ResultCode : ", ""+resultCode);
//        Log.d("RequestCode : ", ""+requestCode);
//        ArrayList<Uri> mURI_Array = new ArrayList<Uri>();
//        switch (requestCode) {
//            case GALLERY:
//                if (resultCode == RESULT_OK) {
//                    //inspect(data.getData());
//                    if(data.getData()!=null){
//                        Uri mImageUri=data.getData();
//                        mURI_Array.add(mImageUri);
//                    }
//                    else
//                    {
//                        if(data.getClipData()!=null){
//                            ClipData mClipData=data.getClipData();
//                            for(int i=0;i<mClipData.getItemCount();i++){
//                                ClipData.Item item = mClipData.getItemAt(i);
//                                Uri uri = item.getUri();
//                                mURI_Array.add(uri);
//                                Log.d("MULTIPLE", uri.toString());
//                            }
//                        }
//                    }
//                    new DoOCR(this,mode).execute(mURI_Array);
//
//                };
//                break;
//            case CAMERA:
//                Log.d("CAMERA", "inside#"+resultCode+"#"+img_URI.toString());
//                if (resultCode == RESULT_OK) {
//                    if (img_URI != null) {
//                        mURI_Array.add(img_URI);
//                        Log.d("CAMERA", img_URI.toString());
//                    }
//                    new DoOCR(this,mode).execute(mURI_Array);
//                }
//                break;
//            default:
//                super.onActivityResult(requestCode, resultCode, data);
//                break;
//        }
//
//    }
//
}
