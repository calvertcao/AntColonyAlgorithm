/**
 * Title: ant Description: Copyright: Copyright (c) 2003 Company:
 * agents.yeah.net
 * 
 * @author jake
 * @version 1.0
 */
package com;
import java.awt.*;


/**
 * 信息素
 */
public class Pheromone {
	int capability;		// 信息素的含量
	int id;				// 信息素的标识
	int x;				// x坐标
	int y;				// y坐标
	int delimiter;		// 在信息素减少的时候消散的数值
	int ant_id;			// 记录改信息素是由哪个蚂蚁释放的
	int kind;			// 信息素的种类，0为对窝的信息素（即刚从窝中爬出来的蚂蚁释放的），1为食物的
	int origin_capcity;	// 信息素的原始大小，因为在环境中（食物和窝的周围都事先释放了一定量的信息素
	Antcolony local;	// 当前主类的指针

	public Pheromone(int x1, int y1, int idd, int deli, int ant, Antcolony colony, int oc, int k) {
		x = x1;
		y = y1;
		id = idd;
		delimiter = deli;
		ant_id = ant;
		local = colony;
		origin_capcity = oc;
		capability = origin_capcity;
		kind = k;
	}

	public void add(int cap) {
		// 增加该点的信息素
		capability += cap;
	}

	public void delimit(Graphics g) {
		// 消散信息素
		if (capability > 0) {
			capability -= delimiter;
			local.pheromoneGrid[kind][x][y] -= delimiter;
		} else {
			// 如果信息素见到0了，从环境信息素数组中注销
			local.pheromoneGrid[kind][x][y] = 0;
			local.phe.removeElement(this);
			// 把信息素从屏幕上抹去
			g.setColor(local.BACK_COLOR);
			g.fillOval(x, y, 1, 1);
		}
	}

	public void draw(Graphics g) {
		// 画信息素
		Color color;
		int cap = (int) (255 * (double) (200 * capability) / (double) (local.maxPheromone));
		if (cap >= 255)
			cap = 255;
		if (cap <= 2) {
			color = local.BACK_COLOR;
		} else {
			if (kind == 0) {
				color = new Color(cap, (int) (cap / 2), 0);
			} else {
				color = new Color(0, (int) (cap / 2), cap);
			}
		}
		g.setColor(color);
		g.fillOval(x, y, 1, 1);
	}
}