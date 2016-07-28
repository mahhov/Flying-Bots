package Bots;

public class SlowHeavyArmorComp extends PlatedBot implements Automated {

	public SlowHeavyArmorComp(int[] location, int sleep) {
		super(location, 3, 5, 20, 10, 500, 250, 5, 200, 0.1, sleep);
	}

	public boolean computeMove(int targetX, int targetY, double targetW,
			int mapW, int mapH) {
		double aa = 125;
		double rr = 1000;
		goalX = (int) ((targetX * 1.0 + 1.0 * x + rr
				* Math.cos(wa - aa * Math.PI / 180.0)) / 2.0);
		goalY = (int) ((targetY * 1.0 + 1.0 * y - rr
				* Math.sin(wa - aa * Math.PI / 180.0)) / 2.0);
		move(goalX, goalY, mapW, mapH);
		return (life > 0);
	}

}
