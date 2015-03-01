/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.worldviewer.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import org.terasology.math.Rect2i;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Edge;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.GraphFacet;
import org.terasology.polyworld.voronoi.Region;
import org.terasology.polyworld.voronoi.Triangle;
import org.terasology.world.generation.WorldFacet;

import com.google.common.math.DoubleMath;

/**
 * Draws the generated graph on a AWT graphics instance
 * @author Martin Steiger
 */
public class GraphFacetLayer extends AbstractFacetLayer {

    private boolean showEdges = true;
    private boolean showBounds = true;
    private boolean showCorners = true;
    private boolean showSites = true;
    private boolean showTris;

    @Override
    public Class<? extends WorldFacet> getFacetClass() {
        return GraphFacet.class;
    }

    @Override
    public void render(BufferedImage img, org.terasology.world.generation.Region region) {
        GraphFacet graphFacet = region.getFacet(GraphFacet.class);

        Graphics2D g = img.createGraphics();
        int dx = region.getRegion().minX();
        int dy = region.getRegion().minZ();
        g.translate(-dx, -dy);
        for (Graph graph : graphFacet.getAllGraphs()) {
            if (showEdges) {
                drawEdges(g, graph);
            }

            if (showTris) {
                drawTriangles(g, graph);
            }

            if (showCorners) {
                drawCorners(g, graph);
            }

            if (showSites) {
                drawSites(g, graph);
            }

            if (showBounds) {
                drawBounds(g, graph);
            }
        }

        g.dispose();
    }

    @Override
    public String getWorldText(org.terasology.world.generation.Region region, int wx, int wy) {
        GraphFacet graphFacet = region.getFacet(GraphFacet.class);
        for (Graph graph : graphFacet.getAllGraphs()) {
            if (graph.getBounds().contains(wx, wy)) {
                return String.format("%d regs, %d corners, %d edges",
                        graph.getRegions().size(), graph.getCorners().size(), graph.getEdges().size());
            }
        }
        return "";
    }

    public boolean isShowEdges() {
        return showEdges;
    }

    public void setShowEdges(boolean showEdges) {
        if (this.showEdges != showEdges) {
            this.showEdges = showEdges;
            notifyObservers();
        }
    }

    public boolean isShowBounds() {
        return showBounds;
    }

    public void setShowBounds(boolean showBounds) {
        if (this.showBounds != showBounds) {
            this.showBounds = showBounds;
            notifyObservers();
        }
    }

    public boolean isShowCorners() {
        return showCorners;
    }

    public void setShowCorners(boolean showCorners) {
        if (this.showCorners != showCorners) {
            this.showCorners = showCorners;
            notifyObservers();
        }
    }

    public boolean isShowSites() {
        return showSites;
    }

    public void setShowSites(boolean showSites) {
        if (this.showSites != showSites) {
            this.showSites = showSites;
            notifyObservers();
        }
    }

    public boolean isShowTris() {
        return showTris;
    }

    public void setShowTris(boolean showTris) {
        if (this.showTris != showTris) {
            this.showTris = showTris;
            notifyObservers();
        }
    }

    public static void drawEdges(Graphics2D g, Graph graph) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.CYAN);
        for (Edge e : graph.getEdges()) {
            BaseVector2f r0c = e.getCorner0().getLocation();
            BaseVector2f r1c = e.getCorner1().getLocation();
            g.drawLine((int) r0c.getX(), (int) r0c.getY(), (int) r1c.getX(), (int) r1c.getY());
        }
    }

    public static void drawPolys(Graphics2D g, Graph graph, Function<Region, Color> colorFunc) {
        List<Region> regions = graph.getRegions();

        for (final Region reg : regions) {

            Color col = colorFunc.apply(reg);
            Collection<Corner> pts = reg.getCorners();

            int[] xPoints = new int[pts.size()];
            int[] yPoints = new int[pts.size()];

            int i = 0;
            for (Corner corner : pts) {
                xPoints[i] = (int) corner.getLocation().getX();
                yPoints[i] = (int) corner.getLocation().getY();
                i++;
            }

            g.setColor(col);
            g.fillPolygon(xPoints, yPoints, pts.size());
        }
    }

    public static void drawTriangles(Graphics2D g, Graph graph) {
        List<Region> regions = graph.getRegions();

        Random r = new Random(12332434);

        for (final Region reg : regions) {
            for (Triangle t : reg.computeTriangles()) {
                g.setColor(new Color(r.nextInt(0xFFFFFF)));
                drawTriangle(g, t);
            }
        }
    }

    public static void drawTriangle(Graphics2D g, Triangle tri) {

        RoundingMode mode = RoundingMode.HALF_UP;
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        BaseVector2f p0 = tri.getRegion().getCenter();
        BaseVector2f p1 = tri.getCorner1().getLocation();
        BaseVector2f p2 = tri.getCorner2().getLocation();

        xPoints[0] = DoubleMath.roundToInt(p0.getX(), mode);
        yPoints[0] = DoubleMath.roundToInt(p0.getY(), mode);

        xPoints[1] = DoubleMath.roundToInt(p1.getX(), mode);
        yPoints[1] = DoubleMath.roundToInt(p1.getY(), mode);

        xPoints[2] = DoubleMath.roundToInt(p2.getX(), mode);
        yPoints[2] = DoubleMath.roundToInt(p2.getY(), mode);

        g.drawPolygon(xPoints, yPoints, 3);
    }

    public static void drawSites(Graphics2D g, Graph graph) {
        List<Region> centers = graph.getRegions();

        g.setColor(Color.BLACK);
        for (Region regs : centers) {
            Vector2f center = regs.getCenter();
            g.fillOval((int) (center.getX() - 2), (int) (center.getY() - 2), 4, 4);
        }
    }

    public static void drawCorners(Graphics2D g, Graph graph) {
        g.setColor(Color.WHITE);
        for (Corner c : graph.getCorners()) {
            ImmutableVector2f loc = c.getLocation();
            g.fillOval((int) (loc.getX() - 2), (int) (loc.getY() - 2), 4, 4);
        }
    }

    public static void drawBounds(Graphics2D g, Graph graph) {
        Rect2i bounds = graph.getBounds();
        g.setColor(Color.PINK);
        g.drawRect(bounds.minX(), bounds.minY(), bounds.width(), bounds.height());
    }

    public static void fillBounds(Graphics2D g, Graph graph) {
        Rect2i bounds = graph.getBounds();
        g.setColor(Color.MAGENTA);
        g.fillRect(bounds.minX() + 1, bounds.minY() + 1, bounds.width() - 1, bounds.height() - 1);
    }

}
