# To install the PROJECT
You need create DATABASE
-----------------------------------------------------------------------
    DROP DATABASE PROJECT;
    CREATE DATABASE PROJECT;
    -- Create the ACCOUNT table.
    CREATE TABLE ACCOUNT (
        ID INT IDENTITY(1,1) PRIMARY KEY,
        USERNAME VARCHAR(255) NOT NULL,
        PASSWORD VARCHAR(255) NOT NULL,
        KIND VARCHAR(10) NOT NULL
    );
    -- Create the CLASS table.
    CREATE TABLE CLASS (
        id INT PRIMARY KEY IDENTITY(1,1),
        name_class NVARCHAR(255) NOT NULL,
        name_subject NVARCHAR(255) NOT NULL,
        background NVARCHAR(255) NOT NULL
    );
    -- Create the Students_List table with a foreign key reference.
    CREATE TABLE STUDENT_LIST (
        name_student NVARCHAR(255) NOT NULL,
        code_student NVARCHAR(255) PRIMARY KEY,
        date_of_birth  DATE NOT NULL,
      	ImageData VARBINARY(MAX) NOT NULL,
        class_id INT NOT NULL,
        FOREIGN KEY (class_id) REFERENCES CLASS(id)
    );
    CREATE TABLE ATTEND_STUDENT_LIST(
        unique_id INT PRIMARY KEY IDENTITY(1,1),
        date_attendace DATE NOT NULL,
        attendance NVARCHAR(255) NOT NULL,
        classID INT NOT NULL,
        Code_student NVARCHAR(255) NOT NULL,
        FOREIGN KEY (classID) REFERENCES CLASS(id),
        FOREIGN KEY (Code_student) REFERENCES STUDENT_LIST(code_student)
    );

-- Insert data into the CLASS table.
INSERT INTO CLASS (name_class, name_subject, background) VALUES ('K20-Fetel', 'Lap trinh java', '1');
INSERT INTO CLASS (name_class, name_subject, background) VALUES ('K20-Fetel', 'He thong nhung', '2');
INSERT INTO CLASS (name_class, name_subject, background) VALUES ('K20-Fetel', 'Thiet ke Logic kha trinh', '3');


-- Insert data into the ACCOUNT table.
INSERT INTO ACCOUNT (USERNAME, PASSWORD, KIND) VALUES ('phuc', '123', 'admin');
INSERT INTO ACCOUNT (USERNAME, PASSWORD, KIND) VALUES ('khanh','456', 'admin');
INSERT INTO ACCOUNT (USERNAME, PASSWORD, KIND) VALUES ('nghia','789', 'teacher');

-- Insert data into the ATTEND_STUDENT_LIST.
INSERT INTO ATTEND_STUDENT_LIST (date_attendace,attendance,classID,Code_student) VALUES ('2-10-2023','absent','1','20200314');
