package es.carlosrolindez.ping;


import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;


class PingGameClass {
    private static final String TAG = "PingGameClass";

    // PING GAME STATES
    public static final int START = 0x10;
    public static final int GETTING_READY = 0x20;
    public static final int PLAYING = 0x30;
    public static final int GOAL = 0x40;
    public static final int END = 0x50;

    private static final float BALL_X_GPF = 2.0f;
    private static final float BALL_Y_GPF = 0.6f;
    private static final int PLAYER_GPF = 2;

    // PING ENTITIES
    private static final int PLAYER_LEFT = 0;
    private static final int PLAYER_RIGHT = 1;
    private static final int BALL = 2;
    private static final int TOP_BAR = 3;
    private static final int BOTTOM_BAR = 4;
    private static final int OBSTACLE_1 = 5;
    private static final int OBSTACLE_2 = 6;

    // PING MOVE RESULTS
    static final int GOAL_MOVEMENT = 3;
    static final int BOUNCE_PLAYER = 2;
    static final int BOUNCE_WALL =   1;
    static final int OK_MOVEMENT =   0;

    // COORDINATES
    private static final int WINDOWS_X_SIZE = 200;
    private static final int WINDOWS_Y_SIZE = 100;
    private static final int SIZE_BALL = 4;
    private static final int WIDTH_PLAYER = 3;
    private static final int HEIGHT_PLAYER = 25;
    private static final int WIDTH_BAR = 190;
    private static final int HEIGHT_BAR = 1;

    private static final int WIDTH_OBSTACLE = 2;
    private static final int HEIGHT_OBSTACLE = 10;


    private static final int INIT_X_PLAYER_LEFT = 7;
    private static final int INIT_X_PLAYER_RIGHT = 193;

    private static final int INIT_Y_TOP_BAR = 1;
    private static final int INIT_Y_BOTTOM_BAR = 99;

    private static final int INIT_Y_OBSTACLE_1 = 25;
    private static final int INIT_Y_OBSTACLE_2 = 75;

    private static final float MIN_LIMIT_Y_BALL = INIT_Y_TOP_BAR + (SIZE_BALL + HEIGHT_BAR) / 2.0f;
    private static final float MAX_LIMIT_Y_BALL = INIT_Y_BOTTOM_BAR - (SIZE_BALL + HEIGHT_BAR) / 2.0f;

    private static final float MIN_LIMIT_X_BALL = INIT_X_PLAYER_LEFT  + (SIZE_BALL + WIDTH_PLAYER) / 2.0f;
    private static final float MAX_LIMIT_X_BALL = INIT_X_PLAYER_RIGHT - (SIZE_BALL + WIDTH_PLAYER) / 2.0f;

    private static final float MIN_LIMIT_Y_PLAYER = INIT_Y_TOP_BAR + (HEIGHT_PLAYER + HEIGHT_BAR) / 2.0f;
    private static final float MAX_LIMIT_Y_PLAYER = INIT_Y_BOTTOM_BAR - (HEIGHT_PLAYER + HEIGHT_BAR) / 2.0f;

    // Game State
    private int gameState;
    private float levelScale;

    private int leftScore, rightScore;

    // Entities
    private final PositionClass mPlayerLeft;
    private final PositionClass mPlayerRight;
    private final PositionClass mBall;
    private final PositionClass mTopBar;
    private final PositionClass mBottomBar;
    private final PositionClass mObstacle1;
    private final PositionClass mObstacle2;

    private final TextView mLeftScoreText;
    private final TextView mRightScoreText;

    // Windows constants
    private static int width, height;
    private static float xGU, yGU; // game units


    PingGameClass(ImageView ball, ImageView playerLeft, ImageView playerRight, ImageView topBar,
                  ImageView bottomBar, TextView leftScoreText, TextView rightScoreText,
                  ImageView obstacle1, ImageView obstacle2) {
        gameState = START;

        width = WINDOWS_X_SIZE; // default values.  Must be updated by UpdateWindowConstanst
        height = WINDOWS_Y_SIZE;
        xGU = 1.0f;
        yGU = 1.0f;

        mPlayerLeft = new PositionClass(playerLeft, PLAYER_LEFT);
        mPlayerRight = new PositionClass(playerRight, PLAYER_RIGHT);
        mBall = new PositionClass(ball, BALL);
        mTopBar = new PositionClass(topBar, TOP_BAR);
        mBottomBar = new PositionClass(bottomBar, BOTTOM_BAR);
        mLeftScoreText = leftScoreText;
        mRightScoreText = rightScoreText;
        mObstacle1 = new PositionClass(obstacle1,OBSTACLE_1);
        mObstacle2 = new PositionClass(obstacle2, OBSTACLE_2);


        leftScore = 0;
        rightScore = 0;
        updateScore();


    }


    private synchronized void updateScore() {
        mLeftScoreText.setText(String.format(Locale.US, "%d",leftScore));
        mRightScoreText.setText(String.format(Locale.US, "%d",rightScore));
    }

    public synchronized void setLevel(int level) {
        switch (level) {
            case Constants.LEVEL_EXPERT:
                levelScale = 2.2f;
                break;
            case Constants.LEVEL_HARD:
                levelScale = 1.6f;
                break;
            case Constants.LEVEL_MEDIUM:
                levelScale = 1.6f;
                break;
            case Constants.LEVEL_EASY:
            default:
                levelScale = 1.0f;
                break;
        }
        mBall.resetPosition();
    }

    public synchronized void leftGoal() {
        leftScore++;
        updateScore();
    }

    public synchronized void rightGoal() {
        rightScore++;
        updateScore();
    }


    public synchronized void updateWindowConstants(int top, int bottom, int left, int right  ) {

        if ((height == (bottom - top)) && (width == (right - left)))
            return;

        height = (bottom - top);
        width = right - left;
        xGU = width / (float)WINDOWS_X_SIZE;
        yGU = height / (float)WINDOWS_Y_SIZE;

        mBall.resizeImage((int)(SIZE_BALL*xGU), (int) (SIZE_BALL*yGU));
        mPlayerLeft.resizeImage((int)(WIDTH_PLAYER*xGU), (int) (HEIGHT_PLAYER*yGU));
        mPlayerRight.resizeImage((int)(WIDTH_PLAYER*xGU), (int) (HEIGHT_PLAYER*yGU));
        mTopBar.resizeImage((int)(WIDTH_BAR*xGU), (int) (HEIGHT_BAR*yGU));
        mBottomBar.resizeImage((int)(WIDTH_BAR*xGU), (int) (HEIGHT_BAR*yGU));
        mObstacle1.resizeImage((int)(WIDTH_OBSTACLE*xGU), (int) (HEIGHT_OBSTACLE*yGU));
        mObstacle2.resizeImage((int)(WIDTH_OBSTACLE*xGU), (int) (HEIGHT_OBSTACLE*yGU));


    }

    public synchronized void setState(int newState) {
        gameState = newState;
    }

    public synchronized int getState() {
        return gameState;
    }

    public void reset() {
        mBall.resetPosition();
        mPlayerLeft.resetPosition();
        mPlayerRight.resetPosition();
    }


    public synchronized String ballMessage() {
        return " "  + (WINDOWS_X_SIZE - mBall.getXPosition()) + " " + mBall.getYPosition() + " " + -mBall.getXdelta() + " " + mBall.getYdelta() + " " ;
    }

    public synchronized String playerMessage() {
        return " "  + mPlayerLeft.getYPosition() + " " ;
    }

    public synchronized void setBall(float xPosition, float yPosition, float xDelta,float yDelta) {
        mBall.setPosition(xPosition, yPosition);
        mBall.setDelta(xDelta, yDelta);
    }

    public synchronized void setPlayerRight(float yPosition) {
        mPlayerRight.setYPosition(yPosition);
    }

    public synchronized  int moveBall() { // calculate next ball position and return true if ball bounced on the player
//TODO bounce obstacles
        int result =   mBall.nextPosition(mPlayerLeft.getYPosition());

        if (result == GOAL_MOVEMENT) {
            rightGoal();
            reset();
            return result;
        }
        mBall.setNextPosition();
        return result;

    }


    public synchronized  boolean movePlayerUp() {
        if (mPlayerLeft.getYPosition() == MIN_LIMIT_Y_PLAYER) return false;
        if ((mPlayerLeft.getYPosition() - PLAYER_GPF) > MIN_LIMIT_Y_PLAYER)
            mPlayerLeft.setYPosition(mPlayerLeft.getYPosition() - PLAYER_GPF);
        else
            mPlayerLeft.setYPosition(MIN_LIMIT_Y_PLAYER);
        return true;

    }

    public synchronized  boolean movePlayerDown() {
        if (mPlayerLeft.getYPosition() == MAX_LIMIT_Y_PLAYER) return false;
        if ((mPlayerLeft.getYPosition() + PLAYER_GPF) < MAX_LIMIT_Y_PLAYER)
            mPlayerLeft.setYPosition(mPlayerLeft.getYPosition() + PLAYER_GPF);
        else
            mPlayerLeft.setYPosition(MAX_LIMIT_Y_PLAYER);
        return true;
    }

    private class PositionClass {

        private float xPosition, yPosition, xDelta, yDelta, xSize, ySize;
        private float xScaleDelta, yScaleDelta;
        private final ImageView mView;
        private final int entity;
        private float nextX, nextY;
        private final Random rand = new Random();

        PositionClass(ImageView view, int entity){
            xPosition = 0;
            yPosition = 0;
            xDelta = 0;
            yDelta = 0;
            xScaleDelta = 0;
            yScaleDelta = 0;
            xSize = 0;
            ySize = 0;
            mView = view;
            this.entity = entity;

            resetPosition();

        }

        private void resetPosition() {
            switch (entity) {
                case PLAYER_LEFT:
                    xSize = WIDTH_PLAYER;
                    ySize = HEIGHT_PLAYER;
                    setPosition(INIT_X_PLAYER_LEFT, WINDOWS_Y_SIZE / 2.0f );
                    break;
                case PLAYER_RIGHT:
                    xSize = WIDTH_PLAYER;
                    ySize = HEIGHT_PLAYER;
                    setPosition(INIT_X_PLAYER_RIGHT, WINDOWS_Y_SIZE / 2.0f );
                    break;
                case TOP_BAR:
                    xSize = WIDTH_BAR;
                    ySize = HEIGHT_BAR;
                    setPosition(WINDOWS_X_SIZE / 2.0f, INIT_Y_TOP_BAR);
                    break;
                case BOTTOM_BAR:
                    xSize = WIDTH_BAR;
                    ySize = HEIGHT_BAR;
                    setPosition(WINDOWS_X_SIZE / 2.0f, INIT_Y_BOTTOM_BAR);
                    break;
                case BALL:
                    xSize = SIZE_BALL;
                    ySize = SIZE_BALL;
                    setPosition(WINDOWS_X_SIZE / 2.0f , WINDOWS_Y_SIZE / 2.0f);
                    setXdelta(BALL_X_GPF);
                    if ((Math.random() * 2) > 1) {
                        setYdelta(BALL_Y_GPF);
                    } else {
                        setYdelta(-BALL_Y_GPF);
                    }
                    break;
                case OBSTACLE_1:
                    xSize = WIDTH_OBSTACLE;
                    ySize = HEIGHT_OBSTACLE;
                    setPosition(WINDOWS_X_SIZE / 2.0f, INIT_Y_OBSTACLE_1);
                    break;
                case OBSTACLE_2:
                    xSize = WIDTH_OBSTACLE;
                    ySize = HEIGHT_OBSTACLE;
                    setPosition(WINDOWS_X_SIZE / 2.0f, INIT_Y_OBSTACLE_2);
                    break;
                default:
                    break;
            }


        }

        private float getXPosition() { return xPosition; }

        private float getYPosition() { return yPosition; }

        private float getXSize() { return xSize;}

        private float getYSize() { return ySize;}

        private void setPosition(float x, float y) {
            xPosition = x;
            yPosition = y;
            setImage();
        }

        private void setYPosition(float y) {
            yPosition = y;
            setImage();
        }

        private void setXPosition(float x) {
            xPosition = x;
            setImage();
        }

        private void setNextPosition() {
            setPosition(nextX,nextY);
        }

        private float getXdelta() {
            return xDelta;
        }

        private float getYdelta() {
            return yDelta;
        }


        private void setXdelta(float x) {
            xDelta = x;
            xScaleDelta = xDelta * levelScale;
        }

        private void setYdelta(float y) {
            yDelta = y;
            yScaleDelta = yDelta * levelScale;
        }

        private void setDelta(float x, float y) {
            xDelta = x;
            yDelta = y;
            xScaleDelta = xDelta * levelScale;
            yScaleDelta = yDelta * levelScale;
        }

        private void resizeImage(int xSize, int ySize) {
            mView.setLayoutParams(new RelativeLayout.LayoutParams(xSize,ySize));
            setImage();
        }

        private void setImage() {
            mView.setX(((xPosition - xSize/2.0f )* xGU));
            mView.setY(((yPosition - ySize/2.0f) * yGU));
        }

        private int nextPosition(float playerYposition) {
            if (xDelta > 0) {                                       // moving to the right...
                if ((xPosition) > MAX_LIMIT_X_BALL) {                       // ... GOAL
                    nextX = MAX_LIMIT_X_BALL;
                    nextY = yPosition;
                    return OK_MOVEMENT;
                } else {
                    return checkBounceWall();                                 //  ... OK movement
                }
            } else {                                                // moving to the left...
                if (xPosition < MIN_LIMIT_X_BALL) {                         // ... GOAL
                    nextX = xPosition;
                    nextY = yPosition;
                    return GOAL_MOVEMENT;
                } else if ((xPosition + xScaleDelta) < MIN_LIMIT_X_BALL) {  // ... bounce on left Player
                    return checkBounce(playerYposition);
                } else return checkBounceWall();                              // ... Ok movement
            }
        }
        private int checkBounceWall() {
            nextX = xPosition + xScaleDelta;
            if ((yPosition + yScaleDelta) > MAX_LIMIT_Y_BALL) {   // checks limits after yDelta
                nextY =  2 * MAX_LIMIT_Y_BALL - yScaleDelta - yPosition;
                yDelta = -yDelta;
                yScaleDelta = -yScaleDelta;
                return BOUNCE_WALL;
            } else if ((yPosition + yScaleDelta) < MIN_LIMIT_Y_BALL) {
                nextY = 2 * MIN_LIMIT_Y_BALL - yScaleDelta - yPosition;
                yScaleDelta = -yScaleDelta;
                yDelta = -yDelta;
                return BOUNCE_WALL;
            } else {
                nextY = yPosition + yScaleDelta;
                return OK_MOVEMENT;
            }
        }


        private int checkBounce(float playerYposition) {
            float distX = xPosition- MIN_LIMIT_X_BALL;
            float distY;
            int result;

            if ((yPosition + yScaleDelta) > MAX_LIMIT_Y_BALL) {             // On bottom Limit ...
                distY = MAX_LIMIT_Y_BALL - yPosition;
                if ((distX / xDelta) > (distY / yDelta)) {                          //  ... first Wall then Player
                    checkBounceWall();
                    yPosition = nextY - yScaleDelta;
                    return checkBouncePlayer(playerYposition);

                } else {                                                            //  ... first Player then Wall
                    result = checkBouncePlayer(playerYposition);
                    checkBounceWall();
                    return result;
                }
            } else if ((yPosition + yScaleDelta) < MIN_LIMIT_Y_BALL) {      // On Top limit ...
                distY = yPosition - MIN_LIMIT_Y_BALL;
                if ((distX / xDelta) > (distY / yDelta)) {                          //  ... first Wall then Player
                    checkBounceWall();
                    yPosition = nextY - yScaleDelta;
                    return checkBouncePlayer(playerYposition);

                } else {                                                            //  ... first Player then Wall
                    result = checkBouncePlayer(playerYposition);
                    checkBounceWall();
                    return result;
                }

            } else  {                                                       // between Limits ... only player
                result = checkBouncePlayer(playerYposition);
                checkBounceWall();
                return result;
            }

        }

        private int checkBouncePlayer(float playerYposition)  {
            float distX = xPosition- MIN_LIMIT_X_BALL;
            float distY = distX * yScaleDelta / xScaleDelta;
            float impactYposition = yPosition + distY;


            if ((playerYposition-(HEIGHT_PLAYER/2)-(mBall.getYSize()/2)) >= impactYposition) {
                nextX = xPosition + xScaleDelta;
                return GOAL_MOVEMENT;
            } else if ((playerYposition-(HEIGHT_PLAYER/4)-(SIZE_BALL/2)) >= impactYposition)  { // Up area
                if (yDelta > 0) {
                    yDelta /= 2;
                    yDelta += randomDelta();
                    if (yDelta < 0) yDelta = 0;
                }
                else {
                    yDelta *= 2;
                    yDelta += randomDelta();
                    if (yDelta < (-3 * BALL_Y_GPF))
                        yDelta = -3 * BALL_Y_GPF;
                }
                yScaleDelta = yDelta * levelScale;
            } else if ((playerYposition+(HEIGHT_PLAYER/4)+(SIZE_BALL/2)) > impactYposition) { // Middle area
                yDelta += randomDelta();
                yScaleDelta = yDelta * levelScale;
            } else if ((playerYposition+(HEIGHT_PLAYER/2)+(SIZE_BALL/2)) > impactYposition) { // Lower area
                if (yDelta > 0) {
                    yDelta *= 2;
                    yDelta += randomDelta();
                    if (yDelta > (3 * BALL_Y_GPF))
                        yDelta = 3 * BALL_Y_GPF;
                }
                else {
                    yDelta /= 2;
                    yDelta += randomDelta();
                    if (yDelta > 0) yDelta = 0;
                }
                yScaleDelta = yDelta * levelScale;
            } else {
                nextX = xPosition + xScaleDelta;
                return GOAL_MOVEMENT;
            }
            nextX = 2 * MIN_LIMIT_X_BALL - xScaleDelta - xPosition;
            xDelta = -xDelta;
            xScaleDelta = -xScaleDelta;
            return BOUNCE_PLAYER;

        }

        private double randomDelta() {
            return ( (BALL_Y_GPF) / 2) * rand.nextGaussian();
        }
    }



}
