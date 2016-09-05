package es.carlosrolindez.ping;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;


public class PingGameClass {
    private static final String TAG = "PingGameClass";

    // PING GAME STATES
    public static final int START = 0x10;
    public static final int GETTING_READY = 0x20;
    public static final int PLAYING = 0x30;
    public static final int GOAL = 0x40;
    public static final int END = 0x50;

    private static final int BALL_GPF = 2;
    private static final int PLAYER_GPF = 2;

    // PING ENTITIES
    private static final int PLAYER_LEFT = 0;
    private static final int PLAYER_RIGHT = 1;
    private static final int BALL = 2;
    private static final int TOP_BAR = 3;
    private static final int BOTTOM_BAR = 4;

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

    private static final int INIT_X_PLAYER_LEFT = 7;
    private static final int INIT_X_PLAYER_RIGHT = 193;

    private static final int INIT_Y_TOP_BAR = 1;
    private static final int INIT_Y_BOTTOM_BAR = 99;

    private static final float MIN_LIMIT_Y_BALL = INIT_Y_TOP_BAR + (SIZE_BALL + HEIGHT_BAR) / 2.0f;
    private static final float MAX_LIMIT_Y_BALL = INIT_Y_BOTTOM_BAR - (SIZE_BALL + HEIGHT_BAR) / 2.0f;;

    private static final float MIN_LIMIT_X_BALL = INIT_X_PLAYER_LEFT  + (SIZE_BALL + WIDTH_PLAYER) / 2.0f;
    private static final float MAX_LIMIT_X_BALL = INIT_X_PLAYER_RIGHT - (SIZE_BALL + WIDTH_PLAYER) / 2.0f;;

    private static final float MIN_LIMIT_Y_PLAYER = INIT_Y_TOP_BAR + (HEIGHT_PLAYER + HEIGHT_BAR) / 2.0f;
    private static final float MAX_LIMIT_Y_PLAYER = INIT_Y_BOTTOM_BAR - (HEIGHT_PLAYER + HEIGHT_BAR) / 2.0f;

    // Game State
    private int gameState;

    private int leftScore, rightScore;

    // Entities
    private PositionClass mPlayerLeft;
    private PositionClass mPlayerRight;
    private PositionClass mBall;
    private PositionClass mTopBar;
    private PositionClass mBottomBar;

    private TextView mLeftScoreText;
    private TextView mRightScoreText;

    // Windows constants
    private static int width, height;
    private static float xGU, yGU; // game units


    PingGameClass(ImageView ball, ImageView playerLeft, ImageView playerRight, ImageView topBar, ImageView bottomBar, TextView leftScoreText,TextView rightScoreText) {
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

        leftScore = 0;
        rightScore = 0;
        updateScore();


    }


    private synchronized void updateScore() {
        mLeftScoreText.setText(String.format(Locale.US, "%d",leftScore));
        mRightScoreText.setText(String.format(Locale.US, "%d",rightScore));
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

        int result1 = mBall.nextYPosition();
        int result2 = mBall.nextXPosition(mPlayerLeft.getYPosition());



        if (result2 == GOAL_MOVEMENT) {
            rightGoal();
            reset();
            return result2;
        } else {
            mBall.setNextPosition();
            if (result2 == BOUNCE_PLAYER) return result2;
            return result1;
        }

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
        private ImageView mView;
        private int entity;
        private float nextX, nextY;

        PositionClass(ImageView view, int entity){
            xPosition = 0;
            yPosition = 0;
            xDelta = 0;
            yDelta = 0;
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
                    setXdelta(BALL_GPF);
                    if ((Math.random() * 2) > 1) {
                        setYdelta(0.60f);
                    } else {
                        setYdelta(-0.60f);
                    }
                default:
                    break;
            }


        }

        private float getXPosition() { return xPosition; }

        private float getYPosition() { return yPosition; }

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
        }

        private void setYdelta(float y) {
            yDelta = y;
        }

        private void setDelta(float x, float y) {
            xDelta = x;
            yDelta = y;
        }

        private void resizeImage(int xSize, int ySize) {
            mView.setLayoutParams(new RelativeLayout.LayoutParams(xSize,ySize));
            setImage();
        }

        private void setImage() {
            mView.setX(((xPosition - xSize/2.0f )* xGU));
            mView.setY(((yPosition - ySize/2.0f) * yGU));
        }


        private int nextYPosition() {
            if ((yPosition + yDelta) > MAX_LIMIT_Y_BALL) {   // checks limits after yDelta
                nextY =  2 * MAX_LIMIT_Y_BALL - yDelta - yPosition;
                yDelta = -yDelta;
                return BOUNCE_WALL;
            } else if ((yPosition + yDelta) < MIN_LIMIT_Y_BALL) {
                nextY = 2 * MIN_LIMIT_Y_BALL - yDelta - yPosition;
                yDelta = -yDelta;
                return BOUNCE_WALL;
            } else {
                nextY = yPosition + yDelta;
                return OK_MOVEMENT;
            }
        }

        private int nextXPosition(float playerYposition) {
            if (xPosition < MIN_LIMIT_X_BALL) {
                nextX = xPosition + xDelta;
                return GOAL_MOVEMENT;
            } else if ((xPosition + xDelta) < MIN_LIMIT_X_BALL) {
                if ((playerYposition-(HEIGHT_PLAYER/2)-(SIZE_BALL/2)) >= yPosition) {
                    nextX = xPosition + xDelta;
                    return GOAL_MOVEMENT;
                } else if ((playerYposition-(HEIGHT_PLAYER/4)-(SIZE_BALL/2)) >= yPosition)  { // Up area
                    if (yDelta > 0) {
                        yDelta /= 2;
                    }
                    else {
                        yDelta *= 2;
                        if (yDelta > (3 * BALL_GPF))
                            yDelta = 3 * BALL_GPF;
                    }
                } else if ((playerYposition+(HEIGHT_PLAYER/4)+(SIZE_BALL/2)) > yPosition) { // Middle area
                } else if ((playerYposition+(HEIGHT_PLAYER/2)+(SIZE_BALL/2)) > yPosition) { // Lower area
                    if (yDelta > 0) {
                        yDelta *= 2;
                        if (yDelta > (3 * BALL_GPF))
                            yDelta = 3 * BALL_GPF;
                    }
                    else {
                        yDelta /= 2;
                    }
                } else {
                    nextX = xPosition + xDelta;
                    return GOAL_MOVEMENT;
                }
                nextX = 2 * MIN_LIMIT_X_BALL - xDelta - xPosition;
                xDelta = -xDelta;
                return BOUNCE_PLAYER;
            } else if ((xPosition + xDelta) <= MAX_LIMIT_X_BALL) {
                nextX = xPosition + xDelta;
                return OK_MOVEMENT;
            } else {
                nextX = MAX_LIMIT_X_BALL;
                return OK_MOVEMENT;
            }
        }

    }

}
