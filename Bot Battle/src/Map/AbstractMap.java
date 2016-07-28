package Map;

import java.awt.Image;

import Bots.*;

public abstract class AbstractMap {

	int levelNum;
	public int level; // 0 to levelNum-1
	int timer;
	int timerMax = 50;
	public int w, h;
	public Image backgroundImage;

	AbstractMap() {
		level = -1;
	}

	public abstract PlatedBot[] getBots();

	public boolean nextLevel() {// return false if out of levels, end of game
		restartTimer();
		if (++level < levelNum)
			return true;
		return false; // out of levels
	}

	public void restartTimer() {
		timer = timerMax;
	}

	public int timer() {
		if (timer > 0)
			timer--;
		return (int) (timer * 100.0 / timerMax);
	}

}
