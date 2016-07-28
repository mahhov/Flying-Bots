
public class CompBot extends Bot {
	int goalX, goalY;
	int strategy;
	final static int CENTER = 0; // approach center
	final static int SPIN = 1; // spin in place
	final static int SIMPLE = 2; // approach while spinning
	final static int DEFENSIVE = 3; // approach while spinning and your weapon
									// away
	final static int BLIND = 4; // approach rapidly without spinning

	CompBot(int x, int y, int strategy) {
		super(x, y);
		this.strategy = strategy;

		maxLife = 100;
		life = maxLife;
	}

	public void computeMove(int targetX, int targetY, double targetW) {
		switch (strategy) {
		case CENTER:
			goalX = 400;
			goalY = 400;
			break;
		case SPIN:
			goalX = (int) (x + 200.0 * Math.cos(wa - 125.0 * Math.PI / 180.0));
			goalY = (int) (y - 200.0 * Math.sin(wa - 125.0 * Math.PI / 180.0));
			break;
		case SIMPLE:
			goalX = (int) ((targetX * 1.0 + 1.0 * x + 300.0 * Math.cos(wa
					- 125.0 * Math.PI / 180.0)) / 2.0);
			goalY = (int) ((targetY * 1.0 + 1.0 * y - 300.0 * Math.sin(wa
					- 125.0 * Math.PI / 180.0)) / 2.0);
			break;
		case DEFENSIVE:
			if (Math.pow(targetX + 200 * Math.cos(targetW) - x, 2)
					+ Math.pow(targetY - 200 * Math.sin(targetW) - y, 2) > 200 * 200) {
				goalX = (int) ((targetX * 1.0 + 1.0 * x + 300.0 * Math.cos(wa
						- 125.0 * Math.PI / 180.0)) / 2.0);
				goalY = (int) ((targetY * 1.0 + 1.0 * y - 300.0 * Math.sin(wa
						- 125.0 * Math.PI / 180.0)) / 2.0);
			} else {
				goalX = (int) (x + 600 * Math.cos(wa - 125.0 * Math.PI / 180.0));
				goalY = (int) (y - 600 * Math.sin(wa - 125.0 * Math.PI / 180.0));
			}
			break;
		case BLIND:
			goalX = (int) ((targetX + x * 2) / 3);
			goalY = (int) ((targetY + y * 2) / 3);
			break;
		}

		move(goalX, goalY);
	}

}
