package com.example.app8.UIStudent_Admin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app8.R;

import java.util.ArrayList;

public class CustomStudentListAdapter extends ArrayAdapter<String> {
    private Context context;
    private ArrayList<String> studentList;
    private ArrayList<byte[]> imageDataList; // Danh sách dữ liệu hình ảnh

    public CustomStudentListAdapter(Context context, ArrayList<String> studentList, ArrayList<byte[]> imageDataList) {
        super(context, R.layout.student_list_custom, studentList);
        this.context = context;
        this.studentList = studentList;
        this.imageDataList = imageDataList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.student_list_custom, parent, false);
        }

        // Lấy tên sinh viên từ danh sách
        String studentName = studentList.get(position);

        // Lấy dữ liệu hình ảnh từ danh sách
        byte[] imageData = imageDataList.get(position);

        // Gán dữ liệu vào TextView và ImageView trong layout custom_student_item.xml
        TextView studentNameTextView = convertView.findViewById(R.id.studentNameTextView);
        ImageView studentImageView = convertView.findViewById(R.id.studentImageView);

        studentNameTextView.setText(studentName);

        // Chuyển dữ liệu byte[] thành hình ảnh và gán cho ImageView
        if (imageData != null && imageData.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            studentImageView.setImageBitmap(bitmap);
        } else {
            // Nếu không có hình ảnh, bạn có thể đặt một hình ảnh mặc định hoặc ẩn ImageView tùy ý.
            // studentImageView.setImageResource(R.drawable.default_image);
            // Hoặc ẩn ImageView đi
            // studentImageView.setVisibility(View.GONE);
        }

        return convertView;
    }
}
