package com.github.jextractall.ui.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class Messages {

    private static ResourceBundle resources =
                ResourceBundle.getBundle("com.github.jextractall.ui.i18n.messages",
                Locale.getDefault(),
                Messages.class.getClassLoader());

    public static ResourceBundle getResourceBundle() {
        return resources;
    }

    public static String getMessage(String key, Object... params) {
        String resKey = resources.getString(key);
        if (resKey != null) {
            return MessageFormat.format(resKey, params);
        }
        return null;
    }

}
