package com.example.app8.Login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app8.UIClass.ClassListActivity;
import com.example.app8.R;
import com.example.app8.SQLServer.SQLConnection;
import com.example.app8.UIStudent_Teacher.TeacherActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginActivity extends AppCompatActivity {

    private TextView textView;
    private EditText editText1;
    private EditText editText2;
    private Button button2;
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
        textView = findViewById(R.id.textView);
        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        button2 = findViewById(R.id.button2);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        connection = SQLConnection.getConnection();

        if (connection != null) {
            textView.setText("SUCCESS CONNECT WITH SQL SERVER");
        } else {
            textView.setText("ERROR");
        }
    }

    public void sqlButton(View view) {
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT USERNAME FROM ACCOUNT;");
                while (resultSet.next()) {
                    textView.setText(resultSet.getString(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            textView.setText("Connection is null");
        }
    }

    public void Login(View view) {
        String username = editText1.getText().toString();
        String password = editText2.getText().toString();

        // Kiểm tra kết nối cơ sở dữ liệu
        if (connection == null) {
            Toast.makeText(LoginActivity.this, "Không thể kết nối đến cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
            return;
        }

        Pair<Boolean, String> authenticationResult = authenticateUser(username, password);
        boolean authenticated = authenticationResult.first;
        String kind = authenticationResult.second;

        if (authenticated) {
            // Nếu đăng nhập thành công, chuyển sang Activity tiếp theo
            Intent intent = new Intent();
            if ("admin".equals(kind)) {
                // User is an admin, go to ClassListActivity
                intent = new Intent(LoginActivity.this, ClassListActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
            } else if ("teacher".equals(kind)) {
                // User is a teacher, go to TeacherActivity
                intent = new Intent(LoginActivity.this, TeacherActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
            } else {
                // Handle other roles or scenarios as needed
                // You can display an error message or navigate to a default activity.
                Toast.makeText(LoginActivity.this, "Error: No 'KIND' detected", Toast.LENGTH_SHORT).show();
            }

        } else {
            // Hiển thị thông báo lỗi nếu đăng nhập không thành công
            Toast.makeText(LoginActivity.this, "Đăng nhập không thành công. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }

    public Pair<Boolean, String> authenticateUser(String username, String password) {
        boolean authenticated = false;

        // Thực hiện truy vấn kiểm tra tài khoản trong cơ sở dữ liệu
        // Thay thế dòng sau bằng cách sử dụng PreparedStatement để tránh lỗi SQL Injection
        String query = "SELECT COUNT(*) FROM ACCOUNT WHERE USERNAME = ? AND PASSWORD = ?";
        String queryKind = "SELECT KIND\n" +
                "FROM dbo.ACCOUNT\n" +
                "WHERE USERNAME = ? AND PASSWORD = ?";
        String KIND = "";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next(); // Di chuyển con trỏ đến dòng đầu tiên

            int count = resultSet.getInt(1);

            if (count == 1) {
                authenticated = true;
            }

            preparedStatement = connection.prepareStatement(queryKind);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSetKind = preparedStatement.executeQuery();
            if (resultSetKind.next()) {
                String kind = resultSetKind.getString("KIND"); // Use the column name
                KIND = kind;
                // Now, 'kind' contains the value from the 'KIND' column in the result
                // Use 'kind' as needed in your code
            } else {
                // Handle the case where no matching user is found
                // You may want to display an error message or take appropriate action
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            // Ghi log lỗi
            Log.e("MyApp", "Lỗi xảy ra: " + e.getMessage());
        }

        return new Pair<>(authenticated, KIND);
    }

}
