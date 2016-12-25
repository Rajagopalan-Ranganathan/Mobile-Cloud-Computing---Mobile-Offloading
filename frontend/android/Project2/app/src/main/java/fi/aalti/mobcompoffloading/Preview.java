package fi.aalti.mobcompoffloading;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;

import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.loader.DefaultImageLoader;

import java.util.ArrayList;
import java.util.List;

import fi.aalti.mobilecompoffloading.R;


public class Preview extends AppCompatActivity {
    private String resultText="";
    private ArrayList<String> arrayImg;
    private ArrayList<String> images = new ArrayList<String>();


    private ScrollGalleryView scrollGalleryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
            Bundle bundle = getIntent().getExtras();
            if (bundle!=null)
            {
                arrayImg = Image.srcImages;
            }
        arrayImg = Image.srcImages;
        if(arrayImg != null) {
            for (int i = 0; i < arrayImg.size(); i++)
                images.add(arrayImg.get(i).toString());


            List<MediaInfo> infos = new ArrayList<>(images.size());

            scrollGalleryView = (ScrollGalleryView) findViewById(R.id.scroll_gallery_view);
            for (String url : images)
                scrollGalleryView
                        .setThumbnailSize(100)
                        .setZoom(true)
                        .setFragmentManager(getSupportFragmentManager())
                        .addMedia(MediaInfo.mediaLoader(new DefaultImageLoader(toBitmap(url))))
                        .addMedia(infos);
        }
    }

    private Bitmap toBitmap(int image) {
        return ((BitmapDrawable) getResources().getDrawable(image)).getBitmap();
    }
    private Bitmap toBitmap(String image){
        byte[] imageByteArray = Base64.decode(image, Base64.DEFAULT);
        return  BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);

    }
 }




















