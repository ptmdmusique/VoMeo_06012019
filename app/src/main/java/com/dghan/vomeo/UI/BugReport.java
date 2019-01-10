package com.dghan.vomeo.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dghan.vomeo.R;

public class BugReport extends AppCompatActivity {

    EditText mBugTitle;
    EditText mBugContent;
    Button btnBugSubmit;
    String title, content, submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug_report);

        //anh xa
        mBugContent = (EditText) findViewById(R.id.bugContent);
        mBugTitle = (EditText) findViewById(R.id.bugTitle);
        btnBugSubmit = (Button) findViewById(R.id.bugSubmit);

        //
        btnBugSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBugTitle.getText().toString() == ""){
                    Toast.makeText(BugReport.this,"Empty Title", Toast.LENGTH_SHORT).show();
                }
                else if(mBugContent.getText().toString().equals("")){
                    Toast.makeText(BugReport.this,"Empty Content", Toast.LENGTH_SHORT).show();
                }
                else{
                    title = mBugTitle.getText().toString();
                    content = mBugContent.getText().toString();
                    Toast.makeText(BugReport.this,"Successfully Submitted", Toast.LENGTH_SHORT).show();
                    StringBuilder stringBuilder =  new StringBuilder();
                    stringBuilder.append(title + ":" + content);
                    submit = stringBuilder.toString();
                }
            }
        });

    }
}
