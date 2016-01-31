package com;

import java.awt.*;
import java.util.Vector;

import javax.swing.JOptionPane;

public class Ant {
	Point nowPt; // 当前点坐标
	int vr; // 速度，每次蚂蚁能走动的最大长度
	int id; // 标识
	Point lastPt; // 上一点坐标
	Color color; // 蚂蚁的颜色
	Color backColor; // 背景的颜色
	int height, width; // 世界的尺寸
	int phe; // 每次释放信息素的数值
	Vector<Point> historyPoint; // 记录一次觅食过程历史上的所有点
	double mainDirect; // 主方向
	Point foodPt; // 记录的食物点，是否找到时候判断用
	Point originPt; // 窝的坐标
	Point aimPt; // 目标点的位置（是窝或者食物的坐标）
	Point startPt; // 起始点的位置（是窝或者食物的坐标）
	int foundTimes; // 找到食物或者窝的次数（如果为偶数，则正在寻找食物，如果是奇数，则正在寻找窝）。
	int maxPheromone; // 最大能够释放的信息素
	int pheromoneCount; // 当前还拥有的信息素的总量
	boolean judged = false; // 判断寻找目标点的工作是否已经进行了
	double mistake; // 犯错误的概率
	int memory; // 记忆走过点的数目
	double countDistance; // 走过的总路程,为单程的路程，也就是说找到食物或者窝就从新计数了。
	double minDistance; // 当前这只蚂蚁再没次往返的时候的最小总距离
	Antcolony localColony; // 主程序的引用

	public Ant(Point nowpt, int vr, int idd, Antcolony colony, Color c,
			double mist, int mem) {
		nowPt = new Point(nowpt.x, nowpt.y);
		originPt = new Point(nowpt.x, nowpt.y);
		foodPt = new Point(nowpt.x, nowpt.y);
		startPt = new Point(nowpt);
		aimPt = new Point(nowpt);
		lastPt = nowPt;
		this.vr = vr;
		id = idd;
		color = c;
		backColor = Antcolony.BACK_COLOR;
		height = Antcolony.height;
		width = Antcolony.width;
		localColony = colony;
		phe = 200;
		mistake = mist;
		historyPoint = new Vector<Point>();
		mainDirect = -1;
		foundTimes = 0;
		maxPheromone = Antcolony.maxPheromone;
		pheromoneCount = maxPheromone;
		memory = mem;
		countDistance = 0;
		minDistance = Double.MAX_VALUE;
	}

	public void init() {
		nowPt = new Point(originPt);
		lastPt = new Point(originPt);
		foodPt = new Point(originPt);
		aimPt = new Point(originPt);
		startPt = new Point(originPt);
		historyPoint.removeAllElements();
		mainDirect = -1;
		foundTimes = 0;
		pheromoneCount = maxPheromone;
		countDistance = 0;
		minDistance = Double.MAX_VALUE;
	}

	public void draw(Graphics g) {
		g.setColor(backColor);
		g.fillOval((int) lastPt.x, (int) lastPt.y, 1, 1);
		g.setColor(color);
		g.fillOval((int) nowPt.x, (int) nowPt.y, 1, 1);
	}

	public boolean Is_End() {
		if (judged == false && judgeEnd()) {
			judged = true;
			return true;
		}
		judged = false;
		return false;
	}

	public boolean Is_Mistake(int value) {
		return value != 0 && Math.random() < mistake;
	}

	public boolean Is_Circle(int xx, int yy) {
		int size = historyPoint.size();
		int minsize = memory;
		if (size < memory)
			minsize = size;
		for (int i = size - 1; i >= size - minsize; i--) {
			Point pt = (Point) (historyPoint.elementAt(i));
			if (pt.x == xx && pt.y == yy) {
				return true;
			}
		}
		return false;
	}

	public Point Search_Round(int kind, int here, int deltx, int delty) {
		int maxphe = here;
		for (int x = -vr; x <= vr; x++) {
			for (int y = -vr; y <= vr; y++) {
				int xx = (nowPt.x + x + width) % width;
				int yy = (nowPt.y + y + height) % height;
				if (x != 0 || y != 0) {
					int phe = Antcolony.pheromoneGrid[1 - kind][xx][yy];
					if (maxphe < phe && !Is_Mistake(here) && !Is_Circle(xx, yy)) {
						maxphe = phe;
						deltx = x;
						delty = y;
					}
				}
			}
		}
		return evadeObs(deltx, delty);
	}

	public boolean Is_same(Point a, Point b) {
		return a.x == b.x && a.y == b.y;
	}

	public void Cal_Way() {
		countDistance += distance(lastPt, nowPt);
		lastPt = nowPt;
		historyPoint.insertElementAt(lastPt, historyPoint.size());
		if (historyPoint.size() > memory)
			historyPoint.removeElementAt(0);
	}

	public void Cal_Next(double direct) {
		int deltx = 0, delty = 0;
		deltx = (int) (vr * Math.cos(direct));
		delty = (int) (vr * Math.sin(direct));
		Point pt = Search_Round(foundTimes % 2,
				Antcolony.pheromoneGrid[1 - foundTimes % 2][nowPt.x][nowPt.y],
				deltx, delty);
		if (Is_same(pt, nowPt))
			pt = evadeObs(deltx, delty);
		Cal_Way();
		nowPt = new Point(pt.x, pt.y);
	}

	public void update() {
		if (Is_End())
			return;
		Cal_Next(selectDirect());
		scatter();
	}

	public int Max_abs(int a, int b) {
		if (Math.abs(a) > Math.abs(b))
			return a;
		else
			return b;
	}

	private Point evadeObs(int deltx, int delty) {
		Point pt = new Point(0, 0);
		int x, y;
		int delt = Max_abs(deltx, delty);
		for (double t = 0; t <= 1; t += 1 / (double) (Math.abs(delt))) {
			x = (int) (deltx * t + nowPt.x);
			y = (int) (delty * t + nowPt.y);
			x = (x + width) % width;
			y = (y + height) % height;
			if (Antcolony.obsGrid[x][y] >= 0) {
				deltx = pt.x - nowPt.x;
				delty = pt.y - nowPt.y;
				mainDirect = (4 * Math.PI * (Math.random() - 0.5) + 2 * Math.PI)
						% (2 * Math.PI);
				break;
			}
			pt = new Point(x, y);
		}
		x = (nowPt.x + deltx + width) % width;
		y = (nowPt.y + delty + height) % height;
		return new Point(x, y);
	}

	public boolean Is_Oldway() {
		int size = historyPoint.size();
		if (size > 4) {
			for (int j = size - 4; j < size - 1; j++) {
				Point pt = (Point) (historyPoint.elementAt(j));
				if (pt.x == lastPt.x && pt.y == lastPt.y)
					return true;
			}
		}
		return false;
	}

	private void scatter() {
		if (pheromoneCount <= 0)
			return;
		int kind = foundTimes % 2;
		int Phec = Antcolony.pheromoneGrid[kind][lastPt.x][lastPt.y];
		boolean ofound = false;
		if (Phec != 0) {
			for (int i = 0; i < Antcolony.phe.size(); i++) {
				Pheromone ph = (Pheromone) (Antcolony.phe.elementAt(i));
				if (lastPt.x == ph.x && lastPt.y == ph.y && ph.kind == kind) {
					ofound = true;
					if (!Is_Oldway()) {
						ph.add(phe);
						Antcolony.pheromoneGrid[kind][lastPt.x][lastPt.y] += phe;
						pheromoneCount -= phe;
					}
					break;
				}
			}
		}
		if (Phec == 0 || !ofound) {
			Pheromone ph = new Pheromone(lastPt.x, lastPt.y,
					Antcolony.phe.size(), Antcolony.delimiter, id, localColony,
					Phec, kind);
			ph.add(phe);
			Antcolony.pheromoneGrid[kind][lastPt.x][lastPt.y] += phe;
			Antcolony.phe.addElement(ph);
			pheromoneCount -= phe;
		}
		phe = (int) (0.005 * pheromoneCount);
		if (phe <= 10)
			phe = 10;
	}

	public double Reverse(double d) {
		return (Math.PI + d) % (2 * Math.PI);
	}

	public void Cal_Finded() {
		pheromoneCount = maxPheromone;
		historyPoint.removeAllElements();
		mainDirect = Reverse(mainDirect);
		foundTimes++;
	}

	public boolean Find_Food() {
		for (int i = 0; i < Antcolony.endCount; i++) {
			if (distance(nowPt, Antcolony.endPt[i]) <= vr) {
				lastPt = new Point(nowPt.x, nowPt.y);
				nowPt.x = Antcolony.endPt[i].x;
				nowPt.y = Antcolony.endPt[i].y;
				countDistance += distance(lastPt, nowPt);
				if (countDistance < minDistance || minDistance < 0)
					minDistance = countDistance;
				countDistance = 0;
				aimPt = new Point(originPt);
				startPt = new Point(nowPt);
				foodPt = new Point(nowPt.x, nowPt.y);
				Cal_Finded();
				return true;
			}
		}
		return false;
	}

	public boolean Find_Home() {
		if (distance(nowPt, aimPt) <= vr) {
			lastPt = new Point(nowPt.x, nowPt.y);
			nowPt.x = aimPt.x;
			nowPt.y = aimPt.y;
			countDistance += distance(lastPt, nowPt);
			if (countDistance < minDistance || minDistance < 0)
				minDistance = countDistance;
			countDistance = 0;
			aimPt = new Point(foodPt);
			startPt = new Point(originPt);
			Cal_Finded();
			return true;
		}
		return false;
	}

	private boolean judgeEnd() {
		int kind = foundTimes % 2;
		if (kind == 0)
			return Find_Food();
		else
			return Find_Home();
	}

	private double selectDirect() {
		double direct, e = 0;
		if (mainDirect < 0) {
			e = 2 * Math.PI * Math.random();
			mainDirect = e;
		}
		direct = mainDirect;
		double re = Math.random();
		double re1 = Math.random();
		direct += Math.PI * (re * re - re1 * re1) / 2;
		if (re < 0.02) {
			int size = (int) (re1 * memory) + 1;
			if (historyPoint.size() > size) {
				Point pt = (Point) (historyPoint.elementAt(historyPoint.size()
						- size));
				if (pt.x != nowPt.x || pt.y != nowPt.y) {
					mainDirect = getDirection(pt, nowPt);
				}
			}
		}
		return direct;
	}

	private double getDirection(Point pt1, Point pt2) {
		double e;
		int deltx1 = pt2.x - pt1.x;
		int deltx2;
		if (pt2.x > pt1.x)
			deltx2 = pt2.x - pt1.x - width;
		else
			deltx2 = pt2.x + width - pt1.x;
		int delty1 = pt2.y - pt1.y;
		int delty2;
		if (pt2.y > pt1.y)
			delty2 = pt2.y - pt1.y - height;
		else
			delty2 = pt2.y + height - pt1.y;
		int deltx = deltx1, delty = delty1;
		if (deltx == 0 && delty == 0)
			return -1;
		if (Math.abs(deltx2) < Math.abs(deltx1)) {
			deltx = deltx2;
		}
		if (Math.abs(delty2) < Math.abs(delty1)) {
			delty = delty2;
		}
		if (deltx != 0) {
			e = Math.atan((double) (delty) / (double) (deltx));
			if (deltx < 0) {
				if (e < 0)
					e = e - Math.PI;
				else
					e = e + Math.PI;
			}
		} else {
			if (delty > 0)
				e = Math.PI / 2;
			else
				e = -Math.PI / 2;
		}
		e = (e + Math.PI * 2) % (2 * Math.PI);
		return e;
	}

	private double distance(Point pt1, Point pt2) {
		int dx1 = pt1.x - pt2.x;
		int dx2;
		int dx, dy;
		if (pt1.x > pt2.x)
			dx2 = pt1.x + width - pt2.x;
		else
			dx2 = pt2.x + width - pt1.x;
		int dy1 = pt1.y - pt2.y;
		int dy2;
		if (pt1.y > pt2.y)
			dy2 = pt1.y + height - pt2.y;
		dy2 = pt2.y + height - pt1.y;
		if (Math.abs(dx1) < Math.abs(dx2))
			dx = dx1;
		else
			dx = dx2;
		if (Math.abs(dy1) < Math.abs(dy2))
			dy = dy1;
		else
			dy = dy2;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public void clone(Ant ant1) {
		nowPt = new Point(ant1.nowPt);
		originPt = new Point(ant1.originPt);
		foodPt = new Point(ant1.foodPt);
		startPt = new Point(ant1.startPt);
		aimPt = new Point(ant1.aimPt);
		lastPt = new Point(ant1.lastPt);
		vr = ant1.vr;
		id = ant1.id;
		color = ant1.color;
		backColor = ant1.backColor;
		height = ant1.height;
		width = ant1.width;
		localColony = ant1.localColony;
		phe = ant1.phe;
		mistake = ant1.mistake;
		historyPoint = ant1.historyPoint;
		mainDirect = ant1.mainDirect;
		foundTimes = ant1.foundTimes;
		maxPheromone = ant1.maxPheromone;
		pheromoneCount = ant1.pheromoneCount;
		memory = ant1.memory;
		countDistance = ant1.countDistance;
		minDistance = ant1.minDistance;
	}

}
