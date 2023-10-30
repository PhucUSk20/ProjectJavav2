package com.example.app8.UIStudent_Teacher;

import android.content.Intent;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app8.AI_Processor.FaceClassifier;
import com.example.app8.R;
import com.example.app8.SQLServer.SQLConnection;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    public static HashMap<String, FaceClassifier.Recognition> registered = new HashMap<>();


    Button registerBtn,recognizeBtn;
    private Connection connection = SQLConnection.getConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recognizeBtn = findViewById(R.id.buttonrecognize);
        int classId = Integer.parseInt(getIntent().getStringExtra("ClassId"));
        recognizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,RecognitionActivity.class));
                registered = getRegisteredFacesFromDatabase(classId);
            }
        });
    }
    private float[] convertBytesToFloatArray(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        float[] floatArray = new float[bytes.length / 4];
        for (int i = 0; i < floatArray.length; i++) {
            floatArray[i] = buffer.getFloat();
        }
        return floatArray;
    }
    public HashMap<String, FaceClassifier.Recognition> getRegisteredFacesFromDatabase(int classId) {
        // Create an AsyncTask to perform the database query asynchronously
        new AsyncTask<Void, Void, HashMap<String, FaceClassifier.Recognition>>() {
            @Override
            protected HashMap<String, FaceClassifier.Recognition> doInBackground(Void... voids) {
                HashMap<String, FaceClassifier.Recognition> registeredFaces = new HashMap<>();

                if (connection != null) {
                    String query = "SELECT name_student, Id, Title, Distance, FaceVector FROM STUDENT_LIST WHERE class_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setInt(1, classId);

                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            while (resultSet.next()) {
                                String sdName = resultSet.getString("name_student");
                                String id = resultSet.getString("Id");
                                String title = resultSet.getString("Title");
                                float distance = resultSet.getFloat("Distance");
                                byte[] faceVectorBytes = resultSet.getBytes("FaceVector");
                                float[] faceEmbeddings = convertBytesToFloatArray(faceVectorBytes);

                                FaceClassifier.Recognition rec = new FaceClassifier.Recognition(id, title, distance, new RectF(), faceEmbeddings);

                                registeredFaces.put(sdName, rec);
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                return registeredFaces;
            }

            @Override
            protected void onPostExecute(HashMap<String, FaceClassifier.Recognition> result) {
                // Handle the result after the database query is complete
                // Set the registered faces and count down the latch
                registered = result;
                Log.d("registeredPostMain", String.valueOf(registered.size()));
                //registeredLatch.countDown();
            }
        }.execute();

        // Return an empty HashMap immediately; the actual result will be set in onPostExecute
        return new HashMap<>();
    }

}