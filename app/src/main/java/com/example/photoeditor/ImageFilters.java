package com.example.photoeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

public class ImageFilters {

    private final static int HIGHEST_COLOR_VALUE = 255;
    private final static int LOWEST_COLOR_VALUE = 0;

    public static Bitmap sketch(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        int oldPixel;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                oldPixel = src.getPixel(x, y);
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

        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        -1, 0, 0, 0, 255,
                        0, -1, 0, 0, 255,
                        0, 0, -1, 0, 255,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);

        return ret;
    }

    public static Bitmap colorRGB(Bitmap src, boolean red, boolean green, boolean blue) {

        int newRed = red ? 1 : 0;
        int newGreen = green ? 1 : 0;
        int newBlue = blue ? 1 : 0;

        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        newRed, newRed, newRed, 0, 0,
                        newGreen, newGreen, newGreen, 0, 0,
                        newBlue, newBlue, newBlue, 0, 0,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);

        return ret;

    }

    public static Bitmap changeBitmapContrastAndBrightness(Bitmap bmp, float contrast, float brightness) {
        contrast *= 0.1;
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

    public static Bitmap vignette(Bitmap src) {
        Bitmap image = src;
        final int width = image.getWidth();
        final int height = image.getHeight();

        float radius = (float) (width / 1.2);
        int[] colors = new int[]{0, 0x55000000, 0xff000000};
        float[] positions = new float[]{0.0f, 0.5f, 1.0f};

        RadialGradient gradient = new RadialGradient(width / 2, height / 2, radius, colors, positions, Shader.TileMode.CLAMP);

        Canvas canvas = new Canvas(image);
        canvas.drawARGB(1, 0, 0, 0);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setShader(gradient);

        final Rect rect = new Rect(0, 0, image.getWidth(), image.getHeight());
        final RectF rectf = new RectF(rect);

        canvas.drawRect(rectf, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(image, rect, rect, paint);

        return image;
    }

    public static Bitmap hue(Bitmap bitmap, float hue) {
        Bitmap newBitmap = bitmap.copy(bitmap.getConfig(), true);
        final int width = newBitmap.getWidth();
        final int height = newBitmap.getHeight();
        float[] hsv = new float[3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = newBitmap.getPixel(x, y);
                Color.colorToHSV(pixel, hsv);
                hsv[0] = hue;
                newBitmap.setPixel(x, y, Color.HSVToColor(Color.alpha(pixel), hsv));
            }
        }
        return newBitmap;
    }

    public static Bitmap saturation(Bitmap bitmap, float sat) {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(sat);
        Bitmap ret = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return ret;
    }

    public static Bitmap swappingColor(Bitmap src) {

        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        0, 0, 1, 0, 0,
                        1, 0, 0, 0, 0,
                        0, 1, 0, 0, 0,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);

        return ret;
    }

    public static Bitmap binary(Bitmap src) {

        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        255, 0, 0, 0, -128 * 255,
                        0, 255, 0, 0, -128 * 255,
                        0, 0, 255, 0, -128 * 255,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);

        return ret;
    }

    public static Bitmap binaryBlackWhite(Bitmap src) {

        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        85, 85, 85, 0, -128 * 255,
                        85, 85, 85, 0, -128 * 255,
                        85, 85, 85, 0, -128 * 255,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);

        return ret;
    }

    public static Bitmap yuv(Bitmap src) {

        ColorMatrix cm = new ColorMatrix();
        cm.setRGB2YUV();
        Bitmap ret = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);

        return ret;
    }

}
