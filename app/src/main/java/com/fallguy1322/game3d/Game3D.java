package com.fallguy1322.game3d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
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
import com.badlogic.gdx.InputAdapter;
import java.util.ArrayList;
import java.util.List;

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
    private boolean up, down, left, right;
    private final float moveSpeed = 4.0f;

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

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                setKey(keycode, true);
                if ((keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE || keycode == Input.Keys.CENTER) && grounded) {
                    verticalVelocity = 7f;
                    grounded = false;
                }
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                setKey(keycode, false);
                return true;
            }

            private void setKey(int keycode, boolean value) {
                if (keycode == Input.Keys.UP || keycode == Input.Keys.W) up = value;
                if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) down = value;
                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) left = value;
                if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) right = value;
            }
        });
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        float dx = (right ? 1f : 0f) - (left ? 1f : 0f);
        float dz = (down ? 1f : 0f) - (up ? 1f : 0f);
        if (dx != 0 || dz != 0) {
            float length = (float)Math.sqrt(dx * dx + dz * dz);
            playerPosition.x += dx / length * moveSpeed * delta;
            playerPosition.z += dz / length * moveSpeed * delta;
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
