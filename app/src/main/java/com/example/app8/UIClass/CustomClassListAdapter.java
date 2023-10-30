package com.example.app8.UIClass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.app8.R;

import java.util.ArrayList;

public class CustomClassListAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> classList;
    private ArrayList<Integer> backgroundList;
    private ArrayList<Integer> studentCountList; // Thêm danh sách số sinh viên
    private static final int FIXED_BACKGROUND_SIZE = 200;

    public CustomClassListAdapter(Context context, ArrayList<String> classList, ArrayList<Integer> backgroundList, ArrayList<Integer> studentCountList) {
        super(context, R.layout.class_list_custom, classList);
        this.context = context;
        this.classList = classList;
        this.backgroundList = backgroundList;
        this.studentCountList = studentCountList; // Khởi tạo danh sách số sinh viên
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.class_list_custom, parent, false);
        }

        // Lấy tên môn học và tên lớp học từ danh sách
        String classInfo = classList.get(position);
        String[] parts = classInfo.split("\n");
        String subjectName = parts[0];
        String className = parts[1];

        // Lấy giá trị background tương ứng
        int backgroundValue = backgroundList.get(position);

        // Chuyển giá trị background thành tên tài nguyên drawable
        String drawableName = "background_" + backgroundValue;

        // Lấy ID tài nguyên drawable từ tên
        int backgroundResId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());

        // Gán dữ liệu vào TextViews trong layout custom_list_class.xml
        TextView subjectNameTextView = convertView.findViewById(R.id.subjectNameTextView);
        TextView classNameTextView = convertView.findViewById(R.id.classNameTextView);
        TextView studentCountTextView = convertView.findViewById(R.id.studentCountTextView);

        subjectNameTextView.setText(subjectName);
        classNameTextView.setText(className);

        // Lấy số sinh viên cho lớp học hiện tại
        int studentCount = studentCountList.get(position);
        studentCountTextView.setText("Số sinh viên: " + studentCount);

        // Thiết lập nền cho mục
        convertView.setBackgroundResource(backgroundResId);

        return convertView;
    }
}