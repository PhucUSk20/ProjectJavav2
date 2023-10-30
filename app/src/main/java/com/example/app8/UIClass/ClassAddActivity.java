package com.example.app8.UIClass;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.app8.R;
import com.example.app8.SQLServer.SQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClassAddActivity extends AppCompatActivity {

    private EditText name_subject;
    private EditText name_class;
    private EditText back_ground;
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_add_activity);

        name_subject = findViewById(R.id.usernameEditText);
        name_class = findViewById(R.id.editText2);
        back_ground = findViewById(R.id.kindEditText);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        connection = SQLConnection.getConnection();

        if (connection == null) {
            Toast.makeText(this, "Không thể kết nối đến cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
        }

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAccount();
            }
        });
    }

    private void addAccount() {
        String namesubject = name_subject.getText().toString();
        String nameclass = name_class.getText().toString();
        String background = back_ground.getText().toString();

        if (connection != null) {
            try {
                String query = "INSERT INTO [PROJECT].[dbo].[CLASS] ([name_subject], [name_class], [background]) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, namesubject);
                preparedStatement.setString(2, nameclass);
                preparedStatement.setString(3, background);
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    // Thêm tài khoản thành công
                    setResult(RESULT_OK);
                    finish(); // Đóng Activity thêm tài khoản và quay lại Activity trước (NextActivity)
                } else {
                    Toast.makeText(this, "Lỗi khi thêm tài khoản.", Toast.LENGTH_SHORT).show();
                }

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi thêm tài khoản.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Không thể kết nối đến cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
        }
    }
}
