/*
 * An optimized LineSegment which only uses the end point with the expectation
 * that a collection of points will represent a continuous set of line segments.
 *
 * Created on Nov 9, 2013
 */

/*
    Copywrite 2013 Will Winder

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

/**
 *
 * @author wwinder
 */
final public class PointSegment {
    private int toolhead = 0; //DEFAULT TOOLHEAD ASSUMED TO BE 0!
    private float speed;
    private Point3f point;
    
    // Line properties
    private boolean isMetric = true;
    private boolean isZMovement = false;
    private boolean isArc = false;
    private boolean isFastTraverse = false;
    private int lineNumber;
    private ArcProperties arcProperties = null;

    private class ArcProperties {
        public boolean isClockwise;
        public float radius = 0;
        public Point3f center = null;
    }
    
    public PointSegment() {
        this.lineNumber = -1;
    }
    
    public PointSegment(PointSegment ps) {
        this(ps.point(), ps.getLineNumber());
    
        this.setToolHead(ps.toolhead);
        this.setSpeed(ps.speed);
        this.setIsMetric(ps.isMetric);
        this.setIsZMovement(ps.isZMovement);
        this.setIsFastTraverse(ps.isFastTraverse);

        if (ps.isArc) {
            this.setArcCenter(ps.center());
            this.setRadius(ps.getRadius());
            this.setIsClockwise(ps.isClockwise());
        }
    }
    
    public PointSegment(final Point3f b, final int num) {
        this();
        this.point = new Point3f(b);
        this.lineNumber = num;
    }
    
    public PointSegment(final Point3f point, final int num, final Point3f center, final float radius, final boolean clockwise) {
        this(point, num);
        this.isArc = true;
        this.arcProperties = new ArcProperties();
        this.arcProperties.center = new Point3f(center);
        this.arcProperties.radius = radius;
        this.arcProperties.isClockwise = clockwise;
    }
    
    public void setPoint(final Point3f point) {
        this.point = new Point3f(point);
    }

    public Point3f point()
    {
        return point;
    }
    
    public float[] points()
    {
        float[] points = {point.x, point.y, point.z};
        return points;
    }
    
    public void setToolHead(final int head) {
        this.toolhead = head;
    }
    
    public int getToolhead()
    {
        return toolhead;
    }
    
    public void setLineNumber(final int num) {
        this.lineNumber = num;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setSpeed(final float s) {
        this.speed = s;
    }
    
    public float getSpeed()
    {
        return speed;
    }
    
    public void setIsZMovement(final boolean isZ) {
        this.isZMovement = isZ;
    }
    
    public boolean isZMovement() {
        return isZMovement;
    }
    
    public void setIsMetric(final boolean isMetric) {
        this.isMetric = isMetric;
    }
    
    public boolean isMetric() {
        return isMetric;
    }
    
    public void setIsArc(final boolean isA) {
        this.isArc = isA;
    }
    
    public boolean isArc() {
        return isArc;
    }
    
    public void setIsFastTraverse(final boolean isF) {
        this.isFastTraverse = isF;
    }
    
    public boolean isFastTraverse() {
        return this.isFastTraverse;
    }
    
    // Arc properties.
    
    public void setArcCenter(final Point3f center) {
        if (this.arcProperties == null) {
            this.arcProperties = new ArcProperties();
        }
        
        this.arcProperties.center = new Point3f(center);
        this.setIsArc(true);
    }
    
    public float[] centerPoints()
    {
        if (this.arcProperties != null && this.arcProperties.center != null) {
            float[] points = {arcProperties.center.x, arcProperties.center.y, arcProperties.center.z};
            return points;
        }
        return null;
    }

    
    public Point3f center() {
        if (this.arcProperties != null && this.arcProperties.center != null) {
            return this.arcProperties.center;
        }
        return null;
    }
        
    public void setIsClockwise(final boolean clockwise) {
        if (this.arcProperties == null) {
            this.arcProperties = new ArcProperties();
        }

        this.arcProperties.isClockwise = clockwise;
    }
    
    public boolean isClockwise() {
        if (this.arcProperties != null && this.arcProperties.center != null) {
            return this.arcProperties.isClockwise;
        }
        return false;
    }
    
    public void setRadius(final float rad) {
        if (this.arcProperties == null) {
            this.arcProperties = new ArcProperties();
        }

        this.arcProperties.radius = rad;
    }
    
    public float getRadius() {
        if (this.arcProperties != null && this.arcProperties.center != null) {
            return this.arcProperties.radius;
        }
        return 0;
    }
    
    public void convertToMetric() {
        if (this.isMetric) {
            return;
        }

        this.isMetric = true;
        this.point.scale(25.4f);

        if (this.isArc && this.arcProperties != null) {
            this.arcProperties.center.scale(25.4f);
            this.arcProperties.radius *= 25.4;
        }
    }
}