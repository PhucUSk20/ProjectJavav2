package com.example.app8.UIStudent_Admin;

public class StudentInfo {
    private String name;
    private String dateOfBirth;
    private String code;
    private byte[] imageData;

    public StudentInfo(String name, String code, String dateOfBirth, byte[] imageData) {
        this.name = name;
        this.code = code;
        this.dateOfBirth = dateOfBirth;
        this.imageData = imageData;
    }

    public String getName() {
        return name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getCode(){
        return code;
    }

    public byte[] getImageData() {
        return imageData;
    }
}
