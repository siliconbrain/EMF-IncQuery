/*******************************************************************************
 * Copyright (c) 2010-2012, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi- initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.tooling.ui.retevis.theme;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.services.IDisposable;

import com.google.common.base.Preconditions;

public class ColorTheme implements IDisposable {

    private Color[] nodeColors;
    private Color[] textColors;
    private int size;

    public ColorTheme(Display display) {
        size = 3;
        nodeColors = new Color[size];
        textColors = new Color[size];

        nodeColors[0] = new Color(display, 255, 255, 255);
        textColors[0] = new Color(display, 0, 0, 0);

        nodeColors[1] = new Color(display, 55, 112, 231);
        textColors[1] = new Color(display, 255, 255, 255);

        nodeColors[2] = new Color(display, 127, 0, 77);
        textColors[2] = new Color(display, 255, 255, 255);
    }

    public Color getNodeColor(int id) {
        Preconditions.checkElementIndex(id, size);
        return nodeColors[id];
    }

    public Color getTextColor(int id) {
        Preconditions.checkElementIndex(id, size);
        return textColors[id];
    }

    @Override
    public void dispose() {
        for (int i = 0; i < size; i++) {
            if (nodeColors[i] != null) {
                nodeColors[i].dispose();
            }
            if (textColors[i] != null) {
                textColors[i].dispose();
            }
        }
    }

}
