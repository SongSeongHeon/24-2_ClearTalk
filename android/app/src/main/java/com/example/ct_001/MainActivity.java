package com.example.ct_001;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;


public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore firestore; // Firestore instance

    private LinearLayout lifeSubcategories, foodSubcategories, workSubcategories, financeSubcategories;
    private ImageView lifeDropdown, foodDropdown, workDropdown, financeDropdown;

    private String selectedKeyword = null; // Selected keyword to pass
    private String selectedCategory = null;
    private LinearLayout[] allSubcategories;
    private ImageView[] allDropdowns;
    private Toast currentToast; // Toast 중복 방지 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firestore initialization
        firestore = FirebaseFirestore.getInstance();

        // 네비게이션 버튼 설정
        setupNavigationButtons();

        // 드롭다운 메뉴 설정
        setupDropdownMenus();

        // 키워드 버튼 클릭 이벤트 처리
        setupKeywordSelection();

        // Start learning button
        Button startLearningButton = findViewById(R.id.startLearningButton);
        startLearningButton.setOnClickListener(v -> {
            if (selectedKeyword == null || selectedCategory == null) {
                Toast.makeText(MainActivity.this, "키워드를 선택하세요!", Toast.LENGTH_SHORT).show();
            } else {
                // Start LearningActivity_1 and pass selected keyword
                Intent intent = new Intent(MainActivity.this, LearningActivity_1.class);
                intent.putExtra("keyword", selectedKeyword);
                intent.putExtra("category", selectedCategory);
                startActivity(intent);
            }
        });
    }

    private void setupNavigationButtons() {
        setupNavigationButton(R.id.learnButton, MainActivity.class, false);
        setupNavigationButton(R.id.reviewButton, ReviewActivity.class, false);
        setupNavigationButton(R.id.performanceButton, ProgressActivity.class, true);
    }

    private void setupNavigationButton(int buttonId, Class<?> targetActivity, boolean shouldFinish) {
        Button button = findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> {
                if (targetActivity.equals(MainActivity.class)) {
                    showToast("현재 화면입니다.");
                } else {
                    Intent intent = new Intent(MainActivity.this, targetActivity);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    if (shouldFinish) {
                        finish();
                    }
                }
            });
        }
    }

    // Toast 메시지 표시 메서드
    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel(); // 기존 Toast 취소
        }
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private void setupDropdownMenus() {
        // XML에서 View 가져오기
        lifeSubcategories = findViewById(R.id.lifeSubcategories);
        foodSubcategories = findViewById(R.id.foodSubcategories);
        workSubcategories = findViewById(R.id.workSubcategories);
        financeSubcategories = findViewById(R.id.financeSubcategories);

        lifeDropdown = findViewById(R.id.lifeDropdown);
        foodDropdown = findViewById(R.id.foodDropdown);
        workDropdown = findViewById(R.id.workDropdown);
        financeDropdown = findViewById(R.id.financeDropdown);

        allSubcategories = new LinearLayout[]{lifeSubcategories, foodSubcategories, workSubcategories, financeSubcategories};
        allDropdowns = new ImageView[]{lifeDropdown, foodDropdown, workDropdown, financeDropdown};

        setupDropdownClickListener(lifeDropdown, lifeSubcategories);
        setupDropdownClickListener(foodDropdown, foodSubcategories);
        setupDropdownClickListener(workDropdown, workSubcategories);
        setupDropdownClickListener(financeDropdown, financeSubcategories);
    }

    private void setupDropdownClickListener(ImageView dropdown, LinearLayout subcategory) {
        if (dropdown != null && subcategory != null) {
            dropdown.setOnClickListener(v -> {
                for (LinearLayout layout : allSubcategories) {
                    if (layout != subcategory && layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                    }
                }
                toggleVisibility(subcategory);
            });
        } else {
            showToast("드롭다운 항목 초기화 오류");
        }
    }

    private void setupKeywordSelection() {
        // 키워드 버튼 클릭 이벤트 설정 (예시: 각 카테고리에서 버튼 처리)
        setKeywordSelection(lifeSubcategories, "생활, 소비");
        setKeywordSelection(foodSubcategories, "음식, 요리");
        setKeywordSelection(workSubcategories, "업무, 협업");
        setKeywordSelection(financeSubcategories, "금융, 자산");
    }

    private void setKeywordSelection(LinearLayout categoryLayout, String category) {
        if (categoryLayout != null) {
            for (int i = 0; i < categoryLayout.getChildCount(); i++) {
                View child = categoryLayout.getChildAt(i);
                if (child instanceof Button) {
                    child.setOnClickListener(v -> {
                        Button button = (Button) v;
                        selectedKeyword = button.getText().toString(); // 선택된 키워드 저장
                        selectedCategory = category; // 카테고리 저장
                        showToast(selectedCategory + ": " + selectedKeyword + " 선택됨");
                    });
                }
            }
        }
    }

    private void toggleVisibility(LinearLayout subcategories) {
        if (subcategories.getVisibility() == View.VISIBLE) {
            subcategories.setVisibility(View.GONE);
            showToast("항목 숨김"); // 테스트용 메시지
        } else {
            subcategories.setVisibility(View.VISIBLE);
            showToast("항목 표시"); // 테스트용 메시지
        }
    }
}
