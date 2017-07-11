/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.ac.kaist.mapping.mapping;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.util.List;

/**
 * Draws up to four other drawables.
 */
public class MultiDrawable extends Drawable {

  private final List<Drawable> drawables;

  public MultiDrawable(List<Drawable> drawables) {
    this.drawables = drawables;
  }

  @Override
  public void draw(Canvas canvas) {
    if (drawables.size() == 1) {
      drawables.get(0).draw(canvas);
      return;
    }
    int width = getBounds().width();
    int height = getBounds().height();

    canvas.save();
    canvas.clipRect(0, 0, width, height);

    if (drawables.size() == 2 || drawables.size() == 3) {
      // Paint left half
      canvas.save();
      canvas.clipRect(0, 0, width / 2, height);
      canvas.translate(-width / 4, 0);
      drawables.get(0).draw(canvas);
      canvas.restore();
    }
    if (drawables.size() == 2) {
      // Paint right half
      canvas.save();
      canvas.clipRect(width / 2, 0, width, height);
      canvas.translate(width / 4, 0);
      drawables.get(1).draw(canvas);
      canvas.restore();
    } else {
      // Paint top right
      canvas.save();
      canvas.scale(.5f, .5f);
      canvas.translate(width, 0);
      drawables.get(1).draw(canvas);

      // Paint bottom right
      canvas.translate(0, height);
      drawables.get(2).draw(canvas);
      canvas.restore();
    }

    if (drawables.size() >= 4) {
      // Paint top left
      canvas.save();
      canvas.scale(.5f, .5f);
      drawables.get(0).draw(canvas);

      // Paint bottom left
      canvas.translate(0, height);
      drawables.get(3).draw(canvas);
      canvas.restore();
    }

    canvas.restore();
  }

  @Override
  public void setAlpha(int i) {

  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {

  }

  @Override
  public int getOpacity() {
    return PixelFormat.UNKNOWN;
  }
}