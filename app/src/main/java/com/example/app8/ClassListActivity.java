package com.example.app8;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClassListActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> accountList;
    private ArrayList<Integer> backgroundList; // Thêm danh sách giá trị background
    private ArrayList<Integer> studentCountList;

    private CustomClassListAdapter adapter; // Sử dụng CustomClassListAdapter
    private Connection connection;
    private static final int ADD_ACCOUNT_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_list_activity);

        listView = findViewById(R.id.listView);
        accountList = new ArrayList<>();
        backgroundList = new ArrayList<>(); // Khởi tạo danh sách background
        studentCountList = new ArrayList<>();
        adapter = new CustomClassListAdapter(this, accountList, backgroundList,studentCountList); // Sử dụng CustomClassListAdapter

        listView.setAdapter(adapter);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        connection = SQLConnection.getConnection();
        // Tải màn hình danh sách lớp
        if (connection != null) {
            loadAccountData();
        } else {
            Toast.makeText(this, "Không thể kết nối đến cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Lấy tên môn học khi mục được bấm
                String selectedSubject = accountList.get(position).split("\n")[0]; // Lấy tên môn học từ dòng đầu tiên

                // Chuyển sang StudentListActivity và gửi tên môn học dưới dạng dữ liệu thêm vào Intent
                Intent intent = new Intent(ClassListActivity.this, StudentListActivity.class);
                intent.putExtra("SubjectName", selectedSubject);
                startActivity(intent);
            }
        });

        Button addAccountButton = findViewById(R.id.addAccountButton);
        addAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang Activity thêm tài khoản
                Intent intent = new Intent(ClassListActivity.this, ClassAddActivity.class);
                startActivityForResult(intent, ADD_ACCOUNT_REQUEST);
            }
        });
    }

    private void loadAccountData() {
        accountList.clear(); // Xóa danh sách tài khoản hiện tại
        backgroundList.clear(); // Xóa danh sách background
        studentCountList.clear(); // Xóa danh sách số sinh viên

        String query = "SELECT [id], [name_class], [name_subject], [background] FROM [PROJECT].[dbo].[CLASS]";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String name_class = resultSet.getString("name_class");
                String name_subject = resultSet.getString("name_subject");
                int backgroundValue = resultSet.getInt("background");

                // Đếm số sinh viên cho lớp học hiện tại
                int studentCount = getStudentCountForClass(resultSet.getInt("id"));

                String accountInfo = name_subject + "\n" + name_class;
                accountList.add(accountInfo);
                backgroundList.add(backgroundValue); // Thêm giá trị background vào danh sách
                studentCountList.add(studentCount); // Thêm số sinh viên vào danh sách
            }
            resultSet.close();
            preparedStatement.close();
            adapter.notifyDataSetChanged(); // Cập nhật danh sách hiển thị trên ListView
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lấy dữ liệu từ cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_ACCOUNT_REQUEST && resultCode == RESULT_OK) {
            // Đã thêm tài khoản mới, cập nhật giao diện
            loadAccountData();
        }
    }
    private int getStudentCountForClass(int classId) {
        String query = "SELECT COUNT(*) AS studentCount FROM STUDENT_LIST WHERE class_id = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, classId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("studentCount");
            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0; // Trả về 0 nếu có lỗi hoặc không có sinh viên
    }

}
