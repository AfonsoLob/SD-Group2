package GUI;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * Utility class providing consistent styling for GUI components.
 */
public class GuiStyles {
    // Colors
    public static final Color CANDIDATE_A_COLOR = new Color(0, 102, 204);
    public static final Color CANDIDATE_B_COLOR = new Color(204, 0, 0);
    public static final Color SUCCESS_COLOR = new Color(0, 153, 0);
    public static final Color ERROR_COLOR = Color.RED;
    
    // Fonts
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font CONTENT_FONT = new Font("Arial", Font.PLAIN, 14);
    public static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font SMALL_FONT = new Font("Arial", Font.PLAIN, 12);
    
    // Create a titled border with consistent style
    public static Border createTitledBorder(String title, int position) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            title, 
            position, 
            TitledBorder.TOP
        );
    }
}
