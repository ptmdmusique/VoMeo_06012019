package com.dghan.vomeo.UI;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.dghan.vomeo.Database.DatabaseHandler;
import com.dghan.vomeo.Database.Word;
import com.dghan.vomeo.R;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {
    public ArrayList<Word> listCard = null;
    private Context context;
    public DatabaseHandler databaseHandler = null;

    public void syncDatabaseHandler(){
        databaseHandler = DatabaseHandler.getInstance();
        listCard = databaseHandler.words;
    }
    public ListAdapter(Context context) {//ArrayList<Word> listCard, Context context) {
        //this.listCard = listCard;
        this.context = context;
    }

    @Override
    public int getCount() {
        return listCard.size();
    }

    @Override
    public Word getItem(int i) {
        return listCard.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    //databaseHandler must be sync before used
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View row;
        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.cards_list, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.cName = row.findViewById(R.id.cName);
            viewHolder.cDes = row.findViewById(R.id.cDes);
            viewHolder.cImg = row.findViewById(R.id.cImage);
            viewHolder.cSpeaker = row.findViewById(R.id.speaker);
            viewHolder.cNote = row.findViewById(R.id.note);
            viewHolder.cFavorite = row.findViewById(R.id.favorite);
            row.setTag(viewHolder);
        } else {
            row = convertView;
            viewHolder = (ViewHolder) row.getTag();
        }
        final Word card = getItem(position);

        viewHolder.cName.setText(card.term);
        viewHolder.cDes.setText(card.definition);
        viewHolder.cImg.setImageResource(card.Img);

        final Handler handler = new Handler();
        viewHolder.cSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.cSpeaker.setImageDrawable(context.getResources().getDrawable(R.drawable.speaker_on));
                databaseHandler.playAudio(card.term);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.cSpeaker.setImageDrawable(context.getResources().getDrawable(R.drawable.speaker));
                    }
                }, 1000);
                //Time delay
            }});

        viewHolder.cNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(parent.getContext(), "This is Note", Toast.LENGTH_SHORT).show();
                BottomSheetExample bottomSheet = new BottomSheetExample();
                bottomSheet.show(((FragmentActivity) context).getSupportFragmentManager(), "bottom Sheet");

            }
        });


        if(card.favorite)
            viewHolder.cFavorite.setImageDrawable(context.getResources().getDrawable(R.drawable.heart_button));
        else
            viewHolder.cFavorite.setImageDrawable(context.getResources().getDrawable(R.drawable.empty_heart_button));
        final int[] flag = {0};
        viewHolder.cFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag [0] == 0) {
                    viewHolder.cFavorite.setImageDrawable(context.getResources().getDrawable(R.drawable.heart_button));
                    Toast.makeText(parent.getContext(), "Favorite is signed", Toast.LENGTH_SHORT).show();
                    flag[0] = 1;
                }
                else if(flag[0] == 1){
                    viewHolder.cFavorite.setImageDrawable(context.getResources().getDrawable(R.drawable.empty_heart_button));
                    Toast.makeText(parent.getContext(), "Favorite is canceled", Toast.LENGTH_SHORT).show();
                    flag[0] = 0;
                }
                databaseHandler.toggleFavorite(position);
                Log.e("favorite", databaseHandler.words.get(position).term);
                Log.e("favorite", String.valueOf(databaseHandler.words.get(position).favorite));
            }
        });

        return row;
    }
}
