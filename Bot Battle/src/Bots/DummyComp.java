package Bots;

public class DummyComp extends PlatedBot implements Automated {

	public DummyComp(int[] location, int sleep) {
		super(location, 2, 4, 20, 0, 500, 200, 2, 100, 1, sleep);
	}

	public boolean computeMove(int targetX, int targetY, double targetW,
			int mapW, int mapH) {
		goalX = (int) (400 + Math.random() * 50 - 25);
		goalY = (int) (400 + Math.random() * 50 - 25);
		move(goalX, goalY, mapW, mapH);
		return (life > 0);
	}

}
