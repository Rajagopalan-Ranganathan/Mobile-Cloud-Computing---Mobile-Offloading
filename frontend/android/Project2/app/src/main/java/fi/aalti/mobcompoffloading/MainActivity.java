

package fi.aalti.mobcompoffloading;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.login.LoginManager;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import fi.aalti.mobilecompoffloading.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rv;
    private Uri img_URI;
    public String token;
    private static final int CAMERA = 1;
    private static final int GALLERY = 2;
    private String mode;
    private boolean asynTaskDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setTitle("Applications");

        rv = (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        Button fblog = (Button) findViewById(R.id.fb_button);
        fblog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                //i.putExtra("token", this.token);
                finish();
                startActivity(i);
            }
        });

        Button rlocal = (Button) findViewById(R.id.radio_local);
        rlocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Info", "Local Mode");
                mode = "LOCAL";

            }
        });


        Button rremote = (Button) findViewById(R.id.radio_remote);
        rremote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Info", "remote Mode");
                mode = "REMOTE";

            }
        });


        Button rbench = (Button) findViewById(R.id.radio_bench);
        rbench.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Info", "bench Mode");
                mode = "BENCH";

            }
        });


        // Button Click Event
        Button mCamButton = (Button) findViewById(R.id.camera_button);
        mCamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("logging", "clicked signInButton");
                String img_FileName = "test.jpg";
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Images.Media.TITLE, img_FileName);
                cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                img_URI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, img_URI);
                startActivityForResult(intent, CAMERA);
            }
        });

        Button mGalButton = (Button) findViewById(R.id.gallery_button);
        mGalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("logging", "clicked signInButton");
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, GALLERY);


            }
        });

        // Call Initialize data metjod to get the data ready
        initializeData();

    }

    private ArrayList<Uri> mURI_Array;
    private backendApplicationService appService;
    private List<History> histories;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("ResultCode : ", "" + resultCode);
        Log.d("RequestCode : ", "" + requestCode);
        mURI_Array = new ArrayList<Uri>();
        switch (requestCode) {
            case GALLERY:
                if (resultCode == RESULT_OK) {
                    //inspect(data.getData());
                    if (data.getData() != null) {
                        Uri mImageUri = data.getData();
                        mURI_Array.add(mImageUri);
                    } else {
                        if (data.getClipData() != null) {
                            ClipData mClipData = data.getClipData();
                            for (int i = 0; i < mClipData.getItemCount(); i++) {
                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                mURI_Array.add(uri);
                                Log.d("MULTIPLE", uri.toString());
                            }
                        }
                    }
                    new DoOCR(this, mode).execute(mURI_Array);

                }
                break;
            case CAMERA:
                Log.d("CAMERA", "inside#" + resultCode + "#" + img_URI.toString());
                if (resultCode == RESULT_OK) {
                    if (img_URI != null) {
                        mURI_Array.add(img_URI);
                        Log.d("CAMERA", img_URI.toString());
                    }
                    new DoOCR(this, mode).execute(mURI_Array);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    private void initializeData() {

        try {
            //Get the data passed from the login activity i.e : Toen
            Bundle extras = getIntent().getExtras();
            token = "";

            //Get token from intent from LoginActivity
            // The Header for this request should be like "Bearer "token""
            if (extras.containsKey("token")) {
                token = "Bearer " + extras.getString("token");

                //Save token in SharedPreferences
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("token", token);
                editor.apply();
            }

            //No token in intent, probably intent is from multivnc
            //Read token from sharedPreferences
            else {
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                //TODO check if token is in sharedPreferences else go back to LoginActivity
                token = sharedPref.getString("token", "");
            }

            //Communicate with the backend to retrieve the Histroy
            //Histroy items are retrived and displayed in the card/recyl;er view using the adapter.
            appService = new backendApplicationClient(this, token).getBackendApplicationService();
            final Call<List<History>> historyItems = appService.getHistory();

            historyItems.enqueue(new Callback<List<History>>() {

                @Override
                public void onResponse(Call<List<History>> call, Response<List<History>> response) {
                    try {
                        histories = response.body();

                        //Check if List is not null
                        if (histories == null) {
                            //init as empty list
                            histories = new ArrayList<History>();
                        }

                        Log.d("response in:", response.code() + "");

                        rv.setAdapter(new HistoryAdapter(getApplicationContext(), histories, MainActivity.this));


                    } catch (NullPointerException ex) {
                        Log.e("BackendConnection", "Response from backend is null");
                    }
                }

                @Override
                public void onFailure(Call<List<History>> call, Throwable t) {
                    // Log error here since request failed
                    Log.e("Error", t.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

        }


    }

}

class DoOCR extends AsyncTask<ArrayList<Uri>, Void, String> {
    private final String TAG = "OCRTask";
    private Context context;
    private ProgressDialog dialog;
    private String datapath = "";
    private TessBaseAPI tessBaseAPI;
    ArrayList<Uri> mArrayUri;
    private String mode = "";
    private String token;
    private boolean asyncTaskdone = false;
    private ArrayList<String> srcImages;
    private ArrayList<String> ocrImages;

    public DoOCR(Context ctx, String mode) {
        this.context = ctx;
        this.mode = mode;
        this.token = ((MainActivity) ctx).token;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (mode.equals("REMOTE")) {
            dialog = new ProgressDialog(context);
            dialog.setMessage("OCR Processing - Remote Mode");
            dialog.setCancelable(false);
            dialog.show();
            Log.d("LOG", "Inside Remote OCR");

        }

        if (mode.equals("LOCAL")) {
            dialog = new ProgressDialog(context);
            dialog.setMessage("OCR Processing - Local Mode");
            dialog.setCancelable(false);
            dialog.show();
        }

        if (mode.equals("BENCH")) {
            dialog = new ProgressDialog(context);
            dialog.setMessage("OCR Processing - Benchmark Mode");
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG, result);
        super.onPostExecute(result);

        //Opens the Result screen
        if (mode.equals("LOCAL")) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Log.d("OCR Result String", result);
            Intent intent = new Intent(this.context, ResultScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Image.srcImages = srcImages;
            intent.putExtra("RESULTTEXT", result);
            intent.putExtra("MODE", this.mode);
            //intent.putExtra("CREATTIME",creattime);
            this.context.startActivity(intent);
        }

        if (mode.equals("REMOTE")) {

            if (asyncTaskdone == true) {

                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.d("OCR Result String", result);

                Intent intent = new Intent(this.context, ResultScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Image.srcImages = srcImages;
                // intent.putExtra("IMAGES", srcImages);
                intent.putExtra("RESULTTEXT", result);
                intent.putExtra("MODE", this.mode);
                //intent.putExtra("CREATTIME",creattime);
                this.context.startActivity(intent);
                Log.d("LOG", "Inside Remote OCR");
            }

        }


        if (mode.equals("BENCH")) {

            if (asyncTaskdone == true) {

                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.d("OCR Result String", result);
                Intent intent = new Intent(this.context, ResultScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("RESULTTEXT", result);
                intent.putExtra("MODE", this.mode);
                //intent.putExtra("CREATTIME",creattime);
                this.context.startActivity(intent);
            }
        }
    }

    protected String doInBackground(ArrayList<Uri>... params) {

        long startMillis = System.currentTimeMillis();
        tessBaseAPI = new TessBaseAPI();
        final StringBuilder ocrText = new StringBuilder();
        String language = "eng";
        try {

            datapath = this.context.getFilesDir() + "/tesseract/";
            tessBaseAPI = new TessBaseAPI();
            check_file_path(new File(datapath + "tessdata/"));
            tessBaseAPI.init(datapath, language);
            mArrayUri = (ArrayList<Uri>) params[0];

            if (mode.equals("BENCH")) {


                int imgindx = 1;
                long startTime, endTime, minTime, maxTime, avgTime, procTime, totTime;
                String minTimeStr, maxTimeStr;
                minTimeStr = maxTimeStr = "";
                ArrayList<Long> times = new ArrayList<Long>();
                procTime = minTime = maxTime = avgTime = totTime = 0;
                for (Uri uri : mArrayUri) {
                    startTime = System.currentTimeMillis();
                    check_and_do_ocr(uri);
                    endTime = System.currentTimeMillis();
                    procTime = endTime - startTime;
                    times.add(procTime);
                    totTime = totTime + procTime;
                    imgindx++;

                }
                avgTime = totTime / mArrayUri.size();
                for (int i = 0; i < times.size(); i++) {

                    procTime = times.get(i);
                    if (i == 0) {
                        minTime = maxTime = procTime;
                        minTimeStr = "Minimum:" + minTime + "ms (" + (i + 1) + ") ";
                        maxTimeStr = "Maximum:" + maxTime + "ms (" + (i + 1) + ") ";

                    } else {
                        if (procTime <= minTime) {
                            minTime = procTime;
                            minTimeStr = "Minimum:" + minTime + "ms (" + (i + 1) + ") ";
                        }
                        if (procTime >= maxTime) {
                            maxTime = procTime;
                            maxTimeStr = "Maximum:" + maxTime + "ms (" + (i + 1) + ") ";
                        }
                    }


                }
                int res = 0;
                for (int i = 0; i < times.size(); i++) {

                    res += Math.pow(times.get(i) - avgTime, 2);

                }

                res = (int) Math.sqrt((int) res / times.size());


                Log.d("mintime", minTimeStr);

                ocrText.append("Number of processed images: ");
                ocrText.append(imgindx - 1);
                ocrText.append("\n");
                ocrText.append("Local Mode: \n");
                ocrText.append("Processing time: ");
                ocrText.append(totTime / (imgindx - 1));
                ocrText.append(" ms    ");
                ocrText.append("Standard Deviation: ");
                ocrText.append(res);
                ocrText.append(" ms    \n");
                ocrText.append(minTimeStr);
                ocrText.append(maxTimeStr);

                imgindx = 1;
                try {
                    final java.util.ArrayList<image64> imglist = new java.util.ArrayList<image64>();
                    final ArrayList<String> images = new ArrayList<String>();

                    for (Uri uri : mArrayUri) {

                        InputStream is = null;
                        String text = "";

                        is = this.context.getContentResolver().openInputStream(uri);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        options.inSampleSize = 2;
                        options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
                        Bitmap bm = BitmapFactory.decodeStream(is, null, options);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                        byte[] byteArrayImage = baos.toByteArray();

                        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.NO_WRAP);
                        String temp = "data:image/png;base64," + encodedImage;
                        Log.d("Enooded image:", encodedImage);

                        image64 img = new image64(encodedImage);
                        images.add(temp);
                        Log.d("Enooded image:", temp);


                    }
                    ocrreq req = new ocrreq(images);

                    //communicate with the nackend to get the OCR processing

                    backendApplicationService appService = new backendApplicationClient(this.context, this.token).getBackendApplicationService();
                    Call<ocrRes> authService = appService.getOcr(req);


                    authService.enqueue(new Callback<ocrRes>() {

                        @Override
                        public void onResponse(Call<ocrRes> call, Response<ocrRes> response) {
                            try {
                                ocrRes resp = response.body();
                                ArrayList<String> ocrResult = new ArrayList<String>();
                                ocrResult = resp.getOcr();

                                int i = 0;
                                long startTime, endTime, minTime, maxTime, avgTime, procTime, totTime;
                                String minImgStr, maxImgStr, minTimeStr, maxTimeStr;
                                minImgStr = maxImgStr = minTimeStr = maxTimeStr = "";
                                startTime = endTime = minTime = maxTime = avgTime = procTime = totTime = 0;

                                ArrayList<Integer> imgSizeList = new ArrayList<Integer>();


                                for (i = 0; i < resp.getThumbnails().size(); i++) {

                                    byte[] imgBytes = Base64.decode(resp.getThumbnails().get(i), Base64.NO_WRAP);
                                    imgSizeList.add(imgBytes.length);

                                }
                                int imgsize, totSize, minSize, maxSize;
                                imgsize = totSize = minSize = maxSize = 0;
                                for (i = 0; i < imgSizeList.size(); i++) {
                                    imgsize = imgSizeList.get(i);
                                    if (i == 0) {
                                        minSize = maxSize = imgsize;
                                        minImgStr = "Minimum:" + minSize + "bytes (" + (i + 1) + ") ";
                                        maxImgStr = "Maximum:" + maxSize + "bytes (" + (i + 1) + ") ";

                                    } else {
                                        if (imgsize <= minSize) {
                                            minSize = imgsize;
                                            minImgStr = "Minimum:" + minSize + "bytes (" + (i + 1) + ") ";
                                        }
                                        if (imgsize >= maxSize) {
                                            maxSize = imgsize;
                                            maxImgStr = "Maximum:" + maxSize + "bytes (" + (i + 1) + ") ";
                                        }
                                    }

                                    totSize += imgsize;
                                }

                                int resimg = 0;
                                for (i = 0; i < imgSizeList.size(); i++) {

                                    resimg += Math.pow(imgSizeList.get(i) - avgTime, 2);

                                }

                                resimg = (int) Math.sqrt((int) resimg / imgSizeList.size());

                                for (i = 0; i < resp.getBenchmarks().size(); i++) {
                                    procTime = Integer.parseInt(resp.getBenchmarks().get(i));
                                    if (i == 0) {
                                        minTime = maxTime = procTime;
                                        minTimeStr = "Minimum:" + minTime + "ms (" + (i + 1) + ") ";
                                        maxTimeStr = "Maximum:" + maxTime + "ms (" + (i + 1) + ") ";

                                    } else {
                                        if (procTime <= minTime) {
                                            minTime = procTime;
                                            minTimeStr = "Minimum:" + minTime + "ms (" + (i + 1) + ") ";
                                        }
                                        if (procTime >= maxTime) {
                                            maxTime = procTime;
                                            maxTimeStr = "Maximum:" + maxTime + "ms (" + (i + 1) + ") ";
                                        }
                                    }

                                    totTime += procTime;
                                }

                                int res = 0;
                                for (i = 0; i < resp.getBenchmarks().size(); i++) {

                                    res += Math.pow(Integer.parseInt(resp.getBenchmarks().get(i)) - avgTime, 2);

                                }

                                res = (int) Math.sqrt((int) res / resp.getBenchmarks().size());

                                Log.d("mintime", minTimeStr);

                                ocrText.append("\n\n");
                                ocrText.append("Remote Mode: \n");
                                ocrText.append("Processing time: ");
                                ocrText.append(totTime / resp.getBenchmarks().size());
                                ocrText.append(" ms    ");
                                ocrText.append("Standard Deviation: ");
                                ocrText.append(res);
                                ocrText.append(" ms    \n");
                                ocrText.append(minTimeStr);
                                ocrText.append(maxTimeStr);
                                ocrText.append("\n\n");
                                ocrText.append("Exchanged Data: ");
                                ocrText.append(totSize / imgSizeList.size());
                                ocrText.append(" bytes    ");
                                ocrText.append("Standard Deviation: ");
                                ocrText.append(resimg);
                                ocrText.append(" bytes    \n");
                                ocrText.append(minImgStr);
                                ocrText.append(maxImgStr);
                                ocrText.append("\n");

                                Log.d("resp:", response.code() + "");
                                asyncTaskdone = true;
                                onPostExecute(ocrText.toString());

                               } catch (NullPointerException ex) {
                                asyncTaskdone = true;
                                onPostExecute("");
                                Log.e("BackendConnection", "Response from backend is null");
                            }
                        }

                        @Override
                        public void onFailure(Call<ocrRes> call, Throwable t) {
                            asyncTaskdone = true;
                            onPostExecute("");
                            Log.e("Error", t.toString());
                        }
                    });

                } catch (Exception ex) {
                    asyncTaskdone = true;
                    Log.d("Error", ex.getMessage());
                }


            }


            if (mode.equals("LOCAL")) {
                int imgindx = 1;
                for (Uri uri : mArrayUri) {
                    ocrText.append("\n");
                    ocrText.append("Image : " + (imgindx));
                    ocrText.append("\n");
                    ocrText.append(check_and_do_ocr(uri));
                    ocrText.append("\n");
                    ocrText.append("===================================");
                    imgindx++;

                    srcImages = new ArrayList<String>();

                    InputStream is = null;
                    String text = "";

                    is = this.context.getContentResolver().openInputStream(uri);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    options.inSampleSize = 2;
                    options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
                    Bitmap bm = BitmapFactory.decodeStream(is, null, options);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                    byte[] byteArrayImage = baos.toByteArray();

                    String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                    String temp = "data:image/png;base64," + encodedImage;
                    Log.d("Enooded image:", encodedImage);
                    srcImages.add(encodedImage);

                }
            }


            if (mode.equals("REMOTE")) {
                int imgindx = 1;
                try {
                    java.util.ArrayList<image64> imglist = new java.util.ArrayList<image64>();
                    srcImages = new ArrayList<String>();

                    ocrImages = new ArrayList<String>();

                    for (Uri uri : mArrayUri) {

                        InputStream is = null;
                        String text = "";

                        is = this.context.getContentResolver().openInputStream(uri);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        options.inSampleSize = 2;
                        options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
                        Bitmap bm = BitmapFactory.decodeStream(is, null, options);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                        byte[] byteArrayImage = baos.toByteArray();

                        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.NO_WRAP);
                        String encodedImage1 = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                        String temp = "data:image/png;base64," + encodedImage;
                        Log.d("Enooded image:", encodedImage);
                        srcImages.add(encodedImage1);
                        ocrImages.add(temp);
                        Log.d("Enooded image:", encodedImage);


                    }
                    ocrreq req = new ocrreq(ocrImages);
                    //Communicate with the backend and perform the ocr and retreive the result
                    backendApplicationService appService = new backendApplicationClient(this.context, this.token).getBackendApplicationService();
                    Call<ocrRes> authService = appService.getOcr(req);

                    authService.enqueue(new Callback<ocrRes>() {

                        @Override
                        public void onResponse(Call<ocrRes> call, Response<ocrRes> response) {
                            try {
                                ocrRes resp = response.body();
                                ArrayList<String> ocrResult = new ArrayList<String>();
                                ocrResult = resp.getOcr();

                                int imgindx = 0;
                                ocrText.append("Creation Time: ");
                                ocrText.append(resp.getCreationTime());
                                ocrText.append("\n\n");
                                for (imgindx = 0; imgindx < ocrResult.size(); imgindx++) {
                                    ocrText.append("\n\n");
                                    ocrText.append("Image : " + (imgindx + 1));
                                    ocrText.append("\n");
                                    //ocrText.append("========================================================");
                                    ocrText.append(ocrResult.get(imgindx).toString());
                                    ocrText.append("\n");
                                    ocrText.append("===================================");

                                }

                                Log.d("OCR Result", ocrResult.get(0).toString());
                                Log.d("Creation Time:", resp.getCreationTime());
                                Log.d("Bench Marks:", resp.getBenchmarks().get(0).toString());
                                Log.d("resp:", response.code() + "");
                                asyncTaskdone = true;
                                onPostExecute(ocrText.toString());
                            } catch (NullPointerException ex) {
                                asyncTaskdone = true;
                                onPostExecute("");
                                Log.e("BackendConnection", "Response from backend is null");
                            }
                        }

                        @Override
                        public void onFailure(Call<ocrRes> call, Throwable t) {
                            asyncTaskdone = true;
                            onPostExecute("");
                            Log.e("Error", t.toString());
                        }
                    });

                } catch (Exception ex) {
                    asyncTaskdone = true;
                    Log.d("Error", ex.getMessage());
                }


            }
            long endMillis = System.currentTimeMillis();
            long deltaMillis = endMillis - startMillis;
            Log.i(TAG, "" + deltaMillis);
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
        Log.d("OCR Result before", ocrText.toString());
        return ocrText.toString();
    }

    private void check_file_path(File dir) {
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        if (dir.exists()) {
            String datafilepath = datapath + "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = this.context.getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String check_and_do_ocr(Uri uri) {
        InputStream is = null;
        String text = "";
        try {
            is = this.context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;
            options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            tessBaseAPI.setImage(bitmap);
            text = tessBaseAPI.getUTF8Text();

            Log.d(TAG, text);
            bitmap.recycle();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            Log.d("ERROR:", ex.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
        }
        return text;
    }
}