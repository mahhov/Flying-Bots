import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.JFrame;

import Bots.*;
import Map.*;

public class Arena extends JFrame implements MouseMotionListener, MouseListener {
	PlatedBot player;
	PlatedBot[] comp;
	Graphics2D brush;
	Image canvas;
	boolean running;
	int mouseX, mouseY;
	int playerGoalX, playerGoalY;
	boolean printScreen;
	boolean restart;
	int screen = 0;
	SoundPlayer sound;
	AbstractMap map;
	int shiftX, shiftY;

	public static void main(String[] args) {
		Arena arena = new Arena();
		arena.beginLoop();
	}

	Arena() {
		super();

		setResizable(false);
		setSize(800, 800);
		setLocation(100, 100);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		canvas = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
		brush = (Graphics2D) canvas.getGraphics();
		brush.setFont(new Font("monospaced", Font.PLAIN, 25));

		addMouseMotionListener(this);
		addMouseListener(this);

		map = new SimpleProgression();
		sound = new SoundPlayer();
	}

	public void initiateBots(boolean nextLevel) {
		if (nextLevel)
			if (map.nextLevel()) {
				PlatedBot[] bots = map.getBots();
				player = bots[0];
				comp = new PlatedBot[bots.length - 1];
				for (int i = 1; i < bots.length; i++)
					comp[i - 1] = bots[i];
			} else
				System.out.println("victory!");
		else {
			map.restartTimer();
			PlatedBot[] bots = map.getBots();
			player = bots[0];
			comp = new PlatedBot[bots.length - 1];
			for (int i = 1; i < bots.length; i++)
				comp[i - 1] = bots[i];
		}
		shiftX = player.x - 400;
		shiftY = player.y - 400;
	}

	public void beginLoop() {
		running = true;

		brush.clearRect(0, 0, 800, 800);
		sound.intro.play();
		wait(500);

		while (running) {

			initiateBots(!restart);

			// starting
			System.out.println("starting");
			int mapStart;
			while ((mapStart = map.timer()) != 0) {
				// brush.setColor(Color.black);
				// brush.fillRect(0, 0, 800, 800);
				PlatedBot.DrawInfo playerDrawInfo = player.getDrawInfo();
				PlatedBot.DrawInfo[] compDrawInfo = new PlatedBot.DrawInfo[comp.length];
				for (int i = 0; i < comp.length; i++)
					compDrawInfo[i] = comp[i].getDrawInfo();
				drawBot(playerDrawInfo, true);
				for (int i = 0; i < comp.length; i++)
					drawBot(compDrawInfo[i], false);
				brush.setColor(Color.white);
				brush.drawString("LEVEL " + map.level, 100, 100 + brush
						.getFontMetrics().getAscent());
				brush.setColor(new Color(0f, 0f, 0f, (float) (mapStart / 100.0)));
				brush.fillRect(0, 0, 800, 800);
				repaint();
				wait(15);
			}

			// running
			System.out.println("running");
			boolean anyCompAlive = true;
			restart = false;
			while (anyCompAlive & !restart) {
				playerGoalX = mouseX + shiftX;
				playerGoalY = mouseY + shiftY;
				int camSlow = 10;
				shiftX = (shiftX * camSlow + player.x - 400) / (camSlow + 1);
				shiftY = (shiftY * camSlow + player.y - 400) / (camSlow + 1);
				if (player.life <= 0)
					restart = true;
				anyCompAlive = false;
				player.move(playerGoalX, playerGoalY, map.w, map.h);
				for (int i = 0; i < comp.length; i++)
					if (((Automated) comp[i]).computeMove(player.x, player.y,
							player.wa, map.w, map.h))
						anyCompAlive = true;
				PlatedBot.DrawInfo playerDrawInfo = player.getDrawInfo();
				PlatedBot.DrawInfo[] compDrawInfo = new PlatedBot.DrawInfo[comp.length];
				for (int i = 0; i < comp.length; i++)
					compDrawInfo[i] = comp[i].getDrawInfo();
				checkCollisions(playerDrawInfo, compDrawInfo);
				drawBot(playerDrawInfo, true);
				for (int i = 0; i < comp.length; i++)
					drawBot(compDrawInfo[i], false);
				paintStunFuzz(playerDrawInfo.stunBar);
				paintArmorSurroundDisplay(playerDrawInfo.armor,
						playerDrawInfo.maxArmor, playerDrawInfo.lifeBar,
						playerDrawInfo.stunBar);
				repaint();
				wait(15);
			}

			// ending
			System.out.println("ending");
			map.restartTimer();
			int mapEnd;
			while ((mapEnd = map.timer()) != 0) {
				playerGoalX = mouseX + shiftX;
				playerGoalY = mouseY + shiftY;
				player.move(playerGoalX, playerGoalY, map.w, map.h);
				for (int i = 0; i < comp.length; i++)
					((Automated) comp[i]).computeMove(player.x, player.y,
							player.wa, map.w, map.h);
				PlatedBot.DrawInfo playerDrawInfo = player.getDrawInfo();
				PlatedBot.DrawInfo[] compDrawInfo = new PlatedBot.DrawInfo[comp.length];
				for (int i = 0; i < comp.length; i++)
					compDrawInfo[i] = comp[i].getDrawInfo();
				checkCollisions(playerDrawInfo, compDrawInfo);
				drawBot(playerDrawInfo, true);
				for (int i = 0; i < comp.length; i++)
					drawBot(compDrawInfo[i], false);
				brush.setColor(new Color(0f, 0f, 0f,
						(float) ((100 - mapEnd) / 100.0)));
				brush.fillRect(0, 0, 800, 800);
				repaint();
				wait(30);
			}
		}

		System.exit(0);
	}

	public void checkCollisions(PlatedBot.DrawInfo pDrawInfo,
			PlatedBot.DrawInfo[] cDrawInfo) {

		brush.setColor(Color.green);

		int[] p = pDrawInfo.coords;
		int[] c;

		for (int i = 0; i < cDrawInfo.length; i++) {

			if (cDrawInfo[i].sleep == 0) {

				c = cDrawInfo[i].coords;

				Line pLeft = new Line(p[0], p[1], p[4], p[5]);
				Line pTop = new Line(p[0], p[1], p[2], p[3]);
				Line pRight = new Line(p[2], p[3], p[6], p[7]);
				Line pBottom = new Line(p[4], p[5], p[6], p[7]);
				Line pWeapon = new Line(p[8], p[9], p[10], p[11]);

				Line cLeft = new Line(c[0], c[1], c[4], c[5]);
				Line cTop = new Line(c[0], c[1], c[2], c[3]);
				Line cRight = new Line(c[2], c[3], c[6], c[7]);
				Line cBottom = new Line(c[4], c[5], c[6], c[7]);
				Line cWeapon = new Line(c[8], c[9], c[10], c[11]);

				int[] collideLocation;
				int side;

				// player hits computer
				int[] collisionPass = null;
				side = 0;

				if ((collideLocation = pWeapon.intersectBothLocations(cLeft)) != null) {
					if (collisionPass == null
							|| collisionPass[2] > collideLocation[2]) {
						collisionPass = collideLocation;
						side = 0;
					}
				}
				if ((collideLocation = pWeapon.intersectBothLocations(cRight)) != null) {
					if (collisionPass == null
							|| collisionPass[2] > collideLocation[2]) {
						collisionPass = collideLocation;
						side = 2;
					}
				}
				if ((collideLocation = pWeapon.intersectBothLocations(cTop)) != null) {
					if (collisionPass == null
							|| collisionPass[2] > collideLocation[2]) {
						collisionPass = collideLocation;
						side = 1;
					}
				}
				if ((collideLocation = pWeapon.intersectBothLocations(cBottom)) != null) {
					if (collisionPass == null
							|| collisionPass[2] > collideLocation[2]) {
						collisionPass = collideLocation;
						side = 3;
					}
				}

				if (collisionPass != null) {
					sound.weaponDamage.play();
					double[] pMomentum = player
							.getWeaponMomentum(collisionPass[2]);
					double[] cMomentum = comp[i].getWeaponMomentum(0);
					double[] momentum = new double[] {
							pMomentum[0] - cMomentum[0],
							pMomentum[1] - cMomentum[1] };
					int size = 3 + distance(momentum) / 3;
					brush.fillArc(collisionPass[0] - size - shiftX,
							collisionPass[1] - size - shiftY, size * 2,
							size * 2, 0, 360);
					if (comp[i].damage(momentum, side, collisionPass[3]))
						sound.die.play();
					player.recoil(momentum, collisionPass[2]);
				}

				// computer hits player
				collisionPass = null;
				side = 0;

				if ((collideLocation = cWeapon.intersectBothLocations(pLeft)) != null) {
					if (collisionPass == null
							|| collisionPass[2] > collideLocation[2]) {
						collisionPass = collideLocation;
						side = 0;
					}
				}
				if ((collideLocation = cWeapon.intersectBothLocations(pRight)) != null) {
					if (collisionPass == null
							|| collisionPass[2] > collideLocation[2]) {
						collisionPass = collideLocation;
						side = 2;
					}
				}
				if ((collideLocation = cWeapon.intersectBothLocations(pTop)) != null) {
					if (collisionPass == null
							|| collisionPass[2] > collideLocation[2]) {
						collisionPass = collideLocation;
						side = 1;
					}
				}
				if ((collideLocation = cWeapon.intersectBothLocations(pBottom)) != null) {
					if (collisionPass == null
							|| collisionPass[2] > collideLocation[2]) {
						collisionPass = collideLocation;
						side = 3;
					}
				}

				if (collisionPass != null) {
					sound.weaponDamage.play();
					double[] cMomentum = comp[i]
							.getWeaponMomentum(collisionPass[2]);
					double[] pMomentum = player.getWeaponMomentum(0);
					double[] momentum = new double[] {
							cMomentum[0] - pMomentum[0],
							cMomentum[1] - pMomentum[1] };
					int size = 3 + distance(momentum) / 3;
					brush.fillArc(collisionPass[0] - size - shiftX,
							collisionPass[1] - size - shiftY, size * 2,
							size * 2, 0, 360);
					if (player.damage(momentum, side, collisionPass[3]))
						sound.die.play();
					comp[i].recoil(momentum, collisionPass[2]);
					brush.setColor(new Color(1f, 0f, 0f, (float) Math.min(
							distance(momentum) / 50.0, 1)));
					brush.fillRect(0, 0, 800, 800);
				}

				// weapons collide
				if ((collideLocation = pWeapon.intersectBothLocations(cWeapon)) != null) {
					sound.weaponReflect.play();
					double[] pMomentum = player
							.getWeaponMomentum(collideLocation[2]);
					double[] cMomentum = comp[i]
							.getWeaponMomentum(collideLocation[3]);
					double[] momentum = new double[] {
							pMomentum[0] - cMomentum[0],
							pMomentum[1] - cMomentum[1] };
					int size = 3 + distance(momentum) / 3;
					brush.fillArc(collideLocation[0] - size - shiftX,
							collideLocation[1] - size - shiftY, size * 2,
							size * 2, 0, 360);
					player.recoil(momentum, collideLocation[2]);
					comp[i].recoil(negativeDArray(momentum), collideLocation[3]);
				}

				// bodies collide
				collisionPass = null;
				boolean horizontal = false;

				if ((collideLocation = pLeft.intersect(cRight)) != null) {
					collisionPass = collideLocation;
					horizontal = true;
				}
				if ((collideLocation = pRight.intersect(cLeft)) != null) {
					if (collisionPass != null)
						System.out.println("overwrite body collision");
					collisionPass = collideLocation;
					horizontal = true;
				}
				if ((collideLocation = pTop.intersect(cBottom)) != null) {
					if (collisionPass != null)
						System.out.println("overwrite body collision");
					collisionPass = collideLocation;
					horizontal = false;
				}
				if ((collideLocation = pBottom.intersect(cTop)) != null) {
					if (collisionPass != null)
						System.out.println("overwrite body collision");
					collisionPass = collideLocation;
					horizontal = false;
				}

				if (collisionPass != null) {
					sound.bodySlam.play();
					double[] pMomentum = player.getWeaponMomentum(0);
					double[] cMomentum = comp[i].getWeaponMomentum(0);
					int momentum;
					if (horizontal)
						momentum = (int) (pMomentum[0] - cMomentum[0]);
					else
						momentum = (int) (pMomentum[1] - cMomentum[1]);
					int size = 3 + Math.abs(momentum) / 1;
					brush.fillArc(collisionPass[0] - size - shiftX,
							collisionPass[1] - size - shiftY, size * 2,
							size * 2, 0, 360);
					if (player.damage(horizontal, -momentum))
						sound.die.play();
					if (comp[i].damage(horizontal, momentum))
						sound.die.play();// }
				}

			}

		} // end i loop
	}

	public void drawBot(PlatedBot.DrawInfo drawInfo, boolean blue) {
		Color primaryColor = null;
		if (blue)
			if (drawInfo.lifeBar > 0)
				primaryColor = new Color(0, 0, 255);
			else
				primaryColor = new Color(0, 0, 150);
		else if (drawInfo.lifeBar > 0)
			primaryColor = new Color(Math.max(0, 255 - drawInfo.sleep), 0, 0);
		else
			primaryColor = new Color(150, 0, 0);
		brush.setColor(primaryColor);

		// life
		int s = (int) (drawInfo.stunBar / 100.0 * 255);

		int lifeW = (int) ((drawInfo.coords[2] - drawInfo.coords[0]) * (drawInfo.lifeBar / 100.0));
		int barY = Math.min(drawInfo.coords[1], drawInfo.coords[3]) - 15;
		brush.setColor(greenifyColor(primaryColor, s));
		brush.fillRect(drawInfo.coords[0] - shiftX, barY - shiftY, lifeW, 8);// life
																				// bar

		// weapon
		brush.setStroke(new BasicStroke(5));
		brush.drawLine(drawInfo.coords[8] - shiftX,
				drawInfo.coords[9] - shiftY, drawInfo.coords[10] - shiftX,
				drawInfo.coords[11] - shiftY);

		// body
		drawSegmentedLine(drawInfo.coords[0], drawInfo.coords[1],
				drawInfo.coords[2], drawInfo.coords[3], drawInfo.armor[1],
				drawInfo.maxArmor, primaryColor, s, false);// up
		drawSegmentedLine(drawInfo.coords[0], drawInfo.coords[1],
				drawInfo.coords[4], drawInfo.coords[5], drawInfo.armor[0],
				drawInfo.maxArmor, primaryColor, s, false);// left
		drawSegmentedLine(drawInfo.coords[2], drawInfo.coords[3],
				drawInfo.coords[6], drawInfo.coords[7], drawInfo.armor[2],
				drawInfo.maxArmor, primaryColor, s, false);// right
		drawSegmentedLine(drawInfo.coords[4], drawInfo.coords[5],
				drawInfo.coords[6], drawInfo.coords[7], drawInfo.armor[3],
				drawInfo.maxArmor, primaryColor, s, false);// down

		// stun
		// int stunW = (int) ((drawInfo.coords[2] - drawInfo.coords[0]) *
		// (drawInfo.stunBar / 100.0));
		// brush.setColor(Color.green);
		// brush.fillRect(drawInfo.coords[0], barY - 8, stunW, 8);
		// brush.fillRect(coords[0] + lifeW, barY, stunW, 8);
	}

	public void drawSegmentedLine(int x1, int y1, int x2, int y2,
			int[][] values, int maxValue, Color primaryColor, int stun,
			boolean screenCoords) {
		int n = values.length;
		double dx = (x2 - x1) * 1.0 / n;
		double dy = (y2 - y1) * 1.0 / n;
		double ix, iy;
		for (int i = 0; i < n; i++) {
			// color
			// brush.setColor(new Color(primaryColor.getRed(), Math.min(255,
			// primaryColor.getGreen() + values[i][1]), primaryColor
			// .getBlue()));

			// color
			brush.setColor(greenifyColor(
					greenifyColor(primaryColor, values[i][1]), stun));

			// line
			ix = x1 + i * dx;
			iy = y1 + i * dy;
			if (!screenCoords) {
				// width
				brush.setStroke(new BasicStroke(
						(int) (values[i][0] * 7.0 / maxValue)));
				brush.drawLine((int) ix - shiftX, (int) iy - shiftY,
						(int) (ix + dx) - shiftX, (int) (iy + dy) - shiftY);
			} else {
				brush.setColor(greenifyColor(
						greenifyColor(primaryColor,
								255 - (255 * values[i][0] / maxValue)), stun));
				// width
				brush.setStroke(new BasicStroke(
						(int) (values[i][0] * 10.0 / maxValue)));
				brush.drawLine((int) ix, (int) iy, (int) (ix + dx),
						(int) (iy + dy));
			}
		}
	}

	public Color greenifyColor(Color c, int green) {
		return new Color(c.getRed(), Math.min(255, c.getGreen() + green),
				c.getBlue());
	}

	public void paint(Graphics g) {
		if (brush != null & (map != null && map.backgroundImage != null)) {
			g.drawImage(canvas, 0, 0, null);
			brush.drawImage(map.backgroundImage, 0 - shiftX, 0 - shiftY, map.w,
					map.h, null);
			brush.setColor(new Color(0f, 0f, 0f, 0.1f));
			brush.fillRect(0, 0, 800, 800);

			if (false & printScreen) {
				try {
					ImageIO.write((RenderedImage) canvas, "bmp", new File(
							"C:\\Users\\m\\Desktop\\screenShots\\img"
									+ (screen++) + ".bmp"));
					System.out.println(screen);
				} catch (IOException e) {
					e.printStackTrace();
				}
				printScreen = false;
			}
		}
	}

	public void paintStunFuzz(int stun) {
		for (int i = 0; i < stun; i++) {
			brush.setColor(new Color(rand(100), rand(100), rand(100)));
			brush.setStroke(new BasicStroke(rand(10)));
			brush.drawArc(rand(800), rand(800), rand(20), rand(20), rand(360),
					rand(360));
		}
	}

	public void paintArmorSurroundDisplay(int[][][] armor, int maxArmor,
			int lifeBar, int stunBar) {
		// body
		Color primaryColor = null;
		if (lifeBar > 0)
			primaryColor = new Color(0, 0, 255);
		else
			primaryColor = new Color(0, 0, 150);

		int s = (int) (stunBar / 100.0 * 255);

		drawSegmentedLine(20, 50, 780, 50, armor[1], maxArmor, primaryColor, s,
				true);// up
		drawSegmentedLine(20, 50, 20, 780, armor[0], maxArmor, primaryColor, s,
				true);// left
		drawSegmentedLine(780, 50, 780, 780, armor[2], maxArmor, primaryColor,
				s, true);// right
		drawSegmentedLine(20, 780, 780, 780, armor[3], maxArmor, primaryColor,
				s, true);// down
	}

	public int rand(int upperBound) {
		return (int) (Math.random() * upperBound);
	}

	public void wait(int howLong) {
		try {
			Thread.sleep(howLong);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	public void mouseClicked(MouseEvent arg0) {
		restart = true;
		// initiateBots(false);
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
		printScreen = true;
	}

	public void mouseReleased(MouseEvent arg0) {
	}

	public int distance(double[] a) {
		return (int) Math.sqrt(Math.pow(a[0], 2) + Math.pow(a[1], 2));
	}

	public double[] negativeDArray(double[] a) {
		for (int i = 0; i < a.length; i++)
			a[i] *= -1;
		return a;
	}

	public class Line {
		double m, b;
		int left, right;
		boolean orderSwitched; // whether x1,y1->left or switched to right

		public Line(int x1, int y1, int x2, int y2) {
			if (x1 < x2) {
				orderSwitched = false;
				left = x1;
				right = x2;
			} else {
				orderSwitched = true;
				left = x2;
				right = x1;
			}
			// left = Math.min(x1, x2);
			// right = Math.max(x1, x2);
			// orderSwitched=(x2<x1);
			m = (y2 - y1) * 1.0 / (x2 - x1);
			b = y1 - m * x1;
		}

		public int getPositionScale(double x) {
			int positionScale = (int) ((x - left) * 10000.0 / (right - left));
			if (orderSwitched)
				positionScale = 10000 - positionScale;
			return positionScale;
		}

		public int[] intersect(Line line2) {
			// return x of collision, y of collision, x scale of parent line,
			try {
				double x = (line2.b - b) * 1.0 / (m - line2.m);
				if ((left < x & x < right) & (line2.left < x & x < line2.right)) {
					return new int[] { (int) x, (int) (b + x * m),
							getPositionScale(x) };
				}
			} catch (ArithmeticException e) {
			}
			return null;
		}

		public int[] intersectBothLocations(Line line2) {
			// return x of collision, y of collision, x scale of parent line,
			// x scale of argument
			try {
				double x = (line2.b - b) * 1.0 / (m - line2.m);
				if ((left < x & x < right) & (line2.left < x & x < line2.right)) {
					return new int[] { (int) x, (int) (b + x * m),
							getPositionScale(x), line2.getPositionScale(x) };
				}
			} catch (ArithmeticException e) {
			}
			return null;
		}
	}

	public class SoundPlayer {

		Sound intro, weaponDamage, weaponReflect, bodySlam, die, victory, loss;

		public SoundPlayer() {
			intro = new Sound("Derezzed");
			weaponDamage = new Sound("hit");
			weaponReflect = new Sound("reflect");
			bodySlam = new Sound("body");
			die = new Sound("die");

			weaponDamage = new Sound("hit");
			weaponReflect = new Sound("hit");
			bodySlam = new Sound("hit");
			// victory=new Sound("");
			// loss=new Sound("");
		}

		public class Sound {
			Clip sound;

			public Sound(String file) {
				try {
					sound = AudioSystem.getClip();
					AudioInputStream inputStream = AudioSystem
							.getAudioInputStream(new File(
									"C:\\Users\\m\\Desktop\\sounds\\" + file
											+ ".wav"));
					sound.open(inputStream);
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				} catch (UnsupportedAudioFileException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			public void play() {
				sound.stop();
				sound.setFramePosition(0);
				sound.start();
			}
		}

	}

}

// collision particles
// upgrade system

// better AI 6
// levels 4
// damage report 3
// improve sounds 5
// transparent body collisions 8