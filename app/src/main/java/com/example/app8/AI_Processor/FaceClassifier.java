package com.example.app8.AI_Processor;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.HashMap;

/** Generic interface for interacting with different recognition engines. */
public interface FaceClassifier {

    void register(String name, String code, String dateofBirth, Recognition recognition, int classId, Bitmap face);

    Recognition recognizeImage(Bitmap bitmap, boolean getExtra);


    public class Recognition {
        private final String id;

        /** Display name for the recognition. */
        private final String title;
        // A sortable score for how good the recognition is relative to others. Lower should be better.
        private final Float distance;

        private float[] faceEmbedding;
        /** Optional location within the source image for the location of the recognized face. */
        private RectF location;
        private Bitmap crop;

        public Recognition(
                final String id, final String title, final Float distance, final RectF location) {
            this.id = id;
            this.title = title;
            this.distance = distance;
            this.location = location;
            this.faceEmbedding = null;
            this.crop = null;
        }

        public Recognition(
                final String id, final String title, final Float distance, final RectF location, final float[] faceEmbedding) {
            this.id = id;
            this.title = title;
            this.distance = distance;
            this.location = location;
            this.faceEmbedding = faceEmbedding;
            this.crop = null;
        }

        public void setEmbeeding(float[] faceEmbedding) {
            this.faceEmbedding = faceEmbedding;
        }
        public float[] getEmbeeding() {
            return faceEmbedding;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getDistance() {
            return distance;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (distance != null) {
                resultString += String.format("(%.1f%%) ", distance * 100.0f);
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }

        public void setCrop(Bitmap crop) {
            this.crop = crop;
        }

        public Bitmap getCrop() {
            return this.crop;
        }
    }
}
