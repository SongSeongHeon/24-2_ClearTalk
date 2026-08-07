package com.example.ct_001;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LearningDataAdapter extends RecyclerView.Adapter<LearningDataAdapter.ViewHolder> {

    private List<String> dates;        // 날짜 데이터 리스트
    private List<Integer> accuracies; // 일치율 데이터 리스트

    // 생성자: 어댑터에 데이터 전달
    public LearningDataAdapter(List<String> dates, List<Integer> accuracies) {
        this.dates = dates;
        this.accuracies = accuracies;
    }

    // ViewHolder 클래스 정의
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, accuracyTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.tv_date);
            accuracyTextView = itemView.findViewById(R.id.tv_accuracy);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_learning_data.xml을 기반으로 ViewHolder 생성
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 데이터 바인딩
        holder.dateTextView.setText(dates.get(position));               // 날짜 설정
        holder.accuracyTextView.setText(accuracies.get(position) + "%"); // 일치율 설정
    }

    @Override
    public int getItemCount() {
        return dates.size(); // 리스트 크기 반환
    }
}
