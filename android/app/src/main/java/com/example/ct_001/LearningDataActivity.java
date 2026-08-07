package com.example.ct_001;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LearningDataActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BarChart barChart;
    private LearningDataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        barChart = findViewById(R.id.bar_chart);

        loadLearningData();
    }

    private void loadLearningData() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users").child("user123");

        List<String> dates = new ArrayList<>();
        List<Integer> accuracies = new ArrayList<>();

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    String date = data.getKey();
                    Integer accuracy = data.child("accuracy").getValue(Integer.class);

                    dates.add(date);
                    accuracies.add(accuracy);
                }

                // RecyclerView 어댑터 설정
                adapter = new LearningDataAdapter(dates, accuracies);
                recyclerView.setAdapter(adapter);

                // BarChart 설정
                setupBarChart(barChart, dates, accuracies);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LearningDataActivity.this, "데이터 로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBarChart(BarChart barChart, List<String> dates, List<Integer> accuracies) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < accuracies.size(); i++) {
            entries.add(new BarEntry(i, accuracies.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "학습 성과");
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // X축 레이블 설정
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        barChart.invalidate(); // 차트 갱신
    }
}
