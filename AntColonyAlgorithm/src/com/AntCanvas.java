package com;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

/**
 * 画布
 */
public class AntCanvas extends Canvas {
	private static final long serialVersionUID = 1L;
	// 画布，一切画图操作均由该类完成
	Color obs_color;		// 障碍物颜色
	Color origin_color;		// 窝的颜色
	Color back_color;		// 背景色
	Color end_color;		// 食物点的颜色
	boolean reset;

	public AntCanvas() {
		super();
		back_color = Antcolony.BACK_COLOR;
		setBackground(back_color);
		setForeground(Color.white);
		obs_color = Antcolony.OBS_COLOR;
		origin_color = Antcolony.ORIGIN_COLOR;
		end_color = Antcolony.End_COLOR;
		reset = true;
	}

	public void Clear() {
		// 清空画布
		reset = true;
		repaint();
	}

	public void paint(Graphics g) {
		int i;
		// 重画的时候仅仅画障碍物
		g.setColor(Color.black);
		g.fillRect(0, 0, getSize().width, getSize().height);
		g.setColor(obs_color);
		for (i = 0; i < Antcolony.obsCount; i++) {
			g.fillRect(Antcolony.obsP[i].x, Antcolony.obsP[i].y, 1, 1);
		}

	}

	public void process() {
		// 处理动画的过程
		Graphics g = this.getGraphics();
		g.setColor(end_color);
		for (int j = 0; j < Antcolony.endCount; j++) {
			// 画所有的食物点
			g.fillRect(Antcolony.endPt[j].x, Antcolony.endPt[j].y, 2, 2);
		}
		for (int i = 0; i < Antcolony.antCount; i++) {
			// 每只蚂蚁开始决策，并画蚂蚁
			Antcolony.ants[i].update();
			Antcolony.ants[i].draw(g);
		}
		
		for (int i = 0; i < Antcolony.phe.size(); i++) {
			Pheromone v = (Pheromone) (Antcolony.phe.elementAt(i));
			// Antcolony的drawPhe变量标志是否画信息素
			switch (Antcolony.drawPhe) {
			case (1):
				v.draw(g);
				break;
			case (2):
				if (v.kind == 1)
					v.draw(g);
				break;
			case (3):
				if (v.kind == 0)
					v.draw(g);
				break;
			}
			v.delimit(g);
		}
		g.setColor(origin_color);
		for (int i = 0; i < Antcolony.originCount; i++) {
			// 画所有的窝
			g.fillRect(Antcolony.originPt[i].x, Antcolony.originPt[i].y, 2, 2);
		}

	}
}