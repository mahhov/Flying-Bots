package Map;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Bots.*;

public class SimpleProgression extends AbstractMap {

	public SimpleProgression() {
		levelNum = 10;
		level = 0;
		w = 1600;
		h = 1600;

		backgroundImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D brush = (Graphics2D) backgroundImage.getGraphics();
		int cMax = 150;
		for (int i = 0; i < 10; i++) {
			brush.setStroke(new BasicStroke((int) (Math.random() * 10)));
			brush.setColor(new Color((int) (Math.random() * cMax), (int) (Math
					.random() * cMax), (int) (Math.random() * cMax)));
			brush.drawArc((int) (Math.random() * w), (int) (Math.random() * h),
					(int) (Math.random() * 100), (int) (Math.random() * 100),
					(int) (0), (int) (360));
		}
		brush.setStroke(new BasicStroke(5));
		brush.setColor(new Color(255, 255, 255));
		brush.drawArc(w / 2 - 100, h / 2 - 100, 200, 200, 0, 360);
		brush.setStroke(new BasicStroke(10));
		brush.setColor(new Color(255, 255, 255));
		brush.drawRect(0, 0, w, h);

	}

	public Bots.PlatedBot[] getBots() {
		System.out.println("loading level: " + level);
		int lightFighter = level;
		int slowHeavyArmor = level / 3;
		PlatedBot[] bots = new PlatedBot[lightFighter + slowHeavyArmor + 1];
		bots[0] = new HeavyAttackPlayer(new int[] { w / 2, h / 2 }, 0);
		for (int i = 1; i <= lightFighter; i++)
			bots[i] = new LightFighterComp(new int[] {
					(int) (Math.random() * w), (int) (Math.random() * h) },
					(int) (Math.random() * 500));
		for (int i = 1 + lightFighter; i <= lightFighter + slowHeavyArmor; i++)
			bots[i] = new SlowHeavyArmorComp(new int[] {
					(int) (Math.random() * w), (int) (Math.random() * h) },
					(int) (Math.random() * 500));
		return bots;
	}

}
