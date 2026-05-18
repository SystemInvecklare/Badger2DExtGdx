package com.github.systeminvecklare.badger.impl.gdx.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;

public class TextureSaver {
	public static void saveTexture(FileHandle file, Texture texture) {
	    int w = texture.getWidth();
	    int h = texture.getHeight();

	    FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
	    SpriteBatch batch = new SpriteBatch();
	    
	    batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
	    
	    fbo.begin();

	    Gdx.gl.glViewport(0, 0, w, h);

	    Gdx.gl.glClearColor(0, 0, 0, 0);
	    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	    batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, w, h));

	    batch.begin();

	    batch.draw(texture, 0, 0, w, h);

	    batch.end();

	    Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, w, h);

	    fbo.end();

	    Pixmap flipped = new Pixmap(w, h, pixmap.getFormat());

	    for (int y = 0; y < h; y++) {
	        for (int x = 0; x < w; x++) {
	            flipped.drawPixel(x, h - 1 - y, pixmap.getPixel(x, y));
	        }
	    }

	    pixmap.dispose();

	    PixmapIO.writePNG(file, flipped);
	    flipped.dispose();

	    batch.dispose();
	    fbo.dispose();
	}
}
