import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

public class Simulator extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;

	public static int width = 300;
	public static int height = width/16*9;
	public static int scale = 3; //scales the size of the window but only processes as if it were width x height
	public static String title = "Simulator";

	private Thread thread;
	private JFrame frame;
	private boolean running = false;
	private Keyboard key;

	private Display display;

	private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

	public Simulator() {
		Dimension size = new Dimension(width*scale, height*scale);
		setPreferredSize(size);
		display = new Display(width,height);
		frame = new JFrame();

		key = new Keyboard();
		addKeyListener(key);

	}

	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Display");
		thread.start();
	}
	public synchronized void stop() {
		running = false;
		try {
		thread.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void run() {
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double ns = 1000000000.0 / 60.0;
		double delta = 0;
		int frames = 0;
		int updates = 0;
		while(running) {
			long now = System.nanoTime();
			delta += (now-lastTime)/ns;
			lastTime = now;
			while (delta>=1) {
				tick();
				updates++;
				delta--;
			}
			render();
			frames++;

			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				System.out.println(updates + " ups, " + frames + " fps");
				frame.setTitle(title + "     |     " + frames + " fps");
				updates = 0;
				frames = 0;
			}
		}
		stop();
	}
	int x=0, y=0;
	public void tick() {
		key.update();
		if (key.up) y--;
		if (key.down) y++;
		if (key.left) x--;
		if (key.right) x++;

	}


	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs==null) {
			createBufferStrategy(3);
			return;
		}

		display.clear();
		display.render(x,y);

		for (int i=0;i<pixels.length;i++) {
			pixels[i] = display.pixels[i];
		}

		Graphics g = bs.getDrawGraphics();
		{
			g.fillRect(0, 0, getWidth(), getHeight());
			g.drawImage(image,  0, 0, getWidth(),  getHeight(),  null);
		}
		g.dispose();
		bs.show();
	}

	public static void main(String[] args) {
		Simulator sim = new Simulator();
		sim.frame.setResizable(false);
		sim.frame.setTitle(Simulator.title);
		sim.frame.add(sim);
		sim.frame.pack();
		sim.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sim.frame.setLocationRelativeTo(null);
		sim.frame.setVisible(true);
		
		sim.start();
	}
}
