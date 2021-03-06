/*
 * Gcode parser that creates an array of line segments which can be drawn.
 *
 * Created on Jan 29, 2013
 */

/*
    Copywrite 2013 Noah Levy, William Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tesladocet.threedprinting.gcode;

import java.util.ArrayList;
import java.util.List;

public class GcodeViewParse {

    // false = incremental; true = absolute
    boolean absoluteMode = true;
    static boolean absoluteIJK = false;

    // Parsed object
    private Point3f min;
    private Point3f max;
    private List<LineSegment> lines;
    
    // Parsing state.
    private Point3f lastPoint;
    private int currentLine = 0;    // for assigning line numbers to segments.
      
    // Debug
    private boolean debug = true;
    
    public GcodeViewParse()
    {
        min = new Point3f();
        max = new Point3f();
        lastPoint = new Point3f();
        lines = new ArrayList<LineSegment>();
    }

    public Point3f getMinimumExtremes()
    {
        return min;
    }
    
    public Point3f getMaximumExtremes()
    {
        return max;
    }
    
    private void testExtremes(final Point3f p3d)
    {
        testExtremes(p3d.x, p3d.y, p3d.z);
    }
    
    private void testExtremes(float x, float y, float z)
    {
        if(x < min.x) {
            min.x = x;
        }
        if(x > max.x) {
            max.x = x;
        }
        if(y < min.y) {
            min.y = y;
        }
        if(y > max.y) {
            max.y = y;
        }
        if(z < min.z) {
            min.z = z;
        }
        if(z > max.z) {
            max.z = z;
        }
    }
    
    public List<LineSegment> toObjRedux(List<String> gcode, float arcSegmentLength) {
        GcodeParser gp = new GcodeParser();
        for (String s : gcode) {
            gp.addCommand(s);
        }
        
        return getLinesFromParser(gp, arcSegmentLength);
    }
    
    private List<LineSegment> getLinesFromParser(GcodeParser gp, float arcSegmentLength) {
        List<PointSegment> psl = gp.getPointSegmentList();
        // For a line segment list ALL arcs must be converted to lines.
        float minArcLength = 0;

        Point3f start = null;
        Point3f end = null;
        LineSegment ls;
        int num = 0;
        for (PointSegment segment : psl) {
            PointSegment ps = segment;
            ps.convertToMetric();
            
            end = ps.point();

            // start is null for the first iteration.
            if (start != null) {
                // Expand arc for graphics.
                if (ps.isArc()) {
                    List<Point3f> points =
                        GcodePreprocessorUtils.generatePointsAlongArcBDring(
                        start, end, ps.center(), ps.isClockwise(), ps.getRadius(), minArcLength, arcSegmentLength);
                    // Create line segments from points.
                    if (points != null) {
                        Point3f startPoint = start;
                        for (Point3f nextPoint : points) {
                            ls = new LineSegment(startPoint, nextPoint, num);
                            ls.setIsArc(ps.isArc());
                            ls.setIsFastTraverse(ps.isFastTraverse());
                            ls.setIsZMovement(ps.isZMovement());
                            this.testExtremes(nextPoint);
                            lines.add(ls);
                            startPoint = nextPoint;
                        }
                    }
                // Line
                } else {
                    ls = new LineSegment(start, end, num++);
                    ls.setIsArc(ps.isArc());
                    ls.setIsFastTraverse(ps.isFastTraverse());
                    ls.setIsZMovement(ps.isZMovement());
                    this.testExtremes(end);
                    lines.add(ls);
                }
            }
            start = end;
        }
        
        return lines;
    }
}