package application;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
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

	public Image originalImage;
    private ImageView imageView;
    private Image editedImage;
    private Stack<ImageFilter> filterStack = new Stack<>();
    private Stack<Image> imageStack = new Stack<>();

   

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Editor");

      
        BorderPane root = new BorderPane();
        
        
        
        VBox filterOptions = createFilterOptions();
        

        root.setTop(createMenuBar(primaryStage));
        root.setLeft(filterOptions);
        root.setCenter(imageView); 

        
        Scene scene = new Scene(root, 800, 800);
        
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        
        Image icon = new Image("application/picture.png");

        primaryStage.setScene(scene);
        primaryStage.getIcons().add(icon);
        primaryStage.show();
    }

    private VBox createFilterOptions() {
        VBox vbox = new VBox();
        vbox.getStyleClass().add("tool-vbox");
        vbox.setPadding(new Insets(10));
        vbox.setFillWidth(true);
        vbox.setSpacing(30);
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800); 

        Button undoButton = new Button("Undo");
        undoButton.setMaxWidth(Double.MAX_VALUE);
        undoButton.setOnAction(event -> undoFilter());

        Button grayscaleButton = new Button("Grayscale");
        grayscaleButton.setMaxWidth(Double.MAX_VALUE);
        grayscaleButton.setOnAction(event -> applyFilter(new GrayscaleFilter()));

        Button blueButton = new Button("Blue Filter");
        blueButton.setMaxWidth(Double.MAX_VALUE);
        blueButton.setOnAction(event -> applyFilter(new BlueFilter()));
        
        Button flipHorizontally = new Button("Flip Horizontally");
        flipHorizontally.setMaxWidth(Double.MAX_VALUE);
        flipHorizontally.setOnAction(event -> applyFilter(new InvertImage("horizontally")));

        Button flipVertically = new Button("Flip Vertically");
        flipVertically.setMaxWidth(Double.MAX_VALUE);
        flipVertically.setOnAction(event -> applyFilter(new InvertImage("vertically")));

        Button invertButton = new Button("Inversion");
        invertButton.setMaxWidth(Double.MAX_VALUE);
        invertButton.setOnAction(event -> applyFilter(new InversionFilter()));

        Button rotateButton = new Button("Rotate");
        rotateButton.setMaxWidth(Double.MAX_VALUE);
        rotateButton.setOnAction(event -> applyFilter(new RotationFilter()));
        
        Button rotateButtonAnti = new Button("Rotate Anti-Clockwise");
        rotateButtonAnti.setMaxWidth(Double.MAX_VALUE);
        rotateButtonAnti.setOnAction(event -> applyFilter(new RotationFilterAnti()));
        
        

        Slider brightnessSlider = new Slider(-100, 100, 0);
        
        Button applyBrightnessButton = new Button("Apply Brightness");
        applyBrightnessButton.setMaxWidth(Double.MAX_VALUE);
        applyBrightnessButton.setOnAction(event -> applyFilter(new BrightnessFilter(brightnessSlider.getValue())));

        Slider contrastSlider = new Slider(-100, 100, 0);
        contrastSlider.getStyleClass().add("my-slider");
        brightnessSlider.getStyleClass().add("my-slider");
        
        
        Button applyContrastButton = new Button("Apply Contrast");
        applyContrastButton.setMaxWidth(Double.MAX_VALUE);
        applyContrastButton.setOnAction(event -> applyFilter(new ContrastFilter(contrastSlider.getValue())));

        Slider blurSlider = new Slider(5, 20, 5);
        blurSlider.getStyleClass().add("my-slider");
        blurSlider.getStyleClass().add("my-slider");
        
        Button blurButton = new Button("Apply Blur");
        blurButton.setMaxWidth(Double.MAX_VALUE);
        blurButton.setOnAction(event -> applyFilter(new BlurFilter(blurSlider.getValue())));
        
        vbox.getChildren().addAll(
            grayscaleButton, blueButton, invertButton, rotateButton, rotateButtonAnti, flipHorizontally, flipVertically,
            new Label("Brightness:"),
            brightnessSlider,
            applyBrightnessButton,
            new Label("Contrast:"),
            contrastSlider,
            applyContrastButton,
            new Label("Blur:"),
            blurSlider,
            blurButton,
            undoButton
        );


        return vbox;
    }
    
    

    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");
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
            
            int height = (int) originalImage.getHeight();
            int width = (int) originalImage.getWidth();
            
            if(width < 1600) {
            	imageView.setFitWidth(width);
            	if(height < 1000) {
                	imageView.setFitHeight(height);
                } else  {
                	imageView.setFitHeight(1000);
                }
            	
            } else  {
            	imageView.setFitWidth(1600);
            }
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

    public class InvertImage implements ImageFilter{
    	
    	private String inversion;
    	
    	public InvertImage(String inversion) {
    		this.inversion = inversion;
    		
    	}
    	@Override
    	public Image apply(Image image) {
    		  
    		        int height=(int) image.getHeight();
    		        int width= (int) image.getWidth();
    		        BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    		        if(inversion.equals("horizontally"))
    		        {
    		            for(int i=0;i<height;i++)
    		            {
    		                for(int j=0;j<width/2;j++)
    		                {
    		                    Color pixel=new Color(image.getPixelReader().getArgb(width-j-1,i));
    		                    outImg.setRGB(width-j-1,i,image.getPixelReader().getArgb(j,i));
    		                    outImg.setRGB(j,i,pixel.getRGB());
    		                }
    		            }
    		        }
    		        else
    		        {
    		            for(int j=0;j<width;j++)
    		            {
    		                for(int i=0;i<height/2;i++)
    		                {
    		                    Color pixel=new Color(image.getPixelReader().getArgb(j,height-i-1));
    		                    outImg.setRGB(j,height-i-1,image.getPixelReader().getArgb(j,i));
    		                    outImg.setRGB(j,i,pixel.getRGB());
    		                }
    		            }
    		        }
    		        
    		        return SwingFXUtils.toFXImage(outImg, null);
    		        
    		    
    	}
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
    
public class BlurFilter implements ImageFilter {
    private double blurRadiusSlider;

    public BlurFilter(double blurRadiusSlider) {
        this.blurRadiusSlider = blurRadiusSlider;
    }

    @Override
    public Image apply(Image image) {
    	
    	 
    	            int n = (int) blurRadiusSlider;
    	            if (n % 2 == 0) {
    	                n++;
    	            }

    	            int m = n / 2;
    	            int width = (int) image.getWidth();
    	            int height = (int) image.getHeight();
    	            BufferedImage blurredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    	            for (int x = 0; x < width; x++) 
    	            {
    	                for (int y = 0; y < height; y++) 
    	                {
    	                    int newRed = 0;
    	                    int newGreen = 0;
    	                    int newBlue = 0;

    	                   
    	                    for (int i = -m; i <= m; i++) 
    	                    {
    	                        for (int j = -m; j <= m; j++) 
    	                        {
    	                            if(i+x<0 || i+x>=width || j+y<0 || j+y>=height)
    	                                continue;
    	                            Color pixel = new Color(image.getPixelReader().getArgb(x + i, y + j));
    	                            newRed += pixel.getRed();
    	                            newGreen += pixel.getGreen();
    	                            newBlue += pixel.getBlue();
    	                        }
    	                    }

    	                    newRed /= (n * n);
    	                    newGreen /= (n * n);
    	                    newBlue /= (n * n);

    	                    blurredImage.setRGB(x, y, new Color(newRed, newGreen, newBlue).getRGB());
    	                }
    	            }
        return SwingFXUtils.toFXImage(blurredImage, null);
    }
}
    public class  RotationFilterAnti implements ImageFilter {
        @Override
        public Image apply(Image image) {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            BufferedImage outImg = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Color originalColor = new Color(image.getPixelReader().getArgb(j, i));
                    outImg.setRGB(i, width - 1 - j, originalColor.getRGB());
                }
            }

            return SwingFXUtils.toFXImage(outImg, null);
        }
    }
    
    public class RotationFilter implements ImageFilter {
        @Override
        public Image apply(Image image) {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            BufferedImage outImg = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Color originalColor = new Color(image.getPixelReader().getArgb(j, i));
                    outImg.setRGB( height - 1 - i, j, originalColor.getRGB());
                }
            }

            return SwingFXUtils.toFXImage(outImg, null);
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

    public class BlueFilter implements ImageFilter {
        @Override
        public Image apply(Image image) {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color color = new Color(image.getPixelReader().getArgb(x, y));
                    int red = color.getRed();
                    int green = color.getGreen();
                    int blue = (int) (color.getBlue() * 1.5); // Increase blue channel intensity

                    // Ensure that blue is clamped to the [0, 255] range
                    blue = Math.min(255, blue);

                    Color filteredColor = new Color(red, green, blue);
                    outImg.setRGB(x, y, filteredColor.getRGB());
                }
            }

            return SwingFXUtils.toFXImage(outImg, null);
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







