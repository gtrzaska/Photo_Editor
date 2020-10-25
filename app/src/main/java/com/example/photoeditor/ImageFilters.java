package com.example.photoeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.util.Random;

public class ImageFilters {

    private final static int HIGHEST_COLOR_VALUE = 255;
    private final static int LOWEST_COLOR_VALUE = 0;

    public static Bitmap grey(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        int A, R, G, B;
        int pixel;

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                int intensity = (R + B + G) / 3;
                R = intensity;
                B = intensity;
                G = intensity;

                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return bmOut;
    }

    public static Bitmap sketch(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        int oldPixel ;

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                oldPixel  = src.getPixel(x, y);
                int oldRed = Color.red(oldPixel);
                int oldBlue = Color.blue(oldPixel);
                int oldGreen = Color.green(oldPixel);
                int oldAlpha = Color.alpha(oldPixel);

                // Algorithm for getting new values after calculation of filter
                // Algorithm for SKETCH FILTER
                int intensity = (oldRed + oldBlue + oldGreen) / 3;

                // applying new pixel value to newBitmap
                // condition for Sketch
                int newPixel = 0;
                int INTENSITY_FACTOR = 120;

                if (intensity > INTENSITY_FACTOR) {
                    // apply white color
                    newPixel = Color.argb(oldAlpha, HIGHEST_COLOR_VALUE, HIGHEST_COLOR_VALUE, HIGHEST_COLOR_VALUE);

                } else if (intensity > 100) {
                    // apply grey color
                    newPixel = Color.argb(oldAlpha, 150, 150, 150);
                } else {
                    // apply black color
                    newPixel = Color.argb(oldAlpha, LOWEST_COLOR_VALUE, LOWEST_COLOR_VALUE, LOWEST_COLOR_VALUE);
                }


                bmOut.setPixel(x, y, newPixel);
            }
        }
        return bmOut;
    }

    public static Bitmap sepia(Bitmap src) {
        int red, green, blue, pixel;
        int height = src.getHeight();
        int width = src.getWidth();
        int depth = 20;

        Bitmap sepia = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i++) {
            pixel = pixels[i];

            red = (pixel >> 16) & 0xFF;
            green = (pixel >> 8) & 0xFF;
            blue = pixel & 0xFF;

            red = green = blue = (red + green + blue) / 3;

            red += (depth * 2);
            green += depth;

            if (red > 255)
                red = 255;
            if (green > 255)
                green = 255;
            pixels[i] = (0xFF << 24) | (red << 16) | (green << 8) | blue;
        }
        sepia.setPixels(pixels, 0, width, 0, 0, width, height);
        return sepia;
    }

    public static Bitmap negative(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                int oldPixel  = src.getPixel(x, y);
                int oldRed = Color.red(oldPixel);
                int oldBlue = Color.blue(oldPixel);
                int oldGreen = Color.green(oldPixel);

                int newRed = HIGHEST_COLOR_VALUE - oldRed;
                int newBlue = HIGHEST_COLOR_VALUE - oldBlue;
                int newGreen = HIGHEST_COLOR_VALUE - oldGreen;
                int newPixel = Color.rgb(newRed, newGreen, newBlue);

                bmOut.setPixel(x, y, newPixel);
            }
        }
        return bmOut;
    }

    public static Bitmap colorRGB(Bitmap src, boolean red, boolean green, boolean blue) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                int oldPixel  = src.getPixel(x, y);
                // each pixel is made from RED_BLUE_GREEN_ALPHA
                // so, getting current values of pixel
                int oldRed = Color.red(oldPixel);
                int oldGreen = Color.green(oldPixel);
                int oldBlue = Color.blue(oldPixel);
                int oldAlpha = Color.alpha(oldPixel);

                // Algorithm for getting new values after calculation of filter
                // Algorithm for GREEN FILTER, by intensity of each pixel
                // set only green value others 0
                int newRed = red ? oldRed : 0;
                int newGreen = green ? oldGreen : 0;
                int newBlue = blue ? oldBlue : 0;

                // applying new pixel value to newBitmap
                int newPixel = Color.argb(oldAlpha, newRed, newGreen, newBlue);
                bmOut.setPixel(x, y, newPixel);
            }
        }
        return bmOut;
    }

    public static Bitmap changeBitmapContrastAndBrightness(Bitmap bmp, float contrast, float brightness)
    {
        contrast*= 0.1;
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

}
