package me.vektory79;

import com.jme3.system.AppSettings;

import java.io.IOException;

/**
 * The starter class.
 */
public class Starter {

    public static void main(final String[] args) {
        final AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        settings.setFullscreen(false);

        final GameApplication application = new GameApplication(false);
        application.setSettings(settings);
        application.setShowSettings(false);
        application.start();
    }
}
