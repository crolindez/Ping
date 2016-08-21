package es.carlosrolindez.ping;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import static java.lang.Math.abs;



public class PingGameClass {

    // PING GAME STATES
    public static final int START = 0x10;
    public static final int GETTING_READY = 0x10;
    public static final int PLAYING = 0x20;
    public static final int GOAL = 0x30;

    // PING ENTITIES
    public static final int PLAYER_LEFT = 0;
    public static final int PLAYER_RIGHT = 1;
    public static final int BALL = 2;
    public static final int TOP_BAR = 3;
    public static final int BOTTOM_BAR = 4;

    // COORDINATES
    public static final int SIZE_BALL = 5;
    public static final int WIDTH_PLAYER = 3;
    public static final int HEIGHT_PLAYER = 25;
    public static final int WIDTH_BAR = 190;
    public static final int HEIGHT_BAR = 1;

    public static final int INIT_X_BALL = 98;
    public static final int INIT_Y_BALL = 48;
    public static final int INIT_X_PLAYER_LEFT = 5;
    public static final int INIT_X_PLAYER_RIGHT = 193;
    public static final int INIT_Y_OFFSET_PLAYER = -10;

    public static final int INIT_X_BAR = 5;
    public static final int INIT_Y_TOP_BAR = 1;
    public static final int INIT_Y_BOTTOM_BAR = 98;

    public static final int MIN_LIMIT_Y_BALL = 2;
    public static final int MAX_LIMIT_Y_BALL = 93;

    public static final int MIN_LIMIT_X_BALL = 9;
    public static final int MAX_LIMIT_X_BALL = 188;


    // Game State
    private int gameState;

    // Entities
    private PlayerClass mPlayerLeft;
    private PlayerClass mPlayerRight;
    private BallClass mBall;
    private BarClass mTopBar;
    private BarClass mBottomBar;

    // Windows constants
    private static int width, height;
    private static double xGU, yGU; // game units
    private static final int XGU = 200;
    private static final int YGU = 100;

    final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

    PingGameClass(ImageView ball, ImageView playerLeft, ImageView playerRight, ImageView topBar, ImageView bottomBar) {
        gameState = START;
        mPlayerLeft = new PlayerClass(playerLeft, PLAYER_LEFT);
        mPlayerRight = new PlayerClass(playerRight, PLAYER_RIGHT);
        mBall = new BallClass(ball);
        mTopBar = new BarClass(topBar, TOP_BAR);
        mBottomBar = new BarClass(bottomBar, BOTTOM_BAR);

        width = XGU; // default values.  Must be updated by UpdateWindowConstanst
        height = YGU;
        xGU = 1.0;
        yGU = 1.0;

        tone(ToneGenerator.TONE_PROP_BEEP2);

    }

    public void updateWindowConstants(int top, int bottom, int left, int right  ) {

        if(height == (bottom - top))
            return;

        height = (bottom - top);
        width = right - left;
        xGU = width / XGU;
        yGU = height / YGU;

        mBall.resizeImage((int)(SIZE_BALL*xGU), (int) (SIZE_BALL*yGU));
        mPlayerLeft.resizeImage((int)(WIDTH_PLAYER*xGU), (int) (HEIGHT_PLAYER*yGU));
        mPlayerRight.resizeImage((int)(WIDTH_PLAYER*xGU), (int) (HEIGHT_PLAYER*yGU));
        mTopBar.resizeImage((int)(WIDTH_BAR*xGU), (int) (HEIGHT_BAR*yGU));
        mBottomBar.resizeImage((int)(WIDTH_BAR*xGU), (int) (HEIGHT_BAR*yGU));

    }

    public void setState(int newState) {
        gameState = newState;
    }

    public int getState() {
        return gameState;
    }

    private void tone(int tone,int duration) {
        tg.startTone(tone, duration); //ToneGenerator.TONE_PROP_BEEP2,duration
    }

    private void tone(int tone) {
        tg.startTone(tone); //ToneGenerator.TONE_PROP_BEEP2,duration
    }

    public void reset() {
        mBall.resetPosition();
        mPlayerLeft.resetPosition();
        mPlayerRight.resetPosition();
    }

    public void moveBall() {
        float nextX,nextY;

        nextX = mBall.nextBouncingXPosition();
        nextY = mBall.nextBouncingYPosition();
        mBall.setPosition(nextX, nextY);
        if ( (nextX != mBall.nextSequenceXPosition()) || (nextY !=mBall.nextSequenceYPosition()) )
            tone(ToneGenerator.TONE_DTMF_8,50);
    }

    public class PositionClass {

        private float xPosition, yPosition, xDelta, yDelta;
        private ImageView mView;

        PositionClass(ImageView view){
            xPosition = 1;
            yPosition = 1;
            xDelta = 0;
            yDelta = 0;
            mView = view;

        }

        public void resizeImage(int xSize, int ySize) {
            mView.setLayoutParams(new RelativeLayout.LayoutParams(xSize,ySize));
        }

        public void setPosition(float x, float y) {
            xPosition = x;
            yPosition = y;
            mView.setX((float) (xPosition * xGU));
            mView.setY((float) (yPosition * yGU));
        }

        public void setYPosition(float y) {
            yPosition = y;
            mView.setY((float) (yPosition * yGU));
        }

        public void setXPosition(float x) {
            xPosition = x;
            mView.setX((float) (xPosition * xGU));
        }

        public void setXdelta(float x) {
            xDelta = x;
        }

        public void setYdelta(float y) {
            yDelta = y;
        }

        public float nextBouncingYPosition() {
            if (yPosition > MAX_LIMIT_Y_BALL) {             // checks absolute limits
                return (MAX_LIMIT_Y_BALL - abs(yDelta));
            } else if (yPosition < MIN_LIMIT_Y_BALL) {
                return ( MIN_LIMIT_Y_BALL + abs(yDelta));
            } else if ((yPosition + yDelta) > MAX_LIMIT_Y_BALL) {   // checks limits after yDelta
                return ( 2 * MAX_LIMIT_Y_BALL - yDelta - yPosition);
            } else if ((yPosition + yDelta) < MIN_LIMIT_Y_BALL) {
                return ( 2 * MIN_LIMIT_Y_BALL - yDelta - yPosition);
            } else {
                return (yPosition + yDelta);
            }
        }

        public float nextBouncingXPosition() {
            if (xPosition > MAX_LIMIT_X_BALL) {             // checks absolute limits
                return (MAX_LIMIT_X_BALL - abs(xDelta));
            } else if (xPosition < MIN_LIMIT_X_BALL) {
                return ( MIN_LIMIT_X_BALL + abs(xDelta));
            } else if ((xPosition + xDelta) > MAX_LIMIT_X_BALL) {   // checks limits after xDelta
                return ( 2 * MAX_LIMIT_X_BALL - xDelta - xPosition);
            } else if ((xPosition + xDelta) < MIN_LIMIT_X_BALL) {
                return ( 2 * MIN_LIMIT_X_BALL - xDelta - xPosition);
            } else {
                return (xPosition + xDelta);
            }
        }

        public float nextSequenceYPosition() {
            return (yPosition + yDelta);
        }

        public float nextSequenceXPosition() {
            return (xPosition + xDelta);
        }
    }

    public class PlayerClass extends PositionClass {
        public int mPlace;

        PlayerClass(ImageView view,int place) {
            super(view);
            mPlace = place;
            if (mPlace == PLAYER_LEFT) {
                setXPosition(INIT_X_PLAYER_LEFT);
            } else  if (mPlace == PLAYER_RIGHT){
                setXPosition(INIT_X_PLAYER_RIGHT);
            }
            else {
                //// TODO: 21/08/2016
            }
            resetPosition();
        }

        public void resetPosition() {
            setYPosition((float)(height/2 + INIT_Y_OFFSET_PLAYER*yGU));
            setYdelta(0);
        }
    }

    public class BallClass extends PositionClass {
        BallClass(ImageView view) {
            super(view);
            resetPosition();
        }

        public void resetPosition() {
            setPosition(INIT_X_BALL, INIT_Y_BALL);
            setXdelta(1);
            if ((Math.random() * 2) > 1) {
                setYdelta(0.25f);
            } else {
                setYdelta(-0.25f);
            }
        }

    }

    public class BarClass extends PositionClass {
        public int mPlace;

        BarClass(ImageView view,int place) {
            super(view);
            mPlace = place;

            if (mPlace == TOP_BAR) {
                setPosition(INIT_X_BAR, INIT_Y_TOP_BAR);
            } else  if (mPlace == BOTTOM_BAR){
                setPosition(INIT_X_BAR, INIT_Y_BOTTOM_BAR);
            }
            else {
                //// TODO: 21/08/2016
            }
        }

    }

}
