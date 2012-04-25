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
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.MouseInputListener;

/*
 import mpicbg.imglib.container.Img;
import mpicbg.imglib.container.ImgFactory;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.display.ARGBScreenImage;
import mpicbg.imglib.display.RealARGBConverter;
import mpicbg.imglib.display.XYProjector;
import mpicbg.imglib.io.LOCI;
import mpicbg.imglib.type.numeric.ARGBType;
import mpicbg.imglib.type.numeric.real.FloatType;
 */

/**
 *
 * @author Aivar Grislis
 */
public class ImgZoomPanel extends JPanel {
    private int m_levels;
    private int m_level;
    private int m_initLevel;
    private int m_width;
    private int m_height;
    private ZoomTileServer m_zoomTileServer;
    private BufferedImage m_bufferedImage;

    public ImgZoomPanel(ZoomTileServer zoomTileServer) {
        m_zoomTileServer = zoomTileServer;
        m_levels = m_zoomTileServer.getLevels();

        // Get the size of the default screen
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

        //TODO consider returning Dimension also; emphasizes 2D nature here
        for (m_level = 0; m_level < m_levels; ++m_level) {
            Dimension levelDimension = m_zoomTileServer.getDimensionByLevel(m_level);
            System.out.println("levelDim is " + levelDimension);
            if (levelDimension.width < screenDim.width && levelDimension.height < screenDim.height) {
                // TODO what if nothing fits??
                m_width = levelDimension.width;
                m_height = levelDimension.height;
                break;
            }
        }
        m_initLevel = m_level;
System.out.println("width " + m_width + " height " + m_height + "level " + m_level);
        m_bufferedImage = new BufferedImage(m_width, m_height, BufferedImage.TYPE_INT_ARGB);

        SwingWorker worker = new SwingWorker<Void, Void>() {
             public Void doInBackground() {
                 show(m_level, 0, 0);
                 return null;
             }
        };
        worker.execute();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel imageCanvas = new JPanel() {
            @Override
            public void paint(Graphics g) {
                System.out.println("PAINT");
                if (null != m_bufferedImage) {
                    g.drawImage(m_bufferedImage, 0, 0, this);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(m_width, m_height);
            }
        };
        add(imageCanvas);
        MouseInputListener listener = new MouseInputListener() {
            public void mousePressed(MouseEvent e) {
            }
            public void mouseReleased(MouseEvent e) {
            }
            public void mouseEntered(MouseEvent e) {
            }
            public void mouseExited(MouseEvent e) {
            }
            public void mouseClicked(MouseEvent e) {
                System.out.println("e button is " + e.getButton());
                switch (e.getButton()) {
                    case 1:
                        if (m_level > 0) {
                            --m_level;
                            SwingWorker worker = new SwingWorker<Void, Void>() {
                                public Void doInBackground() {
                                    show(m_level, 0, 0);
                                    return null;
                                }
                            };
                            worker.execute();
                        }
                        break;
                    case 3:
                        if (m_level < m_initLevel) {
                            ++m_level;
                            SwingWorker worker = new SwingWorker<Void, Void>() {
                                public Void doInBackground() {
                                    show(m_level, 0, 0);
                                    return null;
                                }
                            };
                            worker.execute();
                        }
                        break;
                }
            }
            public void mouseMoved(MouseEvent e) {
            }
            public void mouseDragged(MouseEvent e) {
            }
        };
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }

    public void addZoomViewServer(ZoomTileServer zoomViewServer) {
        m_zoomTileServer = zoomViewServer;
        show(5, 0, 0);
    }

    public void show(int level, int topLeftTileX, int topLeftTileY) {
        Tile tile;
        int tileX;
        int tileY;
        int outputX;
        int outputY;

        System.out.println("XXXXXXXXX output size " + m_width + " " + m_height);
        int tileCount = 0;
        long time = System.currentTimeMillis();
        for (outputY = 0, tileY = topLeftTileY; outputY < m_height; outputY += 256, ++tileY) {
            for (outputX = 0, tileX = topLeftTileX; outputX < m_width; outputX += 256, ++tileX) {
                System.out.println("tileX " + tileX + " tileY " + tileY);
                System.out.println("get tile level " + m_level + " tileX " + tileX + " tileY " + tileY);
                if (2 == tileX) {
                    System.out.println("LAST TILE");
                }
                if (1 == tileY) {
                    System.out.println("SECOND ROW");
                }
                tile = m_zoomTileServer.getTile(m_level, new int[] { tileX, tileY });
                System.out.println("got tile");
                int rgbArray[] = tile.getARGB();
                int w = 256;
                if (outputX + w > m_width) {
                    w = m_width - outputX;
                }
                int h = 256;
                if (outputY + h > m_height) {
                    h = m_height - outputY;
                }
                int offset = 0;
                int scansize = 256;
                //TODO there could be a partial tile here!
                //TODO how would that affect w, h, offset, scansize???
                System.out.println("calling setRGB " + "outputX " + outputX + " outputY " + outputY +
                        " w " + w + " h " + h + " offset " + offset + " scan size " + scansize);
                m_bufferedImage.setRGB(outputX, outputY, w, h, rgbArray, offset, scansize);
                System.out.println("back from setRGB");
                repaint();
                ++tileCount;
                System.out.println("outputY " + outputY + " height " + m_height);
            }
        }
        System.out.println("XXXXXXX level " + level + " tiles " + tileCount + " time " + (System.currentTimeMillis() - time));
        repaint();
    }

/*
    public void addImage(final String name, final Img<FloatType> img) {
        final ImgData imgData = new ImgData(name, img, this);
        images.add(imgData);
        if (imgData.width > maxWidth) maxWidth = imgData.width;
            if (imgData.height > maxHeight) maxHeight = imgData.height;
                add(new SliderPanel(imgData));
    }
*/
}
