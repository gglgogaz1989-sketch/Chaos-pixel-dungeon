
/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Chaos Pixel Dungeon - Dynamic GLSL Lighting
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.watabou.noosa;

import com.badlogic.gdx.Gdx;
import com.watabou.glscripts.Script;
import com.watabou.glwrap.Attribute;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Uniform;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.utils.DeviceCompat;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class NoosaScript extends Script {

	public Uniform uCamera;
	public Uniform uModel;
	public Uniform uTex;
	public Uniform uColorM;
	public Uniform uColorA;

	// Dynamic lighting uniforms
	public Uniform uLightPos;
	public Uniform uLightRadius;
	public Uniform uLightStrength;
	public Uniform uAmbient;

	public Attribute aXY;
	public Attribute aUV;

	private Camera lastCamera;

	// Shared dynamic light settings.
	// Lighting is disabled by default to preserve the original appearance.
	private static float lightX = 0f;
	private static float lightY = 0f;
	private static float lightRadius = 150f;
	private static float lightStrength = 0f;
	private static float ambientLight = 1f;

	public NoosaScript() {

		super();
		compile(shader());

		uCamera = uniform("uCamera");
		uModel = uniform("uModel");
		uTex = uniform("uTex");
		uColorM = uniform("uColorM");
		uColorA = uniform("uColorA");

		uLightPos = uniform("uLightPos");
		uLightRadius = uniform("uLightRadius");
		uLightStrength = uniform("uLightStrength");
		uAmbient = uniform("uAmbient");

		aXY = attribute("aXYZW");
		aUV = attribute("aUV");

		Quad.setupIndices();
		Quad.bindIndices();
	}

	@Override
	public void use() {

		super.use();

		aXY.enable();
		aUV.enable();
	}

	public void drawElements(FloatBuffer vertices, ShortBuffer indices, int size) {

		((Buffer) vertices).position(0);
		aXY.vertexPointer(2, 4, vertices);

		((Buffer) vertices).position(2);
		aUV.vertexPointer(2, 4, vertices);

		Quad.releaseIndices();
		Gdx.gl20.glDrawElements(
				Gdx.gl20.GL_TRIANGLES,
				size,
				Gdx.gl20.GL_UNSIGNED_SHORT,
				indices
		);
		Quad.bindIndices();
	}

	public void drawQuad(FloatBuffer vertices) {

		((Buffer) vertices).position(0);
		aXY.vertexPointer(2, 4, vertices);

		((Buffer) vertices).position(2);
		aUV.vertexPointer(2, 4, vertices);

		Gdx.gl20.glDrawElements(
				Gdx.gl20.GL_TRIANGLES,
				Quad.SIZE,
				Gdx.gl20.GL_UNSIGNED_SHORT,
				0
		);
	}

	public void drawQuad(Vertexbuffer buffer) {

		buffer.updateGLData();
		buffer.bind();

		aXY.vertexBuffer(2, 4, 0);
		aUV.vertexBuffer(2, 4, 2);

		buffer.release();

		Gdx.gl20.glDrawElements(
				Gdx.gl20.GL_TRIANGLES,
				Quad.SIZE,
				Gdx.gl20.GL_UNSIGNED_SHORT,
				0
		);
	}

	public void drawQuadSet(FloatBuffer vertices, int size) {

		if (size == 0) {
			return;
		}

		((Buffer) vertices).position(0);
		aXY.vertexPointer(2, 4, vertices);

		((Buffer) vertices).position(2);
		aUV.vertexPointer(2, 4, vertices);

		Gdx.gl20.glDrawElements(
				Gdx.gl20.GL_TRIANGLES,
				Quad.SIZE * size,
				Gdx.gl20.GL_UNSIGNED_SHORT,
				0
		);
	}

	public void drawQuadSet(Vertexbuffer buffer, int length, int offset) {

		if (length == 0) {
			return;
		}

		buffer.updateGLData();
		buffer.bind();

		aXY.vertexBuffer(2, 4, 0);
		aUV.vertexBuffer(2, 4, 2);

		buffer.release();

		Gdx.gl20.glDrawElements(
				Gdx.gl20.GL_TRIANGLES,
				Quad.SIZE * length,
				Gdx.gl20.GL_UNSIGNED_SHORT,
				Quad.SIZE * Short.SIZE / 8 * offset
		);
	}

	/**
	 * Sets the position and properties of the global dynamic light.
	 *
	 * Coordinates must use the same world-coordinate system as Visual.x/y.
	 *
	 * @param x light X position
	 * @param y light Y position
	 * @param radius light radius in world units; must be greater than zero
	 * @param strength additional illumination, from 0 to 1
	 * @param ambient minimum illumination, from 0 to 1
	 */
	public static void setDynamicLight(
			float x,
			float y,
			float radius,
			float strength,
			float ambient
	) {
		lightX = x;
		lightY = y;
		lightRadius = Math.max(1f, radius);
		lightStrength = clamp01(strength);
		ambientLight = clamp01(ambient);
	}

	/**
	 * Disables dynamic lighting and restores the original brightness.
	 */
	public static void disableDynamicLight() {
		lightStrength = 0f;
		ambientLight = 1f;
	}

	private static float clamp01(float value) {
		return Math.max(0f, Math.min(1f, value));
	}

	/**
	 * Applies the original color transformation and updates the light uniforms.
	 */
	public void lighting(
			float rm, float gm, float bm, float am,
			float ra, float ga, float ba, float aa
	) {
		uColorM.value4f(rm, gm, bm, am);
		uColorA.value4f(ra, ga, ba, aa);

		uLightPos.value2f(lightX, lightY);
		uLightRadius.value1f(lightRadius);
		uLightStrength.value1f(lightStrength);
		uAmbient.value1f(ambientLight);
	}

	public void resetCamera() {
		lastCamera = null;
	}

	public void camera(Camera camera) {

		if (camera == null) {
			camera = Camera.main;
		}

		if (camera != lastCamera && camera.matrix != null) {
			lastCamera = camera;
			uCamera.valueM4(camera.matrix);

			if (!camera.fullScreen) {

				Gdx.gl20.glEnable(Gdx.gl20.GL_SCISSOR_TEST);

				// Fixes pixel scaling issues on some high-DPI displays.
				float xScale = DeviceCompat.getRealPixelScaleX();
				float yScale = DeviceCompat.getRealPixelScaleY();

				Gdx.gl20.glScissor(
						Math.round(camera.x * xScale),
						Math.round(
								(Game.height - camera.screenHeight - camera.y)
										* yScale
						),
						Math.round(camera.screenWidth * xScale),
						Math.round(camera.screenHeight * yScale)
				);

			} else {
				Gdx.gl20.glDisable(Gdx.gl20.GL_SCISSOR_TEST);
			}
		}
	}

	public static NoosaScript get() {
		return Script.use(NoosaScript.class);
	}

	protected String shader() {
		return SHADER;
	}

	private static final String SHADER =

			// Vertex shader
			"uniform mat4 uCamera;\n" +
			"uniform mat4 uModel;\n" +
			"attribute vec4 aXYZW;\n" +
			"attribute vec2 aUV;\n" +
			"varying vec2 vUV;\n" +
			"varying vec2 vWorldPos;\n" +

			"void main() {\n" +
			"  vec4 worldPosition = uModel * aXYZW;\n" +
			"  gl_Position = uCamera * worldPosition;\n" +
			"  vUV = aUV;\n" +
			"  vWorldPos = worldPosition.xy;\n" +
			"}\n" +

			"//\n" +

			// Fragment shader
			"#ifdef GL_ES\n" +
			"  precision mediump float;\n" +
			"#endif\n" +

			"varying vec2 vUV;\n" +
			"varying vec2 vWorldPos;\n" +

			"uniform sampler2D uTex;\n" +
			"uniform vec4 uColorM;\n" +
			"uniform vec4 uColorA;\n" +

			"uniform vec2 uLightPos;\n" +
			"uniform float uLightRadius;\n" +
			"uniform float uLightStrength;\n" +
			"uniform float uAmbient;\n" +

			"void main() {\n" +
			"  vec4 color = texture2D(uTex, vUV) * uColorM + uColorA;\n" +

			"  float distToLight = distance(vWorldPos, uLightPos);\n" +
			"  float falloff = 1.0 - smoothstep(\n" +
			"      uLightRadius * 0.2,\n" +
			"      uLightRadius,\n" +
			"      distToLight\n" +
			"  );\n" +

			"  falloff = falloff * falloff;\n" +
			"  float illumination = clamp(\n" +
			"      uAmbient + uLightStrength * falloff,\n" +
			"      0.0,\n" +
			"      1.0\n" +
			"  );\n" +

			"  color.rgb *= illumination;\n" +
			"  gl_FragColor = color;\n" +
			"}\n";
}
