package application;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class Main extends Application {

	private Image originalImage;
    private ImageView imageView;
    private Image editedImage;
    private Stack<ImageFilter> filterStack = new Stack<>();
    private Stack<Image> imageStack = new Stack<>();

   

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Editor");

        // UI components
        BorderPane root = new BorderPane();
        imageView = new ImageView();
        VBox filterOptions = createFilterOptions();

        root.setTop(createMenuBar(primaryStage));
        root.setLeft(filterOptions);
        root.setCenter(imageView);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    private VBox createFilterOptions() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        Button undoButton = new Button("Undo");
        undoButton.setOnAction(event -> undoFilter());

        Button grayscaleButton = new Button("Grayscale");
        grayscaleButton.setOnAction(event -> applyFilter(new GrayscaleFilter()));

        Button blueButton = new Button("Blue Filter");
        blueButton.setOnAction(event -> applyFilter(new BlueFilter()));

        Button invertButton = new Button("Inversion");
        invertButton.setOnAction(event -> applyFilter(new InversionFilter()));

        Button rotateButton = new Button("Rotate");
        rotateButton.setOnAction(event -> applyFilter(new RotationFilter()));

        Slider brightnessSlider = new Slider(-100, 100, 0);
        Button applyBrightnessButton = new Button("Apply Brightness");
        applyBrightnessButton.setOnAction(event -> applyFilter(new BrightnessFilter(brightnessSlider.getValue())));

        Slider contrastSlider = new Slider(-100, 100, 0);
        Button applyContrastButton = new Button("Apply Contrast");
        applyContrastButton.setOnAction(event -> applyFilter(new ContrastFilter(contrastSlider.getValue())));

        vbox.getChildren().addAll(
            grayscaleButton, blueButton, invertButton, rotateButton,
            new Label("Brightness:"),
            brightnessSlider,
            applyBrightnessButton,
            new Label("Contrast:"),
            contrastSlider,
            applyContrastButton,
            undoButton
        );


        return vbox;
    }

    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open Image");
        MenuItem saveItem = new MenuItem("Save Image");
        openItem.setOnAction(event -> openImage(primaryStage));
        saveItem.setOnAction(event -> saveImage());
        fileMenu.getItems().addAll(openItem, saveItem);
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }
    private void applyFilter(ImageFilter filter) {
        Image editedImage = filter.apply(imageView.getImage());
        filterStack.push(filter);
        imageStack.push(editedImage);
        imageView.setImage(editedImage);
    }

    private void undoFilter() {
        if (!imageStack.isEmpty()) {
            imageStack.pop();
            if (!imageStack.isEmpty()) {
                editedImage = imageStack.peek();
                imageView.setImage(editedImage);
            } else {
                imageView.setImage(originalImage);
            }
        }
    }

    private void openImage(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            originalImage = new Image(selectedFile.toURI().toString());
            imageView.setImage(originalImage);
        }
    }

    private void saveImage() {
        if (editedImage != null) {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
            fileChooser.getExtensionFilters().add(imageFilter);

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(editedImage, null), "png", file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public interface ImageFilter {
        Image apply(Image image);
    }

    public class GrayscaleFilter implements ImageFilter {
        @Override
        public Image apply(Image image) {
        	 int width = (int) image.getWidth();
             int height = (int) image.getHeight();

             BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

             for (int y = 0; y < height; y++) {
                 for (int x = 0; x < width; x++) {
                     Color color = new Color(image.getPixelReader().getArgb(x, y));
                     int gray = (int) (0.2989 * color.getRed() + 0.5870 * color.getGreen() + 0.1140 * color.getBlue());
                     Color grayColor = new Color(gray, gray, gray);
                     outImg.setRGB(x, y, grayColor.getRGB());
                 }
             }

             editedImage = SwingFXUtils.toFXImage(outImg, null);
             

             return editedImage;
        }
    }

    public class BlueFilter implements ImageFilter {
        @Override
        public Image apply(Image image) {
            // Apply blue filter to the image
            // Implement your blue filter logic here
            return image; // Return the processed image
        }
    }

    public class InversionFilter implements ImageFilter {
        @Override
        public Image apply(Image image) {
        	 int width = (int) image.getWidth();
             int height = (int) image.getHeight();

             BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

             for (int y = 0; y < height; y++) {
                 for (int x = 0; x < width; x++) {
                     Color originalColor = new Color(image.getPixelReader().getArgb(x, y));
                     int invertedColor = 0xFF000000 | (255 - originalColor.getRed()) << 16 | (255 - originalColor.getGreen()) << 8 | (255 - originalColor.getBlue());
                     outImg.setRGB(x, y, invertedColor);
                 }
             }

             editedImage = SwingFXUtils.toFXImage(outImg, null);

             return editedImage;
        }
    }

    public class RotationFilter implements ImageFilter {
        @Override
        public Image apply(Image image) {
            // Apply rotation filter to the image
            // Implement your rotation logic here
            return image; // Return the processed image
        }
    }

    public class BrightnessFilter implements ImageFilter {
        private double brightnessFactor;

        public BrightnessFilter(double brightnessFactor) {
            this.brightnessFactor = brightnessFactor;
        }

        @Override
        public Image apply(Image image) {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            int adjustment = (int) brightnessFactor;
            if (adjustment < -100) {
                adjustment = -100;
            } else if (adjustment > 100) {
                adjustment = 100;
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color originalColor = new Color(image.getPixelReader().getArgb(x, y));

                    int newRed = clamp(originalColor.getRed() + (255 - originalColor.getRed()) * adjustment / 100);
                    int newGreen = clamp(originalColor.getGreen() + (255 - originalColor.getGreen()) * adjustment / 100);
                    int newBlue = clamp(originalColor.getBlue() + (255 - originalColor.getBlue()) * adjustment / 100);

                    Color newColor = new Color(newRed, newGreen, newBlue);
                    outImg.setRGB(x, y, newColor.getRGB());
                }
            }

            editedImage = SwingFXUtils.toFXImage(outImg, null);

            return editedImage;
        }

        private int clamp(int value) {
            return Math.min(255, Math.max(0, value));
        }
    }

  public class ContrastFilter implements ImageFilter {
    private double contrastLevel;

    public ContrastFilter(double contrastLevel) {
        this.contrastLevel = contrastLevel;
    }

    @Override
    public Image apply(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        double factor = (259.0 * (contrastLevel + 255.0)) / (255.0 * (259.0 - contrastLevel));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color originalColor = new Color(image.getPixelReader().getArgb(x, y));

                int newRed = truncate(factor * (originalColor.getRed() - 128) + 128);
                int newGreen = truncate(factor * (originalColor.getGreen() - 128) + 128);
                int newBlue = truncate(factor * (originalColor.getBlue() - 128) + 128);

                Color newColor = new Color(newRed, newGreen, newBlue);
                outImg.setRGB(x, y, newColor.getRGB());
            }
        }

        editedImage = SwingFXUtils.toFXImage(outImg, null);

        return editedImage;
    }

    private int truncate(double value) {
        return Math.min(255, Math.max(0, (int) value));
    }
}


    public static void main(String[] args) {
        launch(args);
    }
}







