package Bots;

public class LightFighterComp extends PlatedBot implements Automated {

	public LightFighterComp(int[] location, int sleep) {
		super(location, 2, 4, 20, 4, 100, 50, 7, 120, 1, sleep);
	}

	public boolean computeMove(int targetX, int targetY, double targetW,
			int mapW, int mapH) {
		goalX = (int) ((targetX * 1.0 + 1.0 * x + 300.0 * Math.cos(wa - 125.0
				* Math.PI / 180.0)) / 2.0);
		goalY = (int) ((targetY * 1.0 + 1.0 * y - 300.0 * Math.sin(wa - 125.0
				* Math.PI / 180.0)) / 2.0);
		move(goalX, goalY, mapW, mapH);
		return (life > 0);
	}

}
