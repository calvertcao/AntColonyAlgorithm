package com;
/*
 * @(#)Antcolony.java 1.0 03/05/22
 *
 * You can modify the template of this file in the
 * directory ..\JCreator\Templates\Template_2\Project_Name.java
 *
 * You can also create your own project template by making a new
 * folder in the directory ..\JCreator\Template\. Use the other
 * templates as examples.
 *
 */

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.util.Vector;

public class Antcolony extends Applet implements Runnable {
	//这些，可能是需要用到的变量
	//地图
	static int width = 300, height = 300;	// 环境的长和宽
	//窝与食物的位置
	static Point originPt[];				// 窝点信息
	static int originCount = 1;				// 窝的个数，最大为100
	static Point endPt[];					// 食物点数组，值为食物点坐标。
	static int endCount = 1;					// 食物点的个数，初始的时候为1，最大数为100
	//障碍物
	static int obsGrid[][];					// 障碍物网格数组，这是个width*height的矩阵，数组中存储的是障碍物数组（obsP[]）的指标，这样做可以加快索引的速度。如果该点没有障碍物，则为-1
	static Point obsP[];					// 障碍物数组，存储的是点的信息，指标是障碍物的总数
	static int obsCount;					// 障碍物的数量，最大为width*height
	//信息素
	static int pheromoneGrid[][][];			// 信息素网格数组，2*width*height的三维矩阵，第一维是信息素种类（窝的信息素为0，食物的为1），它存储的是信息素的种类和值
	static Vector<Pheromone> phe;			// 信息素向量（相当于一个数组），当环境更新信息素的时候，只需要查找这个向量就可以了，不用搜索整个width*height这么多的pheromoneGrid数组点
	static int maxPheromone = 500000;		// 最大信息素数值，应该根据地图的复杂程度来定，越复杂越大！
	static int delimiter = 5;				// 信息素消散的速率，为整数，越大则消散的越快
	static int foodR = 20;					// 食物和窝产生梯度的信息素的半径
	
	
	static Ant ants[];						// 蚂蚁数组
	static int antCount;					// 蚂蚁的数量
	private static final long serialVersionUID = 1L;
	static int drawPhe = 2;					// 画信息素的模式，0为不画，1为画所有的信息素,2为画食物的信息素，3为画窝的信息素
	AntCanvas canvas = new AntCanvas();		// 画图用的画布
	boolean isStandalone = false;			// 系统的参数，是否独立运行，不用管
	Thread runner;							// 创建一个线程，让动画平滑的运行
	boolean running;						// 是否让动画运行
	boolean reset = false;					// 是否按下了重置按钮
	static Color OBS_COLOR = Color.red;		// 障碍物的颜色
	static Color ORIGIN_COLOR = Color.yellow;// 窝的颜色
	static Color BACK_COLOR = Color.black;	// 背景色
	static Color ANT_COLOR = Color.white;	// 蚂蚁的颜色
	static Color End_COLOR = Color.cyan;	// 食物点的颜色
	static int delay = 10;					// 每次运行的间隔速率，越小程序运行越快（这个参数基本没用，因为当蚂蚁多了以后，处理过程很耗时间）

	// 下面是一些控件信息
	Button btnStart = new Button("开始");
	Button btnReset = new Button("重来");
	Button btnMap = new Button("编辑地图");
	Button btnConfig = new Button("设置");
	Choice choPDraw = new Choice();

	public void init() {
		// 初始化函数，先画各种控件
		setLayout(new BorderLayout());
		Panel pan = new Panel();
		add("South", pan);
		this.add("Center", canvas);
		pan.add(btnStart);
		pan.add(btnReset);
		pan.add(btnConfig);
		pan.add(btnMap);
		pan.add(choPDraw);
		choPDraw.addItem("不画信息素");
		choPDraw.addItem("画所有信息素");
		choPDraw.addItem("画食物信息素");
		choPDraw.addItem("画窝的信息素");
		choPDraw.select(2);

		// 初始化各个数组
		obsGrid = new int[width][height];
		phe = new Vector<Pheromone>();
		pheromoneGrid = new int[2][width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				obsGrid[i][j] = -1;
				for (int k = 0; k < 2; k++) {
					pheromoneGrid[k][i][j] = 0;
				}
			}
		}

		antCount = 50;// 蚂蚁个数缺省为50
		// 初始化蚂蚁，这些属性都是蚂蚁的最原始的属性
		ants = new Ant[antCount];
		for (int i = 0; i < antCount; i++) {
			ants[i] = new Ant(new Point(0, 0), 3, i, this, ANT_COLOR, 0.001, 50);
		}

		// 下面装载缺省的地图，包括障碍物、食物点、窝点的位置，都放到数组grid[][]中然后交给init_map函数统一处理
		int grid[][] = new int[width][height];

		// 下面从地图库中加在地图
		Maps.loadMap(grid, 0);
		// 初始化地图
		reinit_map(grid);

		// 初始化所有的蚂蚁
		reinit();
	}

	public void reinit_map(int grid[][]) {
		// 将数组grid[][]中存储的信息转换到当前的环境数据结构中
		// 相当于把一个位图信息width*height像素转化成窝、食物、障碍物

		// 先停止程序的运行
		running = false;
		btnStart.setLabel("开始");

		obsCount = 0;
		endCount = 0;
		originCount = 0;
		obsP = new Point[width * height];
		originPt = new Point[100];
		endPt = new Point[100];

		// 清空obs_grid和Pheromone两个数组中的值
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				obsGrid[i][j] = -1;
				for (int k = 0; k < 2; k++) {
					pheromoneGrid[k][i][j] = 0;
				}
			}
		}

		// 从grid数组中读取信息
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				switch (grid[i][j]) {
				case 1:
					// 如果grid[][]存的是障碍物
					obsGrid[i][j] = obsCount;
					obsP[obsCount] = new Point(i, j);
					obsCount++;
					break;
				case 2:
					// 如果grid[][]存的窝点信息，多余的窝点信息省去了
					if (originCount < 100) {
						originPt[originCount] = new Point(i, j);
						originCount++;
					}
					break;
				case 3:
					// 如果grid[][]存的食物点信息，多余的食物点信息省去了
					if (endCount < 100) {
						endPt[endCount] = new Point(i, j);
						endCount++;
					}
					break;
				}
			}
		}
		// 如果没有指定窝，则随机的选择一点
		if (originCount == 0) {
			for (int i = 0; i < width; i++) {
				int j;
				for (j = 0; j < height; j++) {
					if (obsGrid[i][j] < 0) {
						originPt[originCount] = new Point(i, j);
						originCount++;
						break;
					}
				}
				if (j < height - 1) {
					break;
				}
			}
		}
	}

	public void reinit() {
		// 重新初始化整个环境

		// 先停止程序的运行
		running = false;
		btnStart.setLabel("开始");

		// 清空所有信息素Pheromone数组中的值
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				for (int k = 0; k < 2; k++) {
					pheromoneGrid[k][i][j] = 0;
				}
			}
		}

		// 初始化蚂蚁数组，antCount只蚂蚁在不同的窝点之间进行随机的分配
		for (int i = 0; i < antCount; i++) {
			int index = (int) (originCount * Math.random());
			ants[i].originPt = new Point(originPt[index]);
			ants[i].init();
		}

		// 清空信息素向量
		phe.removeAllElements();

		// 在每个食物点和窝点周围分布一定量的按照梯度递减的信息素，分配的是一个点为中心的半径为FoodR的圆，并且信息素按照半径递减
		for (int i = 0; i < endCount; i++) {
			for (int x = -foodR; x <= foodR; x++) {
				int y = (int) (Math.sqrt(foodR * foodR - x * x));
				for (int yy = -y; yy <= y; yy++) {
					pheromoneGrid[1][(endPt[i].x + x + width) % width][(endPt[i].y + yy + height)
							% height] = (int) (1000 * (1 - Math.sqrt(x * x + yy * yy) / foodR));
				}
			}
		}
		for (int i = 0; i < originCount; i++) {
			for (int x = -foodR; x <= foodR; x++) {
				int y = (int) (Math.sqrt(foodR * foodR - x * x));
				for (int yy = -y; yy <= y; yy++) {
					pheromoneGrid[0][(originPt[i].x + x + width) % width][(originPt[i].y + yy + height)
							% height] = (int) (1000 * (1 - Math.sqrt(x * x + yy * yy) / foodR));
				}
			}
		}

		// 重画
		canvas.repaint();

		// 让程序开始运行
		// running=true;
	}

	public void paint(Graphics g) {
		canvas.repaint();
	}

	public void start()
	// 下面三个函数是控制线程的
	{
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
			// running = true;
		}
	}

	public void stop() {
		if (runner != null) {
			runner.interrupt();
			runner.stop();
			runner = null;
			running = false;
		}
	}

	public void run() {
		// 线程一直运行下去
		while (true) {
			if (running) {
				// 如果开始动画，就进行canvas的处理
				canvas.process();
			}
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
			}
		}

	}

	public boolean action(Event evt, Object o) {
		if (evt.target == btnMap) {
			// 开始编辑地图面板
			running = false;
			btnStart.setLabel("开始");
			MapPad ctl = new MapPad(this);
			ctl.setSize(500, 500);
			ctl.setVisible(true);
			return true;
		} else if (evt.target == btnStart) {
			if (!running) {
				// 如果刚刚按下了重置按钮就重新初始化一下
				if (reset)
					reinit();
				btnStart.setLabel("停止");
				reset = false;
				running = true;
			} else {
				btnStart.setLabel("开始");
				running = false;
			}
			return true;
		} else if (evt.target == btnReset) {
			running = false;
			// 表示已经按下了重置按钮，以便下次开始的时候进行重新初始化
			reset = true;
			repaint();
			btnStart.setLabel("开始");
			return true;
		} else if (evt.target == btnConfig) {
			running = false;
			btnStart.setLabel("开始");
			Configer ctl = new Configer(this);
			ctl.setSize(300, 300);
			ctl.setVisible(true);
			return true;
		} else if (evt.target == choPDraw) {
			// 选择画信息素的模式
			drawPhe = choPDraw.getSelectedIndex();
			if (drawPhe != 1) {
				canvas.repaint();
			}
			return true;
		}
		return false;

	}

	/** Destroy the applet */
	public void destroy() {
		// 当结束程序的时候，把线程也结束
		if (runner != null) {
			running = false;
			runner.stop();
			runner = null;
		}
	}
	
	public static void main(String[] args) {
		Antcolony applet = new Antcolony();
		applet.isStandalone = true;
		Frame frame;
		frame = new Frame() {
			private static final long serialVersionUID = 1L;
			protected void processWindowEvent(WindowEvent e) {
				super.processWindowEvent(e);
				if (e.getID() == WindowEvent.WINDOW_CLOSING) {
					System.exit(0);
				}
			}

			public synchronized void setTitle(String title) {
				super.setTitle(title);
				enableEvents(AWTEvent.WINDOW_EVENT_MASK);
			}
		};
		frame.setTitle("Applet Frame");
		frame.add(applet, BorderLayout.CENTER);
		applet.init();
		applet.start();
		frame.setSize(500, 500);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
		frame.setVisible(true);
	}

}
