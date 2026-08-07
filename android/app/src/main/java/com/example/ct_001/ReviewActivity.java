package com.example.ct_001;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class ReviewActivity extends AppCompatActivity {

    private Toast currentToast; // Toast 중복 방지 변수
    private ListView myFavoritesList; // 리스트뷰 변수
    private FirebaseFirestore firestore; // Firestore 인스턴스
    private List<String> favoriteItems = new ArrayList<>(); // 즐겨찾기 항목 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Firestore 초기화
        firestore = FirebaseFirestore.getInstance();

        // 리스트뷰 초기화
        myFavoritesList = findViewById(R.id.myFavoritesList);

        // 즐겨찾기 데이터 불러오기
        loadFavoriteItems();

        // 복습 시작 버튼 설정
        Button startReviewButton = findViewById(R.id.startReviewButton);
        startReviewButton.setOnClickListener(v -> {
            if (favoriteItems.isEmpty()) {
                showToast("즐겨찾기 항목이 없습니다.");
            } else {
                // 랜덤으로 항목 선택
                String randomKeyword = favoriteItems.get(new Random().nextInt(favoriteItems.size()));

                // LearningActivity_1로 데이터 전달
                Intent intent = new Intent(ReviewActivity.this, LearningActivity_1.class);
                intent.putExtra("keyword", randomKeyword);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 애니메이션 효과
            }
        });

        // 결과 초기화 버튼 설정
        Button clearResultsButton = findViewById(R.id.clearResultsButton);
        clearResultsButton.setOnClickListener(v -> {
            firestore.collection("Keywords")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            List<Map<String, Object>> subcategories = (List<Map<String, Object>>) document.get("subcategories");

                            if (subcategories != null) {
                                // Firestore 업데이트를 위한 새로운 서브카테고리 리스트
                                List<Map<String, Object>> updatedSubcategories = new ArrayList<>();

                                for (Map<String, Object> subcategory : subcategories) {
                                    // "isFavorite" 필드를 false로 설정
                                    if (subcategory.containsKey("isFavorite")) {
                                        subcategory.put("isFavorite", false);
                                    }
                                    updatedSubcategories.add(subcategory); // 업데이트된 서브카테고리 추가
                                }

                                // Firestore에 업데이트
                                firestore.collection("Keywords")
                                        .document(document.getId())
                                        .update("subcategories", updatedSubcategories)
                                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "isFavorite 초기화 성공: " + document.getId()))
                                        .addOnFailureListener(e -> Log.e("Firestore", "isFavorite 초기화 실패: " + e.getMessage()));
                            }
                        }

                        // 성공 메시지 표시
                        showToast("모든 즐겨찾기가 초기화되었습니다.");
                        loadFavoriteItems(); // 리스트뷰 업데이트
                    })
                    .addOnFailureListener(e -> showToast("결과 초기화 실패: " + e.getMessage()));
        });

        // 하단 네비게이션 버튼 설정
        setupNavigationButtons();
    }

    private void setupNavigationButtons() {
        setupNavigationButton(R.id.learnButton, MainActivity.class, false);
        setupNavigationButton(R.id.reviewButton, null, true);
        setupNavigationButton(R.id.performanceButton, ProgressActivity.class, false);
    }

    private void setupNavigationButton(int buttonId, Class<?> targetActivity, boolean isCurrent) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            if (isCurrent) {
                showToast("현재 화면입니다.");
            } else if (targetActivity != null) {
                Intent intent = new Intent(ReviewActivity.this, targetActivity);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 페이드 인/아웃 효과
            }
        });
    }

    // Firestore에서 즐겨찾기 항목 불러오기
    private void loadFavoriteItems() {
        favoriteItems.clear(); // 기존 데이터 초기화

        firestore.collection("Keywords")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        List<Map<String, Object>> subcategories = (List<Map<String, Object>>) document.get("subcategories");

                        if (subcategories != null) {
                            for (Map<String, Object> subcategory : subcategories) {
                                Boolean isFavorite = (Boolean) subcategory.get("isFavorite");

                                if (isFavorite != null && isFavorite) {
                                    String name = (String) subcategory.get("name");
                                    if (name != null) {
                                        favoriteItems.add(name); // name 추가
                                    }
                                }
                            }
                        }
                    }

                    // 리스트뷰 어댑터 설정
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_list_item_1,
                            favoriteItems
                    );
                    myFavoritesList.setAdapter(adapter);
                })
                .addOnFailureListener(e -> showToast("데이터 불러오기 실패: " + e.getMessage()));
    }

    // Toast 메시지 표시 메서드
    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel(); // 기존 Toast 취소
        }
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }
}
