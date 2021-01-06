package com.example.photoeditor;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import java.util.Locale;

public class LanguageHelper {

    static void changeLocale(Resources res, String locale) {
        Configuration config;
        config = new Configuration(res.getConfiguration());

        switch (locale) {
            case "pl":
                config.locale = new Locale("pl");
                break;
            case "en":
                config.locale = new Locale("en");
                break;
            default:
                config.locale = new Locale("pl");
                break;
        }
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}
