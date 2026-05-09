package app;

import javafx.application.Platform;
import javafx.scene.Scene;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {

    private static ThemeManager instance;

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private boolean darkMode = false;

    private final List<Scene> registeredScenes = new ArrayList<>();

    private ThemeManager() {}


    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        applyToAll();
    }

    public void toggleTheme() {
        darkMode = !darkMode;
        applyToAll();
    }

    public void registerScene(Scene scene) {
        if (scene != null && !registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyTo(scene);
        }
    }

    public void unregisterScene(Scene scene) {
        registeredScenes.remove(scene);
    }

    private void applyToAll() {
        if (Platform.isFxApplicationThread()) {
            new ArrayList<>(registeredScenes).forEach(this::applyTo);
        } else {
            Platform.runLater(() ->
                    new ArrayList<>(registeredScenes).forEach(this::applyTo)
            );
        }
    }

    public void applyTo(Scene scene) {
        if (scene == null) return;

        String lightCss = getStylesheet("style.css");
        String darkCss  = getStylesheet("style-dark.css");

        scene.getStylesheets().removeIf(s ->
                s.equals(lightCss) || s.equals(darkCss)
        );

        if (darkMode) {
            scene.getStylesheets().add(darkCss);
        } else {
            scene.getStylesheets().add(lightCss);
        }
    }


    private String getStylesheet(String filename) {
        var url = ThemeManager.class.getResource("/" + filename);
        return url != null ? url.toExternalForm() : "";
    }

    public String getToggleLabel() {
        return darkMode ? "☀  Light Mode" : "🌙  Dark Mode";
    }
}
