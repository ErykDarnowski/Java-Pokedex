import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class LoadingTestWindow extends JFrame {

	public LoadingTestWindow() {
		// Window setup
		setSize(800, 600);
		setLocationRelativeTo(null); // center window
		setDefaultCloseOperation(EXIT_ON_CLOSE); // close on `X` click
		
		setTitle("Java Pokedex");
		getContentPane().setBackground(new Color(219, 47, 66)); // 215, 10, 55
		setVisible(true);

		// --- Loading Screen ---
		ImageIcon loading = new ImageIcon(LoadingTest.class.getResource("spinner.gif")); // or later approach (with try-catch)?
		JLabel label = new JLabel("Ładowanie... ", loading, JLabel.CENTER);

		label.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 17));
		label.setForeground(new Color(238, 238, 238));

		// text below icon
		label.setVerticalTextPosition(JLabel.BOTTOM);
		label.setHorizontalTextPosition(JLabel.CENTER);

		// center align label within frame
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setVerticalAlignment(JLabel.CENTER);

		add(label);
	}

}