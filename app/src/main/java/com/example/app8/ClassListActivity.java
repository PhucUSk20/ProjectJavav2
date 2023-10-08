    package com.example.app8;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import android.widget.ImageButton;
    import android.widget.LinearLayout;
    import android.content.Intent;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.AdapterView;
    import android.widget.Button;
    import android.widget.ListView;
    import android.widget.RelativeLayout;
    import android.widget.Toast;
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.util.ArrayList;

    public class ClassListActivity extends AppCompatActivity {
        private LinearLayout navDrawerLayout;
        private boolean isNavDrawerOpen = false; // Biến kiểm tra nav_drawer_layout có đang mở hay không

        private ListView listView;
        private ArrayList<String> accountList;
        private ArrayList<Integer> backgroundList;
        private ArrayList<Integer> studentCountList;

        private CustomClassListAdapter adapter;
        private Connection connection;
        private static final int ADD_ACCOUNT_REQUEST = 1;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.class_list_activity);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            navDrawerLayout = findViewById(R.id.nav_drawer_layout);


            ImageButton navIcon = findViewById(R.id.nav_icon);
            navIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (navDrawerLayout.getVisibility() == View.VISIBLE) {
                        navDrawerLayout.setVisibility(View.GONE);
                    } else {
                        navDrawerLayout.setVisibility(View.VISIBLE);
                    }
                }
            });

            listView = findViewById(R.id.listView);
            accountList = new ArrayList<>();
            backgroundList = new ArrayList<>();
            studentCountList = new ArrayList<>();

            adapter = new CustomClassListAdapter(this, accountList, backgroundList, studentCountList);
            listView.setAdapter(adapter);

            connection = SQLConnection.getConnection();

            if (connection != null) {
                loadAccountData();
            } else {
                Toast.makeText(this, "Không thể kết nối đến cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (navDrawerLayout.getVisibility() == View.VISIBLE) {
                        navDrawerLayout.setVisibility(View.GONE);
                    }
                    // Trong phương thức onItemClick
                    String selectedSubject = accountList.get(position).split("\n")[0];
                    Intent intent = new Intent(ClassListActivity.this, StudentListActivity.class);
                    intent.putExtra("SubjectName", selectedSubject);
                    startActivityForResult(intent, ADD_ACCOUNT_REQUEST);
                }
            });

            Button addAccountButton = findViewById(R.id.addAccountButton);
            addAccountButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ClassListActivity.this, ClassAddActivity.class);
                    startActivityForResult(intent, ADD_ACCOUNT_REQUEST);
                }
            });
        }

        private void loadAccountData() {
            accountList.clear();
            backgroundList.clear();
            studentCountList.clear();

            String query = "SELECT [id], [name_class], [name_subject], [background] FROM [PROJECT].[dbo].[CLASS]";

            try {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String name_class = resultSet.getString("name_class");
                    String name_subject = resultSet.getString("name_subject");
                    int backgroundValue = resultSet.getInt("background");
                    int studentCount = getStudentCountForClass(resultSet.getInt("id"));

                    String accountInfo = name_subject + "\n" + name_class;
                    accountList.add(accountInfo);
                    backgroundList.add(backgroundValue);
                    studentCountList.add(studentCount);
                }
                resultSet.close();
                preparedStatement.close();
                adapter.notifyDataSetChanged();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi lấy dữ liệu từ cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == ADD_ACCOUNT_REQUEST && resultCode == RESULT_OK) {
                // Khi quay lại từ StudentListActivity sau khi thêm sinh viên
                // Cập nhật lại danh sách lớp và số sinh viên mỗi lớp
                loadAccountData();
            }
        }

        @Override
        public void onBackPressed() {
            // Kiểm tra xem nav_drawer_layout có đang mở hay không
            if (isNavDrawerOpen) {
                // Nếu đang mở, đóng nav_drawer_layout
                if (navDrawerLayout.getVisibility() == View.VISIBLE) {
                    navDrawerLayout.setVisibility(View.GONE);
                    isNavDrawerOpen = false;
                }
            } else {
                super.onBackPressed();
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

            return 0;
        }
    }