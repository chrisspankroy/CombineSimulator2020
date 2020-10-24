import java.awt.*;
import java.awt.event.*;
import static java.lang.String.format;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class CombineSimulator extends JPanel implements Runnable {
   enum Dir {
      up(0, -1), right(1, 0), down(0, 1), left(-1, 0);

      Dir(int x, int y) {
         this.x = x;
         this.y = y;
      }

      final int x, y;
   }
   long startTime = System.nanoTime();
   static final Random rand = new Random();
   static final int WALL = -1;
   static final int MAX_ENERGY = 1500;

   volatile boolean gameOver = true;

   Thread gameThread;
   int score;
   int nRows = (int) (fieldSize - (fieldSize * 0.2));
   int nCols = fieldSize;
   Dir dir;
   int energy;

   int[][] grid;
   List<Point> combine, crops;
   Font smallFont;
   static int fieldSize = 64;
   static int speed = 25;
   static int headerWidth = 1;

   public CombineSimulator() {
      setPreferredSize(new Dimension(nCols * 10, nRows * 10));
      setBackground(new Color(165, 42, 42));
      setFont(new Font("TimesNewRoman", Font.BOLD, 48));
      setFocusable(true);

      smallFont = getFont().deriveFont(Font.BOLD, 18);
      initGrid();
      addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            if (gameOver) {
               startNewGame();
               repaint();
            }
         }
      });
   }

   void startNewGame() {
      gameOver = false;

      stop();
      initGrid();
      crops = new LinkedList<>();
      addCrop();

      dir = Dir.left;
      energy = MAX_ENERGY;

      combine = new ArrayList<>();
      for (int x = 0; x < 7; x++)
         combine.add(new Point(nCols - 2, nRows - 2));

      while (crops.isEmpty())
         ;

      (gameThread = new Thread(this)).start();
   }

   void stop() {
      if (gameThread != null) {
         Thread tmp = gameThread;
         gameThread = null;
         tmp.interrupt();
      }
   }

   void initGrid() {
      grid = new int[nRows][nCols];
      for (int r = 0; r < nRows; r++) {
         for (int c = 0; c < nCols; c++) {
            if (c == 0 || c == nCols - 1 || r == 0 || r == nRows - 1)
               grid[r][c] = WALL;
         }
      }
   }

   @Override
   public void run() {

      while (Thread.currentThread() == gameThread) {

         try {
            Thread.sleep(100 - speed);
         } catch (InterruptedException e) {
            return;
         }

         if (hitsWall() || hitsCrop()) {
            Point head = combine.get(0);

            if (dir == Dir.left) {
               dir = Dir.up;
               head.y += dir.y;
               dir = Dir.right;
            } else if (dir == Dir.right) {
               dir = Dir.up;
               head.y += dir.y;
               dir = Dir.left;
            }

         } else {
            if (eatsCrop()) {
               score += headerWidth;
               energy = MAX_ENERGY;
            }
            moveCrop();
         }
         repaint();

      }
   }
   boolean hitsWall() {
      Point head = combine.get(0);
      int nextCol = head.x + dir.x;
      int nextRow = head.y + dir.y;
      int nowCol = head.x + dir.x;
      int nowRow = head.y + dir.y;
      if (grid[nowRow][nowCol] == WALL) {
         return true;
      }
      return grid[nextRow][nextCol] == WALL;
   }

   boolean hitsCrop() {
      Point head = combine.get(0);
      int nextCol = head.x + dir.x;
      int nextRow = head.y + dir.y;
      for (Point p : combine)
         if (p.x == nextCol && p.y == nextRow)
            return true;
      return false;
   }

   boolean eatsCrop() {
      Point head = combine.get(0);
      int nextCol = head.x + dir.x;
      int nextRow = head.y + dir.y;
      for (Point p : crops)
         if (p.x == nextCol && p.y == nextRow) {
            return crops.remove(p);
         }
      return false;
   }

   void gameOver() {
      gameOver = true;
      stop();
   }

   void moveCrop() {
      Point head = combine.get(0);

      head.x += dir.x;
      head.y += dir.y;
   }

   void addCrop() {
      for (int x = 1; x < nCols - 1; x++) {
         for (int y = 1; y < nRows - 1; y++) {
            Point p = new Point(x, y);
            crops.add(p);
         }
      }
   }

   void drawGrid(Graphics2D g) {
      g.setColor(Color.black);
      for (int r = 0; r < nRows; r++) {
         for (int c = 0; c < nCols; c++) {
            if (grid[r][c] == WALL)
               g.fillRect(c * 10, r * 10, 10, 10);
         }
      }
   }

   void drawCrop(Graphics2D g) {
      g.setColor(Color.red);
      for (Point p : combine)
         g.fillRect(p.x * 10, p.y * 10, 10, 10);

      g.setColor(energy < 500 ? Color.red : Color.orange);
      Point head = combine.get(0);
      g.fillRect(head.x * 10, head.y * 10, 10, 10);
   }

   void drawCrops(Graphics2D g) {
      g.setColor(Color.green);
      for (Point p : crops)
         g.fillRect(p.x * 10, p.y * 10, 10, 10);
   }

   void drawStartScreen(Graphics2D g) {
      g.setColor(Color.green);
      g.setFont(smallFont);
      String s = "Combine Simulator 2020";
      g.drawString(s, 210, 190);
      g.setColor(Color.orange);
      g.setFont(smallFont);
      g.drawString("(Click To START)", 250, 250);
   }

 void drawScore(Graphics2D g) {
      int h = getHeight();
      g.setFont(smallFont);
      g.setColor(getForeground());
      String s = format("Crop Count: %d", score);
      
      long endTime   = System.nanoTime();
      long totalTime = (endTime - startTime)/1000000000;
      String t = format("Time: %d", totalTime);
      
      g.drawString(s, 30, h - 30);
      g.drawString(t, 40, 30);
      //g.drawString(format("Energy: %d", energy), getWidth() - 150, h - 30);
}

   @Override
   public void paintComponent(Graphics gg) {
      super.paintComponent(gg);
      Graphics2D g = (Graphics2D) gg;
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      drawGrid(g);

      if (gameOver) {
         drawStartScreen(g);
      } else {
         drawCrop(g);
         drawCrops(g);
         drawScore(g);
      }
   }

   public static void main(String[] args) {

      SwingUtilities.invokeLater(() -> {
         final String fieldSizeList[] = { "64", "70", "80" };
         final String speedList[] = { "25", "50", "75" };
         final String headerWidthList[] = { "1", "2", "3", "4" };
         JComboBox fieldSizeComboBox;
         JComboBox speedComboBox;
         JComboBox headerWidthComboBox;

         fieldSizeComboBox = new JComboBox(fieldSizeList);
         speedComboBox = new JComboBox(speedList);
         headerWidthComboBox = new JComboBox(headerWidthList);

         JFrame mainFrame = new JFrame();

         final CombineSimulator[] gameArr = new CombineSimulator[1];
         gameArr[0] = new CombineSimulator();

         JButton applyButton = new JButton("Apply");
         applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               String newFieldSize = (String) fieldSizeComboBox.getSelectedItem();
               String newSpeed = (String) fieldSizeComboBox.getSelectedItem();
               String newHeaderWidth = (String) headerWidthComboBox.getSelectedItem();

               fieldSize = Integer.parseInt(newFieldSize);
               speed = Integer.parseInt(newSpeed);
               headerWidth = Integer.parseInt(newHeaderWidth);

               mainFrame.remove(gameArr[0]);
               mainFrame.repaint();
               gameArr[0] = new CombineSimulator();
               mainFrame.add(gameArr[0], BorderLayout.NORTH);
               mainFrame.repaint();
               mainFrame.pack();
            }
         });

         mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         mainFrame.setTitle("Combine Simulator");
         mainFrame.setResizable(false);
         mainFrame.add(gameArr[0], BorderLayout.NORTH);

         JPanel options = new JPanel();
         options.add(new JLabel("Field Size:"), BorderLayout.SOUTH);
         options.add(fieldSizeComboBox, BorderLayout.SOUTH);
         options.add(new JLabel("Speed:"), BorderLayout.SOUTH);
         options.add(speedComboBox, BorderLayout.SOUTH);
         options.add(new JLabel("Header Width:"), BorderLayout.SOUTH);
         options.add(headerWidthComboBox, BorderLayout.SOUTH);
         options.add(applyButton, BorderLayout.SOUTH);

         mainFrame.add(options, BorderLayout.SOUTH);

         mainFrame.pack();
         mainFrame.setLocationRelativeTo(null);
         mainFrame.setVisible(true);

      });
   }
}