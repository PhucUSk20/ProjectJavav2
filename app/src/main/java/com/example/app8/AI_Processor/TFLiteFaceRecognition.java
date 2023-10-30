package com.example.app8.AI_Processor;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import android.util.Pair;

import com.example.app8.SQLServer.SQLConnection;
import com.example.app8.UIStudent_Teacher.MainActivity;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class TFLiteFaceRecognition implements FaceClassifier {

    //private static final int OUTPUT_SIZE = 512;
    private static final int OUTPUT_SIZE = 192;

    // Only return this many results.
    private static final int NUM_DETECTIONS = 1;

    // Float model
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;

    private boolean isModelQuantized;
    // Config values.
    private int inputSize;

    private int[] intValues;

    private float[] faceEmbedding;

    private ByteBuffer imgData;

    private Interpreter faceNetModelInterpreter;
    private static final int FACENET_INPUT_IMAGE_SIZE = 112;
    private Connection connection = SQLConnection.getConnection();

    private byte[] convertFloatArrayToBytes(float[] floatArray) {
        ByteBuffer buffer = ByteBuffer.allocate(floatArray.length * 4);
        for (float value : floatArray) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }
    public void register(String name, String code, String dateofBirth, Recognition rec, int classId, Bitmap face) {
        //MainActivity.registered.put(name, rec);
        if (connection != null) {
            String insertSQL = "INSERT INTO STUDENT_LIST (name_student, code_student,date_of_birth, class_id, ImageData, FaceVector, Title, Distance, Id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, code);
                preparedStatement.setString(3, dateofBirth);
                preparedStatement.setInt(4, classId);
                byte[] ImageData = convertImageToBytes(face);
                preparedStatement.setBytes(5, ImageData);
                byte[] faceVectorBytes = convertFloatArrayToBytes(rec.getEmbeeding());
                preparedStatement.setBytes(6, faceVectorBytes);
                preparedStatement.setString(7, rec.getTitle());
                preparedStatement.setFloat(8, rec.getDistance());
                preparedStatement.setString(9, rec.getId());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    private TFLiteFaceRecognition() {}

    //TODO loads the models into mapped byte buffer format
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }



    public static FaceClassifier create(
            final AssetManager assetManager,
            final String modelFilename,
            final int inputSize,
            final boolean isQuantized)
            throws IOException {

        final TFLiteFaceRecognition d = new TFLiteFaceRecognition();
        d.inputSize = inputSize;

        try {
            d.faceNetModelInterpreter = new Interpreter(loadModelFile(assetManager, modelFilename));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        d.isModelQuantized = isQuantized;
        // Pre-allocate buffers.
        int numBytesPerChannel;
        if (isQuantized) {
            numBytesPerChannel = 1; // Quantized
        } else {
            numBytesPerChannel = 4; // Floating point
        }
        d.imgData = ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel);
        d.imgData.order(ByteOrder.nativeOrder());
        d.intValues = new int[d.inputSize * d.inputSize];
        return d;
    }

    //TODO  looks for the nearest embeeding in the dataset
    // and retrurns the pair <id, distance>
    private Pair<String, Float> findNearest(float[] emb) {
        Pair<String, Float> ret = null;
        for (Map.Entry<String, Recognition> entry : MainActivity.registered.entrySet()) {
            final String name = entry.getKey();
            final float[] knownEmb = ((float[]) entry.getValue().getEmbeeding());

            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff*diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(name, distance);
            }
        }
        return ret;
    }


    private float[] getFaceEmbeddings(Bitmap faceBitmap) {
            // Tạo TensorImage từ Bitmap
            TensorImage tensorImage = TensorImage.fromBitmap(faceBitmap);
            // Xử lý ảnh để chuẩn bị cho FaceNet
            ImageProcessor faceNetImageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(FACENET_INPUT_IMAGE_SIZE, FACENET_INPUT_IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new NormalizeOp(0f, 255f))
                    .build();
            tensorImage = faceNetImageProcessor.process(tensorImage);
            // Chuyển đổi TensorImage thành ByteBuffer
            ByteBuffer faceNetByteBuffer = tensorImage.getBuffer();
            // Tạo mảng để lưu trữ kết quả embeddings
            float[][] faceOutputArray = new float[1][192];
            // Sử dụng faceNetModelInterpreter để trích xuất embeddings
            faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray);
            return faceOutputArray[0];
    }

    //TODO TAKE INPUT IMAGE AND RETURN RECOGNITIONS
    @Override
    public Recognition recognizeImage(final Bitmap bitmap, boolean storeExtra) {
        float[] embeedings = getFaceEmbeddings(bitmap);

        float distance = Float.MAX_VALUE;
        String id = "0";
        String label = "?";
        //retrieve database here

        Log.d("registeredSize", String.valueOf(MainActivity.registered.size()));
        if (MainActivity.registered.size() > 0) {
            final Pair<String, Float> nearest = findNearest(embeedings);
            if (nearest != null) {
                final String name = nearest.first;
                label = name;
                distance = nearest.second;
            }
        }
        final int numDetectionsOutput = 1;
        Recognition rec = new Recognition(
                id,
                label,
                distance,
                new RectF());


        if (storeExtra) {
            rec.setEmbeeding(embeedings);
        }

        return rec;
    }
    private byte[] convertImageToBytes(Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
