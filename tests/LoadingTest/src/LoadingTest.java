import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class LoadingTest {
	public static void main(String[] args) {
	    JFrame frame = new JFrame("Test");

	    ImageIcon loading = new ImageIcon(LoadingTest.class.getResource("/spinner.gif"));
	    frame.add(new JLabel("loading... ", loading, JLabel.CENTER));

	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setSize(400, 300);
	    frame.setVisible(true);
	}
}
