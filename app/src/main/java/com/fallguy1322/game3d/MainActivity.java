package com.fallguy1322.game3d;

import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication {
    private Game3D game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        game = new Game3D();
        initialize(game, config);
    }

    // Read analog sticks directly from Android so this also works without extra controller libraries.
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (game != null
                && (event.isFromSource(InputDevice.SOURCE_JOYSTICK)
                || event.isFromSource(InputDevice.SOURCE_GAMEPAD))
                && event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getAxisValue(MotionEvent.AXIS_X);
            float y = event.getAxisValue(MotionEvent.AXIS_Y);

            // Some controllers expose the left stick as HAT axes instead.
            float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
            float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
            if (Math.abs(x) < 0.12f && Math.abs(hatX) > 0.12f) x = hatX;
            if (Math.abs(y) < 0.12f && Math.abs(hatY) > 0.12f) y = hatY;

            game.setGamepadAxes(x, y);
            return true;
        }
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (game != null && isGameControlKey(event.getKeyCode())) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                game.handleAndroidKey(event.getKeyCode(), true);
                return true;
            }
            if (event.getAction() == KeyEvent.ACTION_UP) {
                game.handleAndroidKey(event.getKeyCode(), false);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean isGameControlKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_BUTTON_B:
            case KeyEvent.KEYCODE_BUTTON_X:
            case KeyEvent.KEYCODE_BUTTON_Y:
            case KeyEvent.KEYCODE_BUTTON_START:
            case KeyEvent.KEYCODE_BUTTON_SELECT:
            case KeyEvent.KEYCODE_BUTTON_1:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:
                return true;
            default:
                return false;
        }
    }
}
