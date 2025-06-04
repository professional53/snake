import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }  

    int boardWidth;
    int boardHeight;
    int tileSize = 25;
    
    //snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    //food
    Tile food;
    Tile WALL;
    Random random;

    //game logic
    int velocityX;
    int velocityY;
    Timer gameLoop;

    // other things
    int Delay;
    int Scorer; // same as ActualScore ignore this coming before it
    int ActualScore; // redundant but I don't want to spend the time getting rid of it
    Color snakeColor;
    Color foodColor;
    boolean CCInput = false;

    boolean gameOver = false;

    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<Tile>();
        snakeColor = Color.green;
        foodColor = Color.red;

        food = new Tile(10, 10);
        random = new Random();

        WALL = new Tile(10, 10);
        placeFood();
        
		//game timer
        Delay = 250;
		gameLoop = new Timer(Delay, this); //how long it takes to start timer, milliseconds gone between frames
        gameLoop.start();
	}	
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
	}

	public void draw(Graphics g) {
        //Grid Lines
        for(int i = 0; i < boardWidth/tileSize; i++) {
            //(x1, y1, x2, y2)
            g.drawLine(i*tileSize, 0, i*tileSize, boardHeight);
            g.drawLine(0, i*tileSize, boardWidth, i*tileSize); 
        }

        //Food
        g.setColor(foodColor);
        // g.fillRect(food.x*tileSize, food.y*tileSize, tileSize, tileSize);
        g.fill3DRect(food.x*tileSize, food.y*tileSize, tileSize, tileSize, true);


        // Wall
        g.setColor(Color.darkGray);
        g.fill3DRect(WALL.x*tileSize, WALL.y*tileSize, tileSize, tileSize, true);
        g.setColor(Color.gray);
        g.fill3DRect(WALL.x*tileSize+(tileSize/4), WALL.y*tileSize+(tileSize/4), tileSize-(tileSize/2), tileSize-(tileSize/2), true);

        //Snake Head
        g.setColor(snakeColor);
        // g.fillRect(snakeHead.x, snakeHead.y, tileSize, tileSize);
        // g.fillRect(snakeHead.x*tileSize, snakeHead.y*tileSize, tileSize, tileSize);
        g.fill3DRect(snakeHead.x*tileSize, snakeHead.y*tileSize, tileSize, tileSize, true);
        
        //Snake Body
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);
            // g.fillRect(snakePart.x*tileSize, snakePart.y*tileSize, tileSize, tileSize);
            g.fill3DRect(snakePart.x*tileSize, snakePart.y*tileSize, tileSize, tileSize, true);
		}

        //Score
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if (gameOver && CCInput) {
            g.setColor(Color.red);
            g.drawString("Game Over: " + ActualScore, tileSize - 16, tileSize);
            g.setColor(Color.WHITE);
            g.drawString("Press R to restart ", tileSize - 16, tileSize*2);
        } else if (gameOver) {
            g.setColor(Color.red);
            g.drawString("Game Over: " + ActualScore, tileSize - 16, tileSize);
            g.setColor(Color.WHITE);
            g.drawString("Press R to restart ", tileSize - 16, tileSize*2);
            g.setColor(Color.green);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Press F to unlock In-Game Purchases!!!!!", tileSize - 16, tileSize*3);
        }
        else {
            g.setColor(Color.WHITE);
            g.drawString("Score: " + ActualScore, tileSize - 16, tileSize);
        }
	}

    public void placeFood(){
        WALL.x = random.nextInt(boardWidth/tileSize);
        WALL.y = random.nextInt(boardHeight/tileSize);
        food.x = random.nextInt(boardWidth/tileSize);
        food.y = random.nextInt(boardHeight/tileSize);
        while(food.y==WALL.y && food.x==WALL.x){
            food.x = random.nextInt(boardWidth/tileSize);
            food.y = random.nextInt(boardHeight/tileSize);
        }
	}

    public void move() {
        //eat food
        if (collision(snakeHead, food)) {
            Scorer++;
            ActualScore++;
            if(Scorer>=1){
                Scorer=0;
                Delay=50+(Delay/2);
                gameLoop.setDelay(Delay);
            }
            snakeBody.add(new Tile(food.x, food.y));
            placeFood();
        }


        //move snake body
        for (int i = snakeBody.size()-1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) { //right before the head
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            }
            else {
                Tile prevSnakePart = snakeBody.get(i-1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }
        //move snake head
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        //game over conditions
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);

            //collide with snake head
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
            }
            if (collision(snakeHead, WALL)) {
                gameOver = true;
            }
        }

        if (snakeHead.x*tileSize < 0 || snakeHead.x*tileSize > boardWidth || //passed left border or right border
            snakeHead.y*tileSize < 0 || snakeHead.y*tileSize > boardHeight ) { //passed top border or bottom border
            gameOver = true;
        }
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) { //called every x milliseconds by gameLoop timer
        move();
        repaint();
        if (gameOver) {
            System.out.println("Final Score: "+ActualScore);
            gameLoop.stop();
        }
    }

    public void OpenMTWindow() {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        // System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        }
        if (gameOver && e.getKeyCode() == KeyEvent.VK_R) {
            snakeHead = new Tile(5, 5);
            snakeBody = new ArrayList<Tile>();
            random = new Random();
            placeFood();
            velocityX = 0;
            velocityY = 0;
            ActualScore = 0;
            Delay = 250;
            gameOver = false;
            gameLoop.setDelay(Delay);
            gameLoop.start();
        } else if (gameOver && e.getKeyCode() == KeyEvent.VK_F && !CCInput){
            String finder = JOptionPane.showInputDialog("What is your credit card number?");
            if (finder != null){
                JOptionPane.showMessageDialog(null, "Your Number has been recorded for this session.", "Thank You!", 1);
                CCInput = true;JButton snakeColorBTN = new JButton("[$0.50] Roll Snake Color");
                snakeColorBTN.setBounds(0, boardHeight/2, 200, 60);
                snakeColorBTN.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0){
                        int rand = random.nextInt(99);
                        if(rand == 0){
                            snakeColor = new Color(236,88,0); System.out.println("SNAKE COLOR RESULT: You Rolled Persimmon!!! - MYTHIC");
                        }else if (rand<=5){
                            snakeColor = Color.orange; System.out.println("SNAKE COLOR RESULT: You Rolled Orange! - LEGENDARY");
                        }else if(rand<=15){
                            snakeColor = new Color(161, 51, 245); System.out.println("SNAKE COLOR RESULT: You Rolled Purple - EPIC");
                        }else if(rand<=40){
                            snakeColor = Color.blue; System.out.println("SNAKE COLOR RESULT: You Rolled Blue - RARE");
                        }else{
                            snakeColor = Color.green; System.out.println("SNAKE COLOR RESULT: You Rolled Green - COMMON");
                        }
                    }
                });
                JButton foodColorBTN = new JButton("[$1.00] Roll Food Color");
                foodColorBTN.setBounds(boardWidth/2+50, boardHeight/2, 200, 60);
                foodColorBTN.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0){
                        int rand = random.nextInt(99);
                        if(rand == 0){
                            foodColor = Color.yellow; System.out.println("FOOD COLOR RESULT: You Rolled Yellow! - Legendary");
                        }else if (rand<=5){
                            foodColor = Color.pink; System.out.println("FOOD COLOR RESULT: You Rolled Pink! - Epic");
                        }else if(rand<=15){
                            foodColor = new Color(42, 140, 130); System.out.println("FOOD COLOR RESULT: You Rolled Teal - Rare");
                        }else if(rand<=40){
                            foodColor = new Color(69, 25, 6); System.out.println("FOOD COLOR RESULT: You Rolled Brown - Uncommon");
                        }else{
                            foodColor = Color.red; System.out.println("FOOD COLOR RESULT: You Rolled Red - Common");
                        }
                    }
                });

                JFrame MicrotransactionWindow = new JFrame("SUPPORT OUR GAME!");
                MicrotransactionWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                MicrotransactionWindow.setSize(boardWidth,boardHeight);
                MicrotransactionWindow.setLocationRelativeTo(this);
                MicrotransactionWindow.setResizable(false);
                MicrotransactionWindow.setLayout(null);

                MicrotransactionWindow.add(snakeColorBTN);
                MicrotransactionWindow.add(foodColorBTN);
                MicrotransactionWindow.setVisible(true);
                snakeHead = new Tile(5, 5);
                snakeBody = new ArrayList<Tile>();
                random = new Random();
                placeFood();
                velocityX = 0;
                velocityY = 0;
                Delay = 250;
                ActualScore = 0;
                gameOver = false;
                gameLoop.setDelay(Delay);
                gameLoop.start();
            }
        }
    }



    //not needed
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
