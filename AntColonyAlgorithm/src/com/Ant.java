package com;

import java.awt.*;
import java.util.Random;
import java.util.Vector;
import java.lang.System;

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
	int judged = 0; // 判断寻找目标点的工作是否已经进行了
	double mistake; // 犯错误的概率
	int memory; // 记忆走过点的数目
	double countDistance; // 走过的总路程,为单程的路程，也就是说找到食物或者窝就从新计数了。
	double minDistance; // 当前这只蚂蚁再没次往返的时候的最小总距离
	Antcolony localColony; // 主程序的引用

	/**
	 * 蚂蚁构造函数
	 * 
	 * @param nowpt
	 *            现在所处的位置
	 * @param vr
	 *            速度
	 * @param idd
	 *            该只蚂蚁的标识
	 * @param colony
	 *            主程序引用
	 * @param c
	 *            颜色
	 * @param mist
	 *            犯错误的概率
	 * @param mem
	 *            可记忆走过的点数
	 */

	public Ant(Point nowpt, int vr, int idd, Antcolony colony, Color c,
			double mist, int mem) {
		nowPt = new Point(nowpt.x, nowpt.y);
		originPt = new Point(nowpt.x, nowpt.y);
		foodPt = new Point(nowpt.x, nowpt.y);
		startPt = new Point(nowpt);
		aimPt = new Point(nowpt);
		lastPt = nowPt;
		this.vr = 5;
		id = idd;
		color = c;
		backColor = Antcolony.BACK_COLOR;
		height = Antcolony.height;
		width = Antcolony.width;
		localColony = colony;
		phe = 2000;
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

	/**
	 * 初始化过程
	 */
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

	/**
	 * 把该只蚂蚁画在地图面板上
	 * 
	 * @param g
	 *            抽象的画笔
	 */
	public void draw(Graphics g) {
		// 把蚂蚁在屏幕上画出来，先擦除上次画的点，然后再画蚂蚁现在的点。
		g.setColor(backColor);
		g.fillOval((int) lastPt.x, (int) lastPt.y, 1, 1);
		g.setColor(color);
		g.fillOval((int) nowPt.x, (int) nowPt.y, 1, 1);
	}

	/**
	 * 这个函数是蚂蚁进行决策的主程序，首先判断蚂蚁是否已经找到了目标点 （目标点在没找到食物的时候是食物点，找到以后是自己的窝）
	 * 然后计算蚂蚁的主方向，也就是让蚂蚁的爬动有一个惯性，当没有信息素作指导的时候蚂蚁按照主方向运动
	 * 开始搜索自己周围的空间信息，包括有多少信息素，是否有障碍物。最后根据信息素的大小决定移动到那个点
	 * 根据决策的目标进行真实的移动，其中包括了避障的行为，洒下信息素。
	 */
	public void update() {
		// TODO:更新蚂蚁位置以及环境状态
		// 根据需要调用其他几个方法，如有需要，可以更改该类中的私有函数。
		if (historyPoint.size() != 0) {
			int px = historyPoint.lastElement().x;
			int py = historyPoint.lastElement().y;
			if (px == nowPt.x && py == nowPt.y) {
				lastPt = nowPt;
				if (nowPt.x > 250)
					nowPt.x -= 5;
				else
					nowPt.x += 5;
				if (nowPt.y < 50)
					nowPt.y += 5;
				else
					nowPt.y -= 5;

				return;
			}
		}
		if (judged < 2) {
			if (judgeEnd()) {
				judged++;
				return;
			}
		}
		judged = 0;
		double direct = selectDirect();

		int deltx = 0, delty = 0;
		deltx = (int) (vr * Math.cos(direct));
		delty = (int) (vr * Math.sin(direct));

		int kind = foundTimes % 2;
		int here = Antcolony.pheromoneGrid[1 - kind][nowPt.x][nowPt.y];

		int maxphe = here;

		int deltx1, delty1;
		deltx1 = deltx;
		delty1 = delty;

		for (int x = -vr; x <= vr; x++) {
			for (int y = -vr; y <= vr; y++) {
				int xx = nowPt.x + x;
				int yy = nowPt.y + y;
				if (xx >= width || yy >= height || xx <= 0 || yy <= 0)
					continue;
				if (x != 0 || y != 0) {
					int phe = Antcolony.pheromoneGrid[1 - kind][xx][yy];

					if (maxphe < phe) {

						double ra = Math.random();
						if (here == 0 || ra > mistake) {
							boolean found = false;
							int size = historyPoint.size();
							int minsize = memory;
							if (size < memory)
								minsize = size;
							for (int i = size - 1; i >= size - minsize; i--) {
								Point pt = (Point) (historyPoint.elementAt(i));
								if (pt.x == xx && pt.y == yy) {
									found = true;
									break;
								}
							}
							if (!found) {
								maxphe = Antcolony.pheromoneGrid[1 - kind][xx][yy];
								deltx = x;
								delty = y;
							}
						}
					}
				}
			}
		}
		Point pt;
		pt = evadeObs(deltx, delty);

		if (pt.x == nowPt.x && pt.y == nowPt.y) {
			pt = evadeObs(deltx1, delty1);
		}
		boolean flag = true;
		for (int i = 0; i < historyPoint.size(); i++)
			if (pt.x == historyPoint.get(i).x && pt.y == historyPoint.get(i).y) {
				mainDirect = Math.random() * 2 * Math.PI;
				int dx = (int) (Math.sin(mainDirect) * vr);
				int dy = (int) (Math.cos(mainDirect) * vr);
				// System.out.println(dx+" "+dy);
				pt = evadeObs(dx, dy);
				flag = false;
				break;
			}
		if (flag)
			scatter();

		countDistance += distance(lastPt, nowPt);

		lastPt = new Point(nowPt.x, nowPt.y);

		historyPoint.insertElementAt(lastPt, historyPoint.size());
		if (historyPoint.size() > memory) {
			historyPoint.removeElementAt(0);
		}
		nowPt = new Point(pt.x, pt.y);
	}

	/**
	 * 这个函数根据决策的位移值进行避障的判断，算出真实可以移动到的点
	 * 要移动到的目标点是(nowPt+delt),当前点是nowPt，那么搜索nowPt到(nowPt+delt)，这条直线上的所有点，看有没有障碍物！
	 * 
	 * @param deltx
	 *            目标点与当前点的x偏移
	 * @param delty
	 *            目标点与当前点的y偏移
	 * @return 可移动到的点。
	 */
	/*
	 * private Point evadeObs(int deltx, int delty) {
	 * 
	 * // 根据直线的参数方程： // x=p1x+(p2x-p1x)*t,y=p1y+(p2y-p1y)*t; //
	 * 其中t是参数，取值[0,1]，步长为abs(max{p2x-p1x,p2y-p1y})， //
	 * p1,p2在这里分别是nowPt和nowPt+delt
	 * 
	 * // TODO: 完成避障工作 // 提醒：如果Antcolony.obsGrid[x][y] >= 0，表示在x、y位置存在障碍物 Point
	 * to = new Point(nowPt.x + deltx, nowPt.y + delty); int k =
	 * Math.max(Math.abs(deltx), Math.abs(delty)); for (int i = 0; i <= k; i++)
	 * { int dx = i * deltx / k; int dy = i * delty / k; if
	 * (Antcolony.obsGrid[nowPt.x + dx][nowPt.y + dy] >= 0) { dx = (i - 1) *
	 * deltx / k; dy = (i - 1) * delty / k; mainDirect = reverse(mainDirect);
	 * return new Point(nowPt.x + dx, nowPt.y + dy); } } return to; }
	 */

	private Point evadeObs(int deltx, int delty) {

		// 根据直线的参数方程：
		// x=p1x+(p2x-p1x)*t,y=p1y+(p2y-p1y)*t;
		// 其中t是参数，取值[0,1]，步长为abs(max{p2x-p1x,p2y-p1y})，
		// p1,p2在这里分别是nowPt和nowPt+delt

		// TODO: 完成避障工作
		// 提醒：如果Antcolony.obsGrid[x][y] >= 0，表示在x、y位置存在障碍物
		Point to = new Point(nowPt.x + deltx, nowPt.y + delty);
		int k = Math.max(Math.abs(deltx), Math.abs(delty));
		if (k != 0)
			for (int i = 0; i <= k; i++) {
				int dx = i * deltx / k;
				int dy = i * delty / k;
				if (Antcolony.obsGrid[nowPt.x + dx][nowPt.y + dy] >= 0) {
					dx = (i - 1) * deltx / k;
					dy = (i - 1) * delty / k;
					double re = Math.random();
					double re1 = Math.random();
					mainDirect += Math.PI * (re * re - re1 * re1) / 2 + Math.PI;
					if (mainDirect >= Math.PI * 2)
						mainDirect -= Math.PI * 2;
					return new Point(nowPt.x + dx, nowPt.y + dy);
				}
			}
		return to;
	}

	/**
	 * 释放信息素 释放信息素函数，每只蚂蚁有一个信息素的最大含量maxPheromone，
	 * 并且，每次蚂蚁都释放phe单位信息素，并且从总量pheromoneCount中减去Phe，直到用完所有的信息素
	 */
	private void scatter() {
		if (pheromoneCount <= 0)
			return;
		// 决定释放信息素的种类
		int kind = foundTimes % 2;

		// 获得当前点环境已有信息素的值
		int Phec = Antcolony.pheromoneGrid[kind][lastPt.x][lastPt.y];
		if (Phec != 0) {
			// 如果当前点已经有信息素了
			// TODO: 完成蚂蚁释放信息素过程
			// 提醒：蚂蚁在达到家或者窝时，将可释放的信息素设置为最大，如果总释放量超过了该值，则不再释放
			// 在Antcolony.phe找查找该点的信息素，并更新。
			for (int i = 0; i < Antcolony.phe.size(); i++) {
				Pheromone ph = (Pheromone) (Antcolony.phe.elementAt(i));
				if (lastPt.x == ph.x && lastPt.y == ph.y && ph.kind == kind) {
					ph.ant_id = id;
					if (pheromoneCount < phe) {
						Antcolony.pheromoneGrid[kind][lastPt.x][lastPt.y] += pheromoneCount;
						ph.capability += pheromoneCount;
						pheromoneCount = 0;
					} else {
						Antcolony.pheromoneGrid[kind][lastPt.x][lastPt.y] += phe;
						ph.capability += phe;
						pheromoneCount -= phe;
					}
					Antcolony.phe.addElement(ph);
					Antcolony.phe.removeElementAt(i);
					break;
				}
			}
		} else {
			if (pheromoneCount < phe) {
				Antcolony.pheromoneGrid[kind][lastPt.x][lastPt.y] = pheromoneCount;
				Pheromone ph = new Pheromone(lastPt.x, lastPt.y, 100, 1, id,
						null, 0, kind);
				ph.capability = pheromoneCount;
				pheromoneCount = 0;
				Antcolony.phe.addElement(ph);
			} else {
				Antcolony.pheromoneGrid[kind][lastPt.x][lastPt.y] = phe;
				Pheromone ph = new Pheromone(lastPt.x, lastPt.y, 100, 1, id,
						null, 0, kind);
				ph.capability = phe;
				pheromoneCount -= phe;
				Antcolony.phe.addElement(ph);
			}

		}

		phe = (int) (pheromoneCount * 0.005);
		if (phe <= 10)
			phe = 10;

	}

	/**
	 * 判断蚂蚁是否找到目标点，且如果找到，更新蚂蚁的坐标，直接将其放置到目标点
	 * 
	 * @return 是否找到目标点
	 */
	private double reverse(double a) {
		a = a + Math.PI;
		if (a > 2 * Math.PI)
			a -= 2 * Math.PI;
		return a;
	}

	private double dist(Point a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	private boolean judgeEnd() {
		// 首先获得当前蚂蚁是正在找窝还是在找食物。
		int kind = foundTimes % 2; // 如果是0，则正在找食物，反之，则正在找窝
		if (kind == 0) { // 在找食物
			// TODO: 判断是否找到食物
			// 过程提醒：
			// 1.判断是否找到(所有的食物点坐标与当前点比较，距离小于vr就认为是找到)，如果找到，就直接移动到食物点，并更新相关信息。否则直接返回false
			// 2.更新此次路途的总距离，并更新保存该只蚂蚁找到的“窝与食物”之间的最短距离。
			// 3.为下次路途做准备，清除总距离记录(countDistance)、历史记录(historyPoint)。设置目标点(aimPt)以及起始点(startPt)。更新找到目标次数(foundTimes)
			// 4.改变主方向(mainDirect)，记录找到的食物位置(foodPt)。设置可播撒信息素(pheromoneCount)到最大值(maxPheromone)
			for (int i = 0; i < localColony.endCount; i++) {
				double dis = dist(nowPt, localColony.endPt[i]);
				if (dis <= (Math.random() / 2 + 1) * (Math.random() + 3) * vr) {
					historyPoint.removeAllElements();
					lastPt = nowPt;
					historyPoint.addElement(localColony.endPt[i]);
					countDistance += dis;
					minDistance = Math.min(minDistance, countDistance);
					countDistance = 0.0;
					aimPt = localColony.originPt[i];
					startPt = localColony.endPt[i];
					foundTimes = 1;
					mainDirect = reverse(mainDirect);
					pheromoneCount = maxPheromone;
					return true;
				}
			}
			// 否则没找到
			return false;
		} else { // 在找窝
					// TODO: 判断是否找到窝
					// 过程提醒：
					// 1.判断是否找到(窝坐标与当前点比较，距离小于vr就认为是找到)，如果找到，就直接移动到窝，并更新相关信息。否则直接返回false
					// 2.更新此次路途的总距离，并更新保存该只蚂蚁找到的“窝与食物”之间的最短距离。
					// 3.为下次路途做准备，清除总距离记录(countDistance)、历史记录(historyPoint)。设置目标点(aimPt)以及起始点(startPt)。更新找到目标次数(foundTimes)
					// 4.改变主方向(mainDirect)。设置可播撒信息素(pheromoneCount)到最大值(maxPheromone)

			for (int i = 0; i < localColony.originCount; i++) {
				double dis = dist(nowPt, localColony.endPt[i]);
				if (dis <= (Math.random() + 2) * vr) {
					lastPt = nowPt;
					historyPoint.addElement(localColony.originPt[i]);
					countDistance += dis;
					minDistance = Math.min(minDistance, countDistance);
					countDistance = 0.0;
					historyPoint.removeAllElements();
					historyPoint.addElement(localColony.originPt[i]);
					aimPt = localColony.endPt[i];
					startPt = localColony.originPt[i];
					foundTimes = 0;
					mainDirect = reverse(mainDirect);
					pheromoneCount = maxPheromone;
					return true;
				}
			}
			// 否则没找到
			return false;
		}
	}

	/**
	 * 选择方向
	 * 
	 * @return 方向
	 */
	private double selectDirect() {
		// 选择方向，最后选择的方向为主方向加一个随机扰动
		double direct, e = 0;
		if (mainDirect < 0) {
			// 如果目前还没有主方向角，就随机的选择一个
			e = 2 * Math.PI * Math.random();
			mainDirect = e;
		}
		// 选择主方向角
		direct = mainDirect;
		// 做一个随机模型，产生两个随机数，x,y都是[0,1]内的，这样x^2-y^2就是一个
		// [-1，1]的随机数，并且在0点附近的概率大，两边小
		double re = Math.random();
		double re1 = Math.random();

		if (Math.random() < 0.02) {
			// 以小概率0.02改变主方向的值，主方向的选取为从蚂蚁记住的点中随机选一个点，计算当前点和这个点之间的方向角。
			// int size = (int) (re1 * memory) + 1;
			// if (historyPoint.size() > size) {
			// Point pt = (Point) (historyPoint.elementAt(historyPoint.size() -
			// size));
			// if (pt.x != nowPt.x || pt.y != nowPt.y) {
			// mainDirect = getDirection(pt, nowPt);
			// }
			// }
			direct = mainDirect + 3.14 / 18 * ((int) Math.random()) % 100 / 100;
			direct += Math.PI * (re * re - re1 * re1) / 2;
		}
		return direct;
	}

	/**
	 * 获得pt1-->pt2的方向角
	 * 
	 * @param pt1
	 *            起点
	 * @param pt2
	 *            终点
	 * @return 方向角值（单位为弧度制）
	 */
	private double getDirection(Point pt1, Point pt2) {
		// 这个函数为指定两个点pt1和pt2，给出pt1-->pt2的方向角
		// 此函数的难度主要在于，我们的世界是球面，因此需要从多个方向计算方向角，
		// 其中方向角是所有可能的角中使得两点连线距离最短的角。
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
		// return (Math.atan((double)(pt2.y-pt2.y)/(double)(pt2.x-pt1.x)) +
		// Math.PI * 2) % (2 * Math.PI);
	}

	/**
	 * 计算两点的距离
	 * 
	 * @param pt1
	 *            第一个点的位置
	 * @param pt2
	 *            第二个点的位置
	 * @return 两点距离
	 */
	private double distance(Point pt1, Point pt2) {
		// 给定两点pt1,pt2，计算它们之间的距离，难点在于世界是球面，所有有坐标循环的情况，
		// 这里计算的是所有可能距离中最小的
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
		// return Math.sqrt((pt1.x - pt2.x) * (pt1.x - pt2.x) + (pt1.y - pt2.y)
		// * (pt1.y - pt2.y));
	}

	public void clone(Ant ant1) {
		// 把蚂蚁ant1的属性拷贝到本蚂蚁
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