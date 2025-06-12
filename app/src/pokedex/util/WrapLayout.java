package pokedex.util;

import java.awt.*;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * A FlowLayout subclass that wraps components to new lines when they exceed the container width.
 * Optimized for use within JScrollPane containers and provides better space utilization
 * compared to standard FlowLayout for dynamic content.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public class WrapLayout extends FlowLayout {

    /**
     * Creates a new WrapLayout with left alignment and default gaps.
     */
    public WrapLayout() {
        super(LEFT);
    }

    /**
     * Creates a new WrapLayout with specified alignment and gaps.
     * 
     * @param align the alignment value (LEFT, CENTER, RIGHT)
     * @param hgap  the horizontal gap between components
     * @param vgap  the vertical gap between components
     */
    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    /**
     * Returns the preferred dimensions for this layout given the components
     * in the specified target container.
     * 
     * @param target the container which needs to be laid out
     * @return the preferred dimensions to lay out the subcomponents
     */
    @Override
    public Dimension preferredLayoutSize(Container target) {
        return calculateLayoutSize(target, true);
    }

    /**
     * Returns the minimum dimensions needed to layout the components
     * contained in the specified target container.
     * 
     * @param target the container which needs to be laid out
     * @return the minimum dimensions to lay out the subcomponents
     */
    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = calculateLayoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    /**
     * Calculates the layout size based on the target container and whether
     * preferred or minimum size is requested.
     * 
     * @param target    the target container
     * @param preferred true for preferred size, false for minimum size
     * @return the calculated layout dimensions
     */
    private Dimension calculateLayoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = getTargetWidth(target);
            LayoutMetrics metrics = new LayoutMetrics(target);
            
            Dimension result = new Dimension(0, 0);
            int currentRowWidth = 0;
            int currentRowHeight = 0;
            
            for (int i = 0; i < target.getComponentCount(); i++) {
                Component component = target.getComponent(i);
                
                if (!component.isVisible()) {
                    continue;
                }
                
                Dimension componentSize = preferred ? 
                    component.getPreferredSize() : 
                    component.getMinimumSize();
                
                // Check if component should wrap to new line
                if (shouldWrapComponent(currentRowWidth, componentSize.width, 
                                      targetWidth, metrics.maxWidth)) {
                    addRowToResult(result, currentRowWidth, currentRowHeight);
                    currentRowWidth = 0;
                    currentRowHeight = 0;
                }
                
                // Add horizontal gap if not first component in row
                if (currentRowWidth > 0) {
                    currentRowWidth += metrics.hgap;
                }
                
                currentRowWidth += componentSize.width;
                currentRowHeight = Math.max(currentRowHeight, componentSize.height);
            }
            
            // Add the last row
            addRowToResult(result, currentRowWidth, currentRowHeight);
            
            // Add container insets and gaps
            result.width += metrics.horizontalInsetsAndGap;
            result.height += metrics.insets.top + metrics.insets.bottom + (metrics.vgap * 2);
            
            return adjustForScrollPane(target, result);
        }
    }

    /**
     * Determines the effective target width for layout calculations.
     */
    private int getTargetWidth(Container target) {
        int width = target.getSize().width;
        return width == 0 ? Integer.MAX_VALUE : width;
    }

    /**
     * Determines whether a component should wrap to a new line.
     */
    private boolean shouldWrapComponent(int currentRowWidth, int componentWidth, 
                                      int targetWidth, int maxWidth) {
        return currentRowWidth + componentWidth > maxWidth && targetWidth != Integer.MAX_VALUE;
    }

    /**
     * Adds a completed row's dimensions to the overall result.
     */
    private void addRowToResult(Dimension result, int rowWidth, int rowHeight) {
        result.width = Math.max(result.width, rowWidth);
        if (result.height > 0) {
            result.height += getVgap();
        }
        result.height += rowHeight;
    }

    /**
     * Adjusts the result dimensions when contained within a JScrollPane.
     */
    private Dimension adjustForScrollPane(Container target, Dimension result) {
        Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
        if (scrollPane != null) {
            result.width -= (getHgap() + 1);
        }
        return result;
    }

    /**
     * Helper class to encapsulate layout calculation metrics.
     */
    private class LayoutMetrics {
        final int hgap;
        final int vgap;
        final Insets insets;
        final int horizontalInsetsAndGap;
        final int maxWidth;

        LayoutMetrics(Container target) {
            this.hgap = getHgap();
            this.vgap = getVgap();
            this.insets = target.getInsets();
            this.horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
            this.maxWidth = target.getSize().width - horizontalInsetsAndGap;
        }
    }
}