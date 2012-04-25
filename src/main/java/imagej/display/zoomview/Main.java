/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.display.zoomview;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;

import javax.swing.JFrame;

/**
 *
 * @author Customer
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        final JFrame frame = new JFrame("ImgPanel Test Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SetupDialog setupDialog = new SetupDialog(frame);
        System.out.println("setupDialog filename " + setupDialog.getFileName());

        // this is just a nominal pixel size for fake images
        int dim[] = { 1024, 768 };

        switch (setupDialog.getPixels()) {
            case ACTUAL:
                break;
            case X4:
                dim[0] *= 4;
                dim[1] *= 4;
                break;
            case X16:
                dim[0] *= 16;
                dim[1] *= 16;
                break;
            case MP50:
                dim = new int[] { 7 * 1024, 7 * 1024 };
                break;
            case MP100:
                dim = new int[] { 10 * 1024 , 10 * 1024 };
                break;
            case MP500:
                dim = new int[] { 23 * 1024, 23 * 1024 };
                break;
            case GP1:
                dim = new int[] { 32 * 1024, 32 * 1024 };
                break;
            case GP4:
                dim = new int[] { 64 * 1024, 64 * 1024 };
                break;
        }

        //TODO setting up the zoom viewer, could happen elsewhere

        //TODO the cache should be shared
        TileCache tileCache = new TileCache(setupDialog.getCacheSize());

        //TODO need factory that projects ImgLib images
        ITileFactory factory = new MyTileFactory(new File(setupDialog.getFileName()));

        ZoomTileServer zoomTileServer = new ZoomTileServer();
        zoomTileServer.init(tileCache, factory, dim);

        final ImgZoomPanel imgZoomPanel = new ImgZoomPanel(zoomTileServer);
        frame.setContentPane(imgZoomPanel);
        frame.pack();
        center(frame);
        frame.setVisible(true);
    }


    private static void center(final Window win) {
        final Dimension size = win.getSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = (screen.width - size.width) / 2;
        final int h = (screen.height - size.height) / 2;
        win.setLocation(w, h);
    }
}
