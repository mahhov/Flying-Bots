public package Bots;

public class PlatedBot {
	public int x, y;
	int w, h, bw, bh;
	int oldX, oldY; // used for collision reverses
	double a, ma;
	public double wa;
	double mwa, mx, my;
	double maxSpeed, maxWSpeed;
	int weaponLength, weaponAttack;
	public int life;
	int maxLife, maxArmor;
	int stun;
	public int sleep;
	int[][][] armor; // left,up,right,down; i; amount, flash
	double stunModify;
	int armorColorModify = 5;
	int goalX, goalY;

	public PlatedBot(int[] location, int bodyWidth, int bodyHeight,
			int segmentSize, int attack, int life, int armor, int speed,
			int weaponLength, double stunModify, int sleep) {
		this.x = location[0];
		this.y = location[1];
		bw = bodyWidth;
		bh = bodyHeight;

		a = 0;
		mx = 0;
		my = 0;
		ma = 0;
		w = bw * segmentSize;
		h = bh * segmentSize;
		wa = -Math.PI / 2;
		stun = 0;
		this.stunModify = stunModify;
		weaponAttack = attack;

		maxLife = life;
		maxArmor = armor;
		maxSpeed = speed;
		this.weaponLength = weaponLength;
		maxWSpeed = 15 * Math.PI / 180;

		this.life = maxLife;
		this.sleep = sleep;
		this.armor = new int[][][] { new int[bh][2], new int[bw][2],
				new int[bh][2], new int[bw][2] };
		for (int side = 0; side < this.armor.length; side++)
			for (int i = 0; i < this.armor[side].length; i++)
				this.armor[side][i] = new int[] { maxArmor, 0 };
	}

	public PlatedBot(int[] location, int sleep) {

		this(location, 2, 4, 20, 6, 200, 100, 7, 120, 1, sleep);

		// this.x = location[0];
		// this.y = location[1];
		// bw = 2;
		// bh = 4;
		//
		// a = 0;
		// mx = 0;
		// my = 0;
		// ma = 0;
		// w = bw * 20;
		// h = bh * 20;
		// wa = -Math.PI / 2;
		// stun = 0;
		// weaponAttack = 6;

		// maxLife = 200;
		// maxArmor = 100;
		// maxSpeed = 7;
		// weaponLength = 120;
		// maxWSpeed = 90 * Math.PI / 180;
		//
		// life = maxLife;
		// armor = new int[][][] { new int[bh][2], new int[bw][2], new
		// int[bh][2],
		// new int[bw][2] };
		// for (int side = 0; side < armor.length; side++)
		// for (int i = 0; i < armor[side].length; i++)
		// armor[side][i] = new int[] { maxArmor, 0 };
	}

	public void move(int towardsX, int towardsY, int mapW, int mapH) {
		if (sleep > 0)
			sleep--;
		else {

			oldX = x;
			oldY = y;
			double oldwx = x + weaponLength * Math.cos(wa);
			double oldwy = y - weaponLength * Math.sin(wa);

			if (stun == 0 & life > 0) {
				mx += (towardsX - x) / 200.0;
				my += (towardsY - y) / 200.0;
			} else {
				my += 0.3;
				if (stun > 0)
					stun--;
			}

			for (int side = 0; side < armor.length; side++)
				for (int i = 0; i < armor[side].length; i++)
					if (armor[side][i][1] > 0)
						armor[side][i][1]--;

			double speed = Math.sqrt(Math.pow(mx, 2) + Math.pow(my, 2));
			if (speed > maxSpeed) {
				double movementAngle = Math.atan2(my, mx);
				my = (int) (Math.sin(movementAngle) * maxSpeed);
				mx = (int) (Math.cos(movementAngle) * maxSpeed);
			}

			double goalA = Math.atan2(15, mx) - Math.PI / 2;
			ma = (goalA - a);

			x += Math.round(mx);
			y += Math.round(my);
			if (x - w / 2 < 0) {
				mx *= -0.2;
				x = 0 + w / 2;
			}
			if (x + w / 2 > mapW) {
				mx *= -0.2;
				x = mapW - w / 2;
			}
			if (y - h / 2 < 30) {
				my *= -0.2;
				y = 30 + h / 2;
			}
			if (y + h / 2 > mapH) {
				my *= -.2;
				y = mapH - h / 2;
			}
			mx *= 0.93;
			my *= 0.93;
			a += ma;

			double goalWA = Math.atan2((y - 3) - oldwy, -x + oldwx);
			double dwa = (goalWA - wa);
			while (dwa > Math.PI)
				dwa -= Math.PI * 2;
			while (dwa < -Math.PI)
				dwa += Math.PI * 2;
			mwa += dwa / 20;
			if (mwa > maxWSpeed)
				mwa = maxWSpeed;
			if (mwa < -maxWSpeed)
				mwa = -maxWSpeed;
			wa += mwa;
			mwa *= 0.993;
		}
	}

	public boolean damage(boolean horizontal, double momentum) {
		// body to body
		// returns true if died
		// if (life > 0) {
		x = oldX;
		y = oldY;
		int damage = 0;
		if (horizontal) {
			damage = (int) Math.abs(momentum - mx);
			mx += momentum;
		} else {
			damage = (int) Math.abs(momentum - my);
			my += momentum;
		}
		stun += damage * stunModify;
		// life -= damage;
		if (life < 0) {
			return true;
		}
		// }
		return false;
	}

	public boolean damage(double[] momentum, int side, int scale) {
		// weapon to body
		// returns true if died
		// if (life > 0) {
		int amount = (int) Math.sqrt(Math.pow(momentum[0], 2)
				+ Math.pow(momentum[1], 2));
		mx += momentum[0];
		my += momentum[1];
		stun += amount * stunModify;
		int i = 0;
		if (side == 0 || side == 2)
			i = (int) ((scale - 1) * 1.0 / 10000 * bh);
		if (side == 1 || side == 3)
			i = (int) ((scale - 1) * 1.0 / 10000 * bw);
		armor[side][i][0] -= amount;
		armor[side][i][1] += amount * armorColorModify;
		if (armor[side][i][0] < 0) {
			life += armor[side][i][0];
			armor[side][i][0] = 0;
			if (life < 0) {
				life = 0;
				return true;
			}
			// }
		}
		return false;
	}

	public void recoil(double[] momentum, int collisionLocation) {
		// int collisionLocation=how far from center, 0 to 10,000
		double weaponWeight = weaponAttack / (collisionLocation / 10000.0)
				* weaponLength;
		mwa += momentum[0] / weaponWeight * Math.sin(wa) + momentum[1]
				/ weaponWeight * Math.cos(wa);
		double velocityChange = Math.sqrt(Math.pow(momentum[0] * Math.cos(wa),
				2) + Math.pow(momentum[1] * Math.sin(wa), 2));
		mx -= velocityChange * Math.cos(wa);
		my += velocityChange * Math.sin(wa);
	}

	public double[] getWeaponMomentum(int collisionLocation) {
		// int collisionLocation=how far from center, 0 to 10,000
		double weaponWeight = weaponAttack * mwa
				* (collisionLocation / 10000.0) * weaponLength;
		return new double[] { mx - weaponWeight * Math.sin(wa),
				my - weaponWeight * Math.cos(wa) };
	}

	public DrawInfo getDrawInfo() {

		DrawInfo r = new DrawInfo();

		int x1 = (int) (x - w * Math.cos(a) / 2 - h * Math.sin(a) / 2);
		int x2 = (int) (x + w * Math.cos(a) / 2 - h * Math.sin(a) / 2);
		int x3 = (int) (x - w * Math.cos(a) / 2 + h * Math.sin(a) / 2);
		int x4 = (int) (x + w * Math.cos(a) / 2 + h * Math.sin(a) / 2);

		int y1 = (int) (y - h * Math.cos(a) / 2 + w * Math.sin(a) / 2);
		int y2 = (int) (y - h * Math.cos(a) / 2 - w * Math.sin(a) / 2);
		int y3 = (int) (y + h * Math.cos(a) / 2 + w * Math.sin(a) / 2);
		int y4 = (int) (y + h * Math.cos(a) / 2 - w * Math.sin(a) / 2);

		int xw1 = (int) (x - weaponLength / 10 * Math.cos(wa));
		int xw2 = (int) (x + weaponLength * Math.cos(wa));
		int yw1 = (int) (y + weaponLength / 10 * Math.sin(wa));
		int yw2 = (int) (y - weaponLength * Math.sin(wa));

		r.coords = new int[] { x1, y1, x2, y2, x3, y3, x4, y4, xw1, yw1, xw2,
				yw2 };
		r.lifeBar = (int) (100.0 * life / maxLife);
		r.stunBar = (int) (100.0 * stun / stunModify / maxLife);

		r.armor = this.armor;
		r.maxArmor = maxArmor;

		r.sleep = sleep;

		return r;
	}

	public class DrawInfo {
		// int x1, x2, x3, x4;
		// int y1, y2, y3, y4;
		// int xw1, xw2, yw1, yw2;
		public int[] coords;
		public int lifeBar; // 0 to 100
		public int stunBar;
		public int[][][] armor; // left,up,right,down; i; amount, flash
		public int maxArmor;
		public int sleep;
	}

}

// switch (strategy) {
// case CENTER:
// goalX = (int) (400 + Math.random() * 50 - 25);
// goalY = (int) (400 + Math.random() * 50 - 25);
// break;
// case SPIN:
// goalX = (int) (x + 200.0 * Math.cos(wa - 125.0 * Math.PI / 180.0));
// goalY = (int) (y - 200.0 * Math.sin(wa - 125.0 * Math.PI / 180.0));
// break;
// case SIMPLE:
// goalX = (int) ((targetX * 1.0 + 1.0 * x + 300.0 * Math.cos(wa
// - 125.0 * Math.PI / 180.0)) / 2.0);
// goalY = (int) ((targetY * 1.0 + 1.0 * y - 300.0 * Math.sin(wa
// - 125.0 * Math.PI / 180.0)) / 2.0);
// break;
// case DEFENSIVE:
// if (Math.pow(targetX + 200 * Math.cos(targetW) - x, 2)
// + Math.pow(targetY - 200 * Math.sin(targetW) - y, 2) > 200 * 200) {
// goalX = (int) ((targetX * 1.0 + 1.0 * x + 300.0 * Math.cos(wa
// - 125.0 * Math.PI / 180.0)) / 2.0);
// goalY = (int) ((targetY * 1.0 + 1.0 * y - 300.0 * Math.sin(wa
// - 125.0 * Math.PI / 180.0)) / 2.0);
// } else {
// goalX = (int) (x + 600 * Math.cos(wa - 125.0 * Math.PI / 180.0));
// goalY = (int) (y - 600 * Math.sin(wa - 125.0 * Math.PI / 180.0));
// }
// break;
// case BLIND:
// goalX = (int) ((targetX + x * 2) / 3);
// goalY = (int) ((targetY + y * 2) / 3);
// break;
// }
