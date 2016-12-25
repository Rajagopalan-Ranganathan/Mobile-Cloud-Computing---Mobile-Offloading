package fi.aalti.mobcompoffloading;



import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import fi.aalti.mobilecompoffloading.R;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryCardReclyerViewHolder> {

    public static class HistoryCardReclyerViewHolder extends RecyclerView.ViewHolder {


        CardView cardView;
        TextView ocrTextView;
        ImageView histAppIcon;

        HistoryCardReclyerViewHolder(View itemView) {
            super(itemView);
            itemView.getContext();
            cardView = (CardView)itemView.findViewById(R.id.cv);
            ocrTextView = (TextView)itemView.findViewById(R.id.ocr);
            histAppIcon = (ImageView)itemView.findViewById(R.id.thumbnail);
        }
    }

    Context vmContext;
    List<History> histories;
    MainActivity mainActivity;

    HistoryAdapter(Context vmContext, List<History> VMListApps, MainActivity mainActivity){
        this.vmContext = vmContext;
        this.histories = VMListApps;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public HistoryCardReclyerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        HistoryCardReclyerViewHolder pvh = new HistoryCardReclyerViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(HistoryCardReclyerViewHolder histAppViewHolder, final int i) {

        //The name from the back end is x-y-applicationName, so we need to split based on "-" and then get the applicaiton name
        Log.d("HISTORYCARD", i+"");

        String ocrTrunc = histories.get(i).getTruncatedOCR();

        String ocrt = histories.get(histories.size()-(i+1)).getTruncatedOCR();

       // histAppViewHolder.ocrTextView.setText(ocrTrunc);
        histAppViewHolder.ocrTextView.setText(ocrt);

        byte[] imageByteArray = Base64.decode(histories.get(i).getThumbnails().get(0), Base64.DEFAULT);
        byte[] imgByteArray = Base64.decode(histories.get(histories.size()-(i+1)).getThumbnails().get(0), Base64.DEFAULT);

        Glide.with(vmContext)
                //.load(imageByteArray)
                .load(imgByteArray)
                .asBitmap()
                .placeholder(R.drawable.ic_launcher)
                .into(histAppViewHolder.histAppIcon);


        histAppViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("VMListItemClicked","Remote Application " + histories.get(i).getCreationTime());
                Intent intent = new Intent(vmContext,ResultScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Image.srcImages = histories.get(i).getSources();
                //intent.putExtra("IMAGES", histories.get(i).getSources());
                intent.putExtra("RESULTTEXT",histories.get(histories.size()-(i+1)).getOcr().toString());
                intent.putExtra("CREATTIME",histories.get(histories.size()-(i+1)).getCreationTime());
                vmContext.startActivity(intent);


            }
       }) ;
    }

   @Override
    public int getItemCount() {
      return histories.size();
        }
}
