package com.dghan.vomeo.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.Editable;
import android.text.NoCopySpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.TextWatcher;
import android.widget.Toast;

import com.dghan.vomeo.FullscreenActivity;
import com.dghan.vomeo.R;

public class BottomSheetExample extends BottomSheetDialogFragment implements NoCopySpan {
//    private  BottomSheetListener mListener;
    String txt_memo;
    Context c = FullscreenActivity.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        Button btnSave = v.findViewById(R.id.btn_save);
        Button btnClear = v.findViewById(R.id.btn_clear);
        final EditText txtNote = v.findViewById(R.id.memo);

        SharedPreferences preferences = c.getSharedPreferences("PREFS", 0);
        txt_memo = preferences.getString("memo", "");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt_memo= txtNote.getText().toString();

                SharedPreferences preferences = c.getSharedPreferences("PREFS", 0);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("memo", txt_memo);
                editor.commit();

                Toast.makeText(c, "memo saved", Toast.LENGTH_SHORT).show();

            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt_memo = "";
            }
        });

        return  v;
    }

}
