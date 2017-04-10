package ru.supervital.test.itsc.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import itsc.test.supervital.ru.R;
import ru.supervital.test.itsc.adapter.HistoryListRecyclerAdapter;
import ru.supervital.test.itsc.adapter.Steps;
import ru.supervital.test.itsc.data.StepsDbHelper;

public class HistoryActivity extends AppCompatActivity {

    @BindView(R.id.lvMain)
    protected RecyclerView lvMain;
    protected ArrayList<Steps> stepsHistotry = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);
        StepsDbHelper mDbHelper = new StepsDbHelper(this);
        stepsHistotry = mDbHelper.getStepsPerDay();
        lvMain.setLayoutManager(new LinearLayoutManager(this));
        lvMain.setAdapter(new HistoryListRecyclerAdapter(stepsHistotry, this));
        mDbHelper.close();
    }
}
