package com;

import java.awt.event.*;
import java.awt.*;

/**
 * Title: ant Description: Copyright: Copyright (c) 2003 Company:
 * agents.yeah.net
 * 
 * @author jake
 * @version 1.0
 */

// 这个文件负责编辑地图
class MapCanvas extends Canvas {
	// 地图的画布类
	MapPad localpad;// 与地图编辑器主控界面交互数据
	Graphics pang;// 需要画图的视图
	int width, height;// 画图的长度和宽度

	// 记录画图时需要的一些点的信息
	Point startPt = new Point(-1, -1);// 这个点为鼠标刚按下时候的坐标
	Point firstPt = new Point(0, 0);// 这个点其实跟上个点一样，只不过是在鼠标拖拉画图的时候起作用
	Point lastPt = new Point(0, 0);// 这点记录在鼠标进行拖拉的时候刚刚拖过的点的位置，为了能够抹去不需要的图

	MapCanvas(MapPad pad) {
		// 初始化一些参数
		localpad = pad;
		width = localpad.local.width;
		height = localpad.local.height;
		this.setBackground(localpad.local.BACK_COLOR);
		this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				this_mouseMoved(e);
			}

			public void mouseDragged(MouseEvent e) {
				this_mouseDragged(e);
			}
		});
		this.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				this_mousePressed(e);
			}

			public void mouseReleased(MouseEvent e) {
				this_mouseReleased(e);
			}

			public void mouseDragged(MouseEvent e) {
				this_mouseDragged(e);
			}

			public void mouseClicked(MouseEvent e) {
				this_mouseClicked(e);
			}

		});

	}

	void this_mousePressed(MouseEvent e) {
		// 鼠标刚按下就记录起始点，并判断这个点是否超界
		startPt = new Point(e.getX(), e.getY());
		int x = startPt.x, y = startPt.y;
		if (startPt.x < 0)
			x = 0;
		if (startPt.y < 0)
			y = 0;
		if (startPt.x >= width)
			x = width - 1;
		if (startPt.y >= height)
			y = height - 1;
		startPt = new Point(x, y);
	}

	void this_mouseDragged(MouseEvent e) {
		// 鼠标拖拉事件，只有当画障碍物、并且是直线、矩形、圆的时候才行否则退出
		int x2 = e.getX(), y2 = e.getY();
		if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height)
			return;

		if (localpad.Kind == 0 && localpad.Shape == 3) {
			// 如果是画障碍物的点，则画这个点，只不过宽度是2
			pang.setColor(localpad.KindColor[localpad.Kind + 1]);
			pang.fillRect(x2, y2, 2, 2);

			localpad.grid[x2][y2] = localpad.Kind + 1;
			if (x2 + 1 >= width)
				x2 = x2 - 1;
			if (y2 + 1 >= height)
				y2 = y2 - 1;
			localpad.grid[x2 + 1][y2] = localpad.Kind + 1;
			localpad.grid[x2][y2 + 1] = localpad.Kind + 1;
			localpad.grid[x2 + 1][y2 + 1] = localpad.Kind + 1;
			return;
		}
		if (localpad.Kind != 0 || localpad.Shape == 3)
			return;
		if (startPt.x < 0 || startPt.y < 0)
			return;
		int x1 = startPt.x, y1 = startPt.y;
		if (x2 < x1) {
			x1 = x2;
			x2 = startPt.x;
		}
		if (y2 < y1) {
			y1 = y2;
			y2 = startPt.y;
		}
		int xx1, yy1, xx, yy;
		xx = lastPt.x;
		yy = lastPt.y;
		xx1 = firstPt.x;
		yy1 = firstPt.y;
		if (xx < xx1) {
			xx1 = xx;
			xx = firstPt.x;
		}
		if (yy < yy1) {
			yy1 = yy;
			yy = firstPt.y;
		}

		// 如果是画直线，先把上一点画过的直线擦除，也就是用背景色重画一遍刚才的直线
		// 如果不是画直线，就清除刚才画过的矩形，方便的事，如果画椭圆，也可以用一个大的矩形套住。
		if (localpad.Shape == 2) {
			pang.setColor(localpad.local.BACK_COLOR);
			pang.drawLine(firstPt.x, firstPt.y, lastPt.x, lastPt.y);
		} else
			pang.clearRect(xx1, yy1, Math.abs(xx - xx1), Math.abs(yy - yy1));

		// 画完了那个矩形以后，需要把原来有的图形信息重新画出来，因为鼠标拖拉的时候并不真正的作图
		for (int i = xx1; i <= xx; i++) {
			for (int j = yy1; j <= yy; j++) {
				if (localpad.grid[i][j] > 0) {
					pang.setColor(localpad.KindColor[localpad.grid[i][j]]);
					if (localpad.grid[i][j] > 1)
						pang.fillRect(i, j, 2, 2);
					pang.fillRect(i, j, 1, 1);
				}
			}
		}
		// 根据形状的不同，选择画图方法
		if (localpad.Shape == 4)
			pang.setColor(localpad.local.BACK_COLOR);
		else
			pang.setColor(localpad.local.OBS_COLOR);
		switch (localpad.Shape) {
		case 0:
			// 画矩形最简单
			pang.fillRect(x1, y1, Math.abs(x1 - x2), Math.abs(y1 - y2));
			break;
		case 1:
			// 画一个椭圆
			pang.fillOval(x1, y1, Math.abs(x1 - x2), Math.abs(y1 - y2));
			break;
		case 2:
			// 画直线
			pang.drawLine(startPt.x, startPt.y, e.getX(), e.getY());
			break;
		case 4:
			// 橡皮，用背景色画矩形
			pang.fillRect(x1, y1, Math.abs(x1 - x2), Math.abs(y1 - y2));
			break;
		}
		// 得到鼠标拖拉起始点和终点的坐标
		lastPt = new Point(e.getX(), e.getY());
		if (lastPt.x >= width)
			lastPt.x = width - 1;
		if (lastPt.x < 0)
			lastPt.x = 0;
		if (lastPt.y >= height)
			lastPt.y = height - 1;
		if (lastPt.y < 0)
			lastPt.y = 0;

		firstPt = new Point(startPt.x, startPt.y);

	}

	void this_mouseReleased(MouseEvent e) {
		// 鼠标释放开始真正的画图，也就是把图形上的元素转换到数组grid中，
		// 这个数组相当于一个位图，记录了每个点的种类（窝、食物点、障碍物）
		if (localpad.Kind != 0 || localpad.Shape == 3)
			return;
		int x2 = e.getX(), y2 = e.getY();
		if (x2 < 0)
			x2 = 0;
		if (x2 >= width)
			x2 = width - 1;
		if (y2 < 0)
			y2 = 0;
		if (y2 >= height)
			y2 = height - 1;
		int x1 = startPt.x, y1 = startPt.y;
		if (x2 < x1) {
			x1 = x2;
			x2 = startPt.x;
		}
		if (y2 < y1) {
			y1 = y2;
			y2 = startPt.y;
		}

		// 选定画图的颜色
		if (localpad.Shape != 4)
			pang.setColor(localpad.local.OBS_COLOR);
		else
			pang.setColor(localpad.local.BACK_COLOR);

		// 根据画图种类不同进行不同的处理
		switch (localpad.Shape % 4) {
		case 0:
			// 是矩形的时候就循环坐标，填充一个矩形
			pang.fillRect(x1, y1, Math.abs(x1 - x2), Math.abs(y1 - y2));
			for (int i = x1; i < x2; i++) {
				for (int j = y1; j < y2; j++) {
					if (localpad.Shape == 0)
						localpad.grid[i][j] = 1;
					if (localpad.Shape == 4)
						localpad.grid[i][j] = 0;
				}
			}
			break;
		case 1:
			// 是椭圆就循环产生一个椭圆
			pang.fillOval(x1, y1, x2 - x1, y2 - y1);
			double a = (double) (x2 - x1) / 2;
			double b = (double) (y2 - y1) / 2;
			for (double x = -a; x <= a; x++) {
				double yy1 = b * Math.sqrt(a * a - x * x) / a;
				for (double y = -yy1; y <= yy1; y++) {
					int xx = (int) (x + (x1 + x2) / 2);
					int yy = (int) (y + (y1 + y2) / 2);
					localpad.grid[xx][yy] = 1;
				}
			}
			break;
		case 2:
			// 是直线的时候，比较麻烦，需要用直线的参数方程x=x1+(x2-x1)*t,y=y1+(y2-y1)*t,t在[0,1]内
			repaint(x1 - 1, y1 - 1, x2 - x1 + 2, y2 - y1 + 2);
			x1 = startPt.x;
			y1 = startPt.y;
			x2 = e.getX();
			y2 = e.getY();
			if (x2 < 0)
				x2 = 0;
			if (x2 >= width)
				x2 = width - 1;
			if (y2 < 0)
				y2 = 0;
			if (y2 >= height)
				y2 = height - 1;
			double step;
			if (x2 != x1 || y2 != y1) {
				if (Math.abs(x2 - x1) > Math.abs(y2 - y1))
					step = (double) (1 / (double) (Math.abs(x2 - x1)));
				else
					step = (double) (1 / (double) (Math.abs(y2 - y1)));
				for (double t = 0; t <= 1; t += step) {
					int x = (int) ((x2 - x1) * t + x1);
					int y = (int) ((y2 - y1) * t + y1);
					// 为了直线不产生误差，所以画宽度为2的线
					pang.fillRect(x, y, 1, 1);
					pang.fillRect(x + 1, y, 1, 1);
					pang.fillRect(x, y + 1, 1, 1);
					localpad.grid[x][y] = 1;
					localpad.grid[x + 1][y] = 1;
					pang.fillRect(x, y + 1, 1, 1);
				}
			}
		}
		startPt = new Point(-1, -1);

	}

	void this_mouseMoved(MouseEvent e) {
		// 在鼠标移动的时候，如果当前模式为橡皮，则鼠标移动到的点会被一个小矩形遮挡
		if (localpad.Shape == 4) {
			for (int x = lastPt.x - 2; x < lastPt.x + 2; x++) {
				for (int y = lastPt.y - 2; y < lastPt.y + 2; y++) {
					if (x >= 0 && x < width && y >= 0 && y < height) {
						if (localpad.grid[x][y] > 0) {
							pang.setColor(localpad.KindColor[localpad.grid[x][y]]);
							if (localpad.grid[x][y] > 1)
								pang.fillRect(x, y, 2, 2);
							pang.fillRect(x, y, 1, 1);
						}
					}
				}
			}
			pang.setColor(localpad.local.BACK_COLOR);
			pang.fillRect(e.getX() - 2, e.getY() - 2, 4, 4);
			lastPt = new Point(e.getX(), e.getY());
			if (lastPt.x >= width)
				lastPt.x = width - 1;
			if (lastPt.x < 0)
				lastPt.x = 0;
			if (lastPt.y >= height)
				lastPt.y = height - 1;
			if (lastPt.y < 0)
				lastPt.y = 0;

		}
	}

	void this_mouseClicked(MouseEvent e) {
		// 鼠标单击事件，处理画点的事件，每个点的宽度都是2
		if (e.getX() < 0 || e.getX() > width || e.getY() < 0 || e.getY() > width)
			return;
		if (localpad.Kind == 1 || localpad.Kind == 2 || (localpad.Kind == 0 && localpad.Shape == 3)) {
			pang.setColor(localpad.KindColor[localpad.Kind + 1]);
			int x = e.getX();
			int y = e.getY();
			pang.fillRect(x, y, 2, 2);
			localpad.grid[x][y] = localpad.Kind + 1;
			if (localpad.Kind == 0) {
				if (x + 1 >= width)
					x = x - 1;
				if (y + 1 >= height)
					y = y - 1;
				localpad.grid[x + 1][y] = localpad.Kind + 1;
				localpad.grid[x][y + 1] = localpad.Kind + 1;
				localpad.grid[x + 1][y + 1] = localpad.Kind + 1;
			}
			return;
		}
		// 橡皮擦除所进行的动作
		if (localpad.Shape == 4) {
			pang.setColor(localpad.KindColor[0]);
			int x1 = e.getX() - 2;
			int y1 = e.getY() - 2;
			int x2 = x1 + 4;
			int y2 = y1 + 4;
			pang.fillRect(x1, y1, 4, 4);
			if (x1 < 0)
				x1 = 0;
			if (x2 >= width)
				x2 = width - 1;
			if (y1 < 0)
				y1 = 0;
			if (y2 >= height)
				y2 = height - 1;
			for (int x = x1; x < x2; x++) {
				for (int y = y1; y < y2; y++) {
					localpad.grid[x][y] = 0;
				}
			}
		}
	}

	public void paint(Graphics g) {
		// 重画画布的时候，把数组中的信息再提取出来画到屏幕上
		g.clearRect(0, 0, width, height);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (localpad.grid[i][j] != 0) {
					g.setColor(localpad.KindColor[localpad.grid[i][j]]);
					if (localpad.grid[i][j] > 1)
						g.fillRect(i, j, 2, 2);
					else
						g.fillRect(i, j, 1, 1);
				}
			}
		}

	}
}


public class MapPad extends Frame {
	// 整个地图编辑器控制面板类
	Panel pan = new Panel();
	MapCanvas Canvas;
	Choice shapeCh = new Choice();
	Choice KindCh = new Choice();
	Choice MapCh = new Choice();
	Button btnConfirm = new Button("确定");
	int Shape = 0;						// 这是画图的形状：0矩形，1椭圆，2直线，3点，4橡皮
	int Kind = 0;						// 画图的种类：0障碍物，1窝点，2是食物点
	Color KindColor[] = new Color[4];	// 画图种类的颜色信息，0为背景色，1为障碍物颜色，2为窝点颜色，3为食物点颜色
	int grid[][];						// 所有的图像元素信息存在这里
	Antcolony local;					// 主程序的指针

	public MapPad(Antcolony colony) {
		local = colony;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		setLayout(new BorderLayout());
		// 数组初始化
		grid = new int[local.width][local.height];
		for (int i = 0; i < local.width; i++) {
			for (int j = 0; j < local.height; j++) {
				grid[i][j] = local.obsGrid[i][j] + 1;
				if (grid[i][j] > 1)
					grid[i][j] = 1;
			}
		}
		for (int i = 0; i < local.endCount; i++) {
			grid[local.endPt[i].x][local.endPt[i].y] = 3;
		}
		for (int i = 0; i < local.originCount; i++) {
			grid[local.originPt[i].x][local.originPt[i].y] = 2;
		}

		// 对颜色数组负值
		KindColor[0] = local.BACK_COLOR;
		KindColor[1] = local.OBS_COLOR;
		KindColor[2] = local.ORIGIN_COLOR;
		KindColor[3] = local.End_COLOR;

		// 画控件
		this.setBackground(Color.black);
		Canvas = new MapCanvas(this);
		pan.setBackground(Color.lightGray);
		this.add("South", pan);
		this.add("Center", Canvas);
		pan.add(new Label("形状:"));
		pan.add(shapeCh);
		pan.add(KindCh);
		pan.add(btnConfirm);
		pan.add(MapCh);
		shapeCh.addItem("矩形");
		shapeCh.addItem("圆");
		shapeCh.addItem("直线");
		shapeCh.addItem("点");
		shapeCh.addItem("橡皮");
		KindCh.addItem("障碍物");
		KindCh.addItem("窝");
		KindCh.addItem("食物");
		MapCh.addItem("加载地图");
		MapCh.addItem("山区地形");
		MapCh.addItem("分形叶");
		MapCh.addItem("迷宫");
		// 事件捕获器，jb自己加上去的，不关我的事儿。
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				this_windowClosing(e);
			}

			public void windowOpened(WindowEvent e) {
				this_windowOpened(e);
			}
		});
	}

	void this_windowClosing(WindowEvent e) {
		this.setVisible(false);
		this.dispose();
	}

	public boolean action(Event evt, Object o) {
		// 选择画图种类，以及形状，这两个选择之间有关联
		if (evt.target == shapeCh) {
			Shape = shapeCh.getSelectedIndex();
			if (Shape != 3) {
				KindCh.select(0);
				Kind = 0;
			}
			return true;
		}
		if (evt.target == KindCh) {
			Kind = KindCh.getSelectedIndex();
			if (Kind != 0) {
				shapeCh.select(3);
				Shape = 3;
			} else {
				shapeCh.select(0);
				Shape = 0;
			}
			return true;
		}
		if (evt.target == MapCh) {
			int index = MapCh.getSelectedIndex();
			if (index <= 0)
				return true;
			// 清空数组：
			for (int i = 0; i < local.width; i++) {
				for (int j = 0; j < local.height; j++) {
					grid[i][j] = 0;
				}
			}
			// 加载地图
			Maps.loadMap(grid, index - 1);
			Canvas.repaint();
			return true;
		}
		if (evt.target == btnConfirm) {
			// 如果选择了确定，那么先初始化地图，再初始化所有蚂蚁，调用主程序类
			local.reinit_map(grid);
			local.reinit();
			this.setVisible(false);
			this.dispose();
			return true;
		}
		return false;

	}

	void this_windowOpened(WindowEvent e) {
		Canvas.pang = Canvas.getGraphics();
	}

}