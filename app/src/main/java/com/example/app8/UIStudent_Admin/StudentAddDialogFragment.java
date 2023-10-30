package com.example.app8.UIStudent_Admin;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.app8.R;
import com.example.app8.SQLServer.SQLConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentAddDialogFragment extends DialogFragment {
    private static final int PICK_IMAGE = 1;
    private EditText editTextName;
    private EditText editTextCode;
    private EditText editTextDateOfBirth;
    private ImageView imageView;
    private Connection connection;
    private byte[] imageData; // Dữ liệu ảnh dưới dạng mã nhị phân
    private StudentListActivity activityReference;

    public void setActivityReference(StudentListActivity activity) {
        this.activityReference = activity;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.student_add_fragmentdialog, container, false);

        editTextName = view.findViewById(R.id.editTextName);
        editTextCode = view.findViewById(R.id.editTextCode);
        editTextDateOfBirth = view.findViewById(R.id.editTextDateOfBirth);
        imageView = view.findViewById(R.id.imageView);
        connection = SQLConnection.getConnection();
        Bundle args = getArguments();
        int classId = args.getInt("ClassId");

        // Xử lý sự kiện khi bấm vào ImageView để chọn ảnh từ thư viện
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy thông tin sinh viên từ các EditText và hình ảnh từ ImageView
                String studentName = editTextName.getText().toString();
                String studentCode = editTextCode.getText().toString();
                String dateOfBirth = editTextDateOfBirth.getText().toString();

                // Thực hiện lưu thông tin sinh viên vào SQL Server
                boolean isSaved = addStudentToDatabase(studentName, studentCode, dateOfBirth,classId,imageData);

                if (isSaved) {
                    // Đóng DialogFragment sau khi lưu thành công
                    dismiss();
                    if (activityReference != null) {
                        activityReference.loadAndUpdateStudentList(classId);

                        // Đặt kết quả thành RESULT_OK và kết thúc hoạt động
                        Intent resultIntent = new Intent();
                        getActivity().setResult(Activity.RESULT_OK, resultIntent);
                    }
                } else {
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Đặt ảnh lên ImageView
                imageView.setImageURI(selectedImageUri);

                // Chuyển đổi ảnh thành mảng byte
                 imageData = convertImageToBytes(selectedImageUri);
                // Lưu imageData để sử dụng sau này trong phương thức onClick của nút lưu
            }
        }
    }


    // Hàm này thêm sinh viên vào cơ sở dữ liệu
    private boolean addStudentToDatabase(String studentName, String studentCode, String dateOfBirth, int classId, byte[] imageData) {
        if (connection != null) {
            try {
                // Thực hiện truy vấn SQL để thêm sinh viên vào cơ sở dữ liệu
                String insertStudentQuery = "INSERT INTO STUDENT_LIST (name_student, code_student, date_of_birth, class_id, ImageData) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(insertStudentQuery);
                preparedStatement.setString(1, studentName);
                preparedStatement.setString(2, studentCode);
                preparedStatement.setString(3, dateOfBirth);
                preparedStatement.setInt(4, classId);
                preparedStatement.setBytes(5, imageData);

                int rowsInserted = preparedStatement.executeUpdate();
                preparedStatement.close();

                return rowsInserted > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    private byte[] convertImageToBytes(Uri imageUri) {
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}