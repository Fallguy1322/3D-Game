package com.fallguy1322.game3d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import android.view.KeyEvent;

public class Game3D extends ApplicationAdapter {
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private Model groundModel;
    private Model playerModel;
    private ModelInstance ground;
    private ModelInstance player;

    private final Vector3 playerPosition = new Vector3(0, 0.6f, 0);
    private final Vector3 cameraTarget = new Vector3();
    private float verticalVelocity = 0f;
    private boolean grounded = true;

    // Volatile because Android dispatches controller events on the UI thread.
    private volatile boolean up, down, left, right;
    private volatile float stickX, stickY;
    private final float moveSpeed = 4.0f;
    private static final float DEAD_ZONE = 0.18f;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.1f;
        camera.far = 100f;

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.75f, 0.75f, 0.78f, 1f));
        environment.add(new DirectionalLight().set(0.9f, 0.9f, 0.85f, -1f, -0.8f, -0.4f));

        ModelBuilder builder = new ModelBuilder();
        groundModel = builder.createBox(40f, 0.2f, 40f,
            new Material(ColorAttribute.createDiffuse(new Color(0.22f, 0.48f, 0.27f, 1f))),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        ground = new ModelInstance(groundModel);
        ground.transform.setToTranslation(0, -0.1f, 0);

        playerModel = builder.createBox(0.8f, 1.2f, 0.8f,
            new Material(ColorAttribute.createDiffuse(new Color(0.15f, 0.55f, 1f, 1f))),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        player = new ModelInstance(playerModel);
        player.transform.setToTranslation(playerPosition);

        // Keyboard and TV remote fallback, alongside native PS3 gamepad events.
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                setLibGdxKey(keycode, true);
                if ((keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE
                        || keycode == Input.Keys.CENTER) && grounded) {
                    jump();
                }
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                setLibGdxKey(keycode, false);
                return true;
            }
        });
    }

    private void setLibGdxKey(int keycode, boolean pressed) {
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) up = pressed;
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) down = pressed;
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) left = pressed;
        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) right = pressed;
    }

    public void setGamepadAxes(float x, float y) {
        stickX = Math.abs(x) < DEAD_ZONE ? 0f : x;
        stickY = Math.abs(y) < DEAD_ZONE ? 0f : y;
    }

    public void handleAndroidKey(int keyCode, boolean pressed) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP: up = pressed; break;
            case KeyEvent.KEYCODE_DPAD_DOWN: down = pressed; break;
            case KeyEvent.KEYCODE_DPAD_LEFT: left = pressed; break;
            case KeyEvent.KEYCODE_DPAD_RIGHT: right = pressed; break;
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_BUTTON_1:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:
                if (pressed) jump();
                break;
            case KeyEvent.KEYCODE_BUTTON_B:
                // Circle/B is a convenient alternative jump button.
                if (pressed) jump();
                break;
            default:
                break;
        }
    }

    private void jump() {
        if (grounded) {
            verticalVelocity = 7f;
            grounded = false;
        }
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        float dx = (right ? 1f : 0f) - (left ? 1f : 0f) + stickX;
        float dz = (down ? 1f : 0f) - (up ? 1f : 0f) + stickY;
        dx = Math.max(-1f, Math.min(1f, dx));
        dz = Math.max(-1f, Math.min(1f, dz));

        if (dx != 0 || dz != 0) {
            float length = (float)Math.sqrt(dx * dx + dz * dz);
            if (length > 1f) {
                dx /= length;
                dz /= length;
            }
            playerPosition.x += dx * moveSpeed * delta;
            playerPosition.z += dz * moveSpeed * delta;
        }

        if (!grounded) {
            verticalVelocity -= 18f * delta;
            playerPosition.y += verticalVelocity * delta;
            if (playerPosition.y <= 0.6f) {
                playerPosition.y = 0.6f;
                verticalVelocity = 0f;
                grounded = true;
            }
        }

        player.transform.setToTranslation(playerPosition);
        cameraTarget.set(playerPosition.x, playerPosition.y + 0.2f, playerPosition.z);
        camera.position.set(playerPosition.x, playerPosition.y + 4.5f, playerPosition.z + 7.5f);
        camera.lookAt(cameraTarget.x, cameraTarget.y, cameraTarget.z);
        camera.up.set(Vector3.Y);
        camera.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.42f, 0.68f, 0.9f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        modelBatch.begin(camera);
        modelBatch.render(ground, environment);
        modelBatch.render(player, environment);
        modelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null && height > 0) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }
    }

    @Override
    public void dispose() {
        if (modelBatch != null) modelBatch.dispose();
        if (groundModel != null) groundModel.dispose();
        if (playerModel != null) playerModel.dispose();
    }
}
