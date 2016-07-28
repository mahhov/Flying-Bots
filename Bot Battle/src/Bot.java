
public class Bot {
	int x, y, w, h;
	double a, ma, wa, mwa, mx, my;
	double maxSpeed, maxWSpeed;
	int weaponLength;
	int weaponAttack;
	int life, maxLife;
	int stun;

	Bot(int x, int y) {
		this.x = x;
		this.y = y;
		a = 0;
		mx = 0;
		my = 0;
		ma = 0;
		w = 40;
		h = 70;
		wa = -Math.PI / 2;
		stun = 0;
		weaponAttack = 3;

		maxLife = 300;
		life = maxLife;
		maxSpeed = 7;
		weaponLength = 120;
		maxWSpeed = 90 * Math.PI / 180;
	}

	public void move(int towardsX, int towardsY) {
		double oldwx = x + weaponLength * Math.cos(wa);
		double oldwy = y - weaponLength * Math.sin(wa);

		if (stun == 0 & life > 0) {
			mx += (towardsX - x) / 200.0;
			my += (towardsY - y) / 200.0;
		} else {
			my += 0.3;
			stun--;
		}
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
		if (x < 0) {
			mx *= -0.2;
			x = 0;
		}
		if (x > 800) {
			mx *= -0.2;
			x = 800;
		}
		if (y < 0) {
			my *= -0.2;
			y = 0;
		}
		if (y > 800) {
			my *= -.2;
			y = 800;
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

	public boolean damage(boolean horizontal, double momentum) {
		// body to body
		// returns true if died
		if (life > 0) {
			int damage = 0;
			if (horizontal) {
				damage = (int) Math.abs(momentum - mx);
				mx = momentum;
			} else {
				damage = (int) Math.abs(momentum - my);
				my = momentum;
			}
			stun += damage * 2;
			life -= damage;
			if (life < 0) {
				return true;
			}
		}
		return false;
	}

	public boolean damage(double[] momentum) {
		// weapon to body
		// returns true if died
		if (life > 0) {
			int amount = (int) Math.sqrt(Math.pow(momentum[0], 2)
					+ Math.pow(momentum[1], 2));
			mx += momentum[0];
			my += momentum[1];
			stun += amount * 2;
			life -= amount;
			if (life < 0) {
				life = 0;System.out.println("d");
				return true;
			}
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

	public int[] getDrawInfo() {

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

		return new int[] { x1, y1, x2, y2, x3, y3, x4, y4, xw1, yw1, xw2, yw2,
				(int) (100.0 * life / maxLife),
				(int) (100.0 * stun / 2 / maxLife) };
	}

}
