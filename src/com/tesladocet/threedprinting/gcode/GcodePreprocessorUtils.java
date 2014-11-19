/*
 * Collection of useful command preprocessor methods.
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author wwinder
 */
public class GcodePreprocessorUtils {

	static DecimalFormatSymbols dfs = new DecimalFormatSymbols();
	
	static { 
		// TODO let it settable and clever
		dfs.setDecimalSeparator('.');
	}
	
    /**
     * Searches the command string for an 'f' and replaces the speed value 
     * between the 'f' and the next space with a percentage of that speed.
     * In that way all speed values become a ratio of the provided speed 
     * and don't get overridden with just a fixed speed.
     */
    static public String overrideSpeed(String command, float speed) {
        String returnString = command;
        
        // Check if command sets feed speed.
        Pattern pattern = Pattern.compile("F([0-9.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()){
            Double originalFeedRate = Double.parseDouble(matcher.group(1));
            //System.out.println( "Found feed     " + originalFeedRate.toString() );
            Double newFeedRate      = originalFeedRate * speed / 100.0;
            //System.out.println( "Change to feed " + newFeedRate.toString() );
            returnString = matcher.replaceAll( "F" + newFeedRate.toString() );
        }

        return returnString;
    }
    
    /**
     * Removes any comments within parentheses or beginning with a semi-colon.
     */
    static public String removeComment(String command) {
        String newCommand = command;

        // Remove any comments within ( parentheses ) using regex "\([^\(]*\)"
        newCommand = newCommand.replaceAll("\\([^\\(]*\\)", "");

        // Remove any comment beginning with ';' using regex ";.*"
        newCommand = newCommand.replaceAll(";.*", "");

        // Don't send these to the controller.
        if (newCommand.endsWith("%")) {
            newCommand = newCommand.substring(0, newCommand.length()-1);
        }
        
        return newCommand.trim();
    }
    
    /**
     * Searches for a comment in the input string and returns the first match.
     */
    static public String parseComment(String command) {
        String comment = "";

        // REGEX: Find any comment, includes the comment characters:
        //              "(?<=\()[^\(\)]*|(?<=\;)[^;]*"
        //              "(?<=\\()[^\\(\\)]*|(?<=\\;)[^;]*"
        
        Pattern pattern = Pattern.compile("(?<=\\()[^\\(\\)]*|(?<=\\;).*|%");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()){
            comment = matcher.group(0);
        }

        return comment;
    }
    
    static public String truncateDecimals(int length, String command) {
        StringBuilder df = new StringBuilder();
        
        // Build up the decimal formatter.
        df.append("#");
        
        if (length != 0) { df.append("."); }
        for (int i=0; i < length; i++) {
            df.append('#');
        }
        
        DecimalFormat formatter = new DecimalFormat(df.toString(), dfs);
        
        // Build up the regular expression.
        df = new StringBuilder();
        df.append("\\d+\\.\\d");
        for (int i=0; i < length; i++) {
            df.append("\\d");
        }
        df.append('+');
        Pattern pattern = Pattern.compile(df.toString());
        Matcher matcher = pattern.matcher(command);

        // Build up the truncated command.
        Double d;
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            d = Double.parseDouble(matcher.group());
            matcher.appendReplacement(sb, formatter.format(d));
        }
        matcher.appendTail(sb);
        
        // Return new command.
        return sb.toString();
    }
    
    
    static public String removeAllWhitespace(String command) {
        return command.replaceAll("\\s","");
    }
    
    static public List<String> parseCodes(List<String> args, char code) {
        List<String> l = new ArrayList<String>();
        char address = Character.toUpperCase(code);
        
        for (String s : args) {
            if (s.length() > 0 && Character.toUpperCase(s.charAt(0)) == address) {
                l.add(s.substring(1));
            }
        }
        
        return l;
    }
    
    static private Pattern gPattern = Pattern.compile("[Gg]0*(\\d+)");
    static public List<Integer> parseGCodes(String command) {
        Matcher matcher = gPattern.matcher(command);
        List<Integer> codes = new ArrayList<Integer>();
        
        while (matcher.find()) {
            codes.add(Integer.parseInt(matcher.group(1)));
        }
        
        return codes;
    }

    static private Pattern mPattern = Pattern.compile("[Mm]0*(\\d+)");
    static public List<Integer> parseMCodes(String command) {
        Matcher matcher = gPattern.matcher(command);
        List<Integer> codes = new ArrayList<Integer>();
        
        while (matcher.find()) {
            codes.add(Integer.parseInt(matcher.group(1)));
        }
        
        return codes;
    }

    /**
     * Update a point given the arguments of a command.
     */
    static public Point3f updatePointWithCommand(String command, Point3f initial, boolean absoluteMode) {
        List<String> l = GcodePreprocessorUtils.splitCommand(command);
        return updatePointWithCommand(l, initial, absoluteMode);
    }
    
    /**
     * Update a point given the arguments of a command, using a pre-parsed list.
     */
    static public Point3f updatePointWithCommand(List<String> commandArgs, Point3f initial, boolean absoluteMode) {

        float x = parseCoord(commandArgs, 'X');
        float y = parseCoord(commandArgs, 'Y');
        float z = parseCoord(commandArgs, 'Z');

        return updatePointWithCommand(initial, x, y, z, absoluteMode);
    }

    /**
     * Update a point given the new coordinates.
     */
    static public Point3f updatePointWithCommand(Point3f initial, float x, float y, float z, boolean absoluteMode) {

        Point3f newPoint = new Point3f(initial.x, initial.y, initial.z);

        if (absoluteMode) {
            if (!Double.isNaN(x)) {
                newPoint.x = x;
            }
            if (!Double.isNaN(y)) {
                newPoint.y = y;
            }
            if (!Double.isNaN(z)) {
                newPoint.z = z;
            }
        } else {
            if (!Double.isNaN(x)) {
                newPoint.x += x;
            }
            if (!Double.isNaN(y)) {
                newPoint.y += y;
            }
            if (!Double.isNaN(z)) {
                newPoint.z += z;
            }
        }

        return newPoint;
    }
    
    static public Point3f updateCenterWithCommand(List<String> commandArgs, Point3f initial, Point3f nextPoint, boolean absoluteIJKMode, boolean clockwise) {
        float i      = parseCoord(commandArgs, 'I');
        float j      = parseCoord(commandArgs, 'J');
        float k      = parseCoord(commandArgs, 'K');
        float radius = parseCoord(commandArgs, 'R');
        
        if (Double.isNaN(i) && Double.isNaN(j) && Double.isNaN(k)) {
            return GcodePreprocessorUtils.convertRToCenter(
                            initial, nextPoint, radius, absoluteIJKMode,
                            clockwise);
        }

        return updatePointWithCommand(initial, i, j, k, absoluteIJKMode);

    }
        
    static public String generateG1FromPoints(final Point3f start, final Point3f end, final boolean absoluteMode, DecimalFormat formatter) {
        DecimalFormat df = formatter;
        if (df == null) {
            df = new DecimalFormat("#.####");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("G1");

        if (absoluteMode) {
            if (!Double.isNaN(end.x)) {
                sb.append("X");
                sb.append(df.format(end.x));
            }
            if (!Double.isNaN(end.y)) {
                sb.append("Y");
                sb.append(df.format(end.y));
            }
            if (!Double.isNaN(end.z)) {
                sb.append("Z");
                sb.append(df.format(end.z));
            }
        } else { // calculate offsets.
            if (!Double.isNaN(end.x)) {
                sb.append("X");
                sb.append(df.format(end.x-start.x));
            }
            if (!Double.isNaN(end.y)) {
                sb.append("Y");
                sb.append(df.format(end.y-start.x));
            }
            if (!Double.isNaN(end.z)) {
                sb.append("Z");
                sb.append(df.format(end.z-start.x));
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Splits a gcode command by each word/argument, doesn't care about spaces.
     * This command is about the same speed as the string.split(" ") command,
     * but might be a little faster using precompiled regex.
     */
    static public List<String> splitCommand(String command) {
        List<String> l = new ArrayList<String>();
        boolean readNumeric = false;
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < command.length(); i++){
            char c = command.charAt(i);
                        
            // If the last character was numeric (readNumeric is true) and this
            // character is a letter or whitespace, then we hit a boundary.
            if (readNumeric && !Character.isDigit(c) && c != '.') {
                readNumeric = false; // reset flag.
                
                l.add(sb.toString());
                sb = new StringBuilder();
                
                if (Character.isLetter(c)) {
                    sb.append(c);
                }
            }

            else if (Character.isDigit(c) || c == '.' || c == '-') {
                sb.append(c);
                readNumeric = true;
            }
            
            else if (Character.isLetter(c)) {
                sb.append(c);
            }
        }
        
        // Add final one
        if (sb.length() > 0) {
            l.add(sb.toString());
        }
        
        return l;
    }
    
    // TODO: Replace everything that uses this with a loop that loops through
    //       the string and creates a hash with all the values.
    static public float parseCoord(List<String> argList, char c)
    {
        char address = Character.toUpperCase(c);
        for(String t : argList)
        {
            if (t.length() > 0 && Character.toUpperCase(t.charAt(0)) == address)
            {
                return (float) Double.parseDouble(t.substring(1));
            }
        }
        return (float) Double.NaN;
    }
    
    static public List<String> convertArcsToLines(Point3f start, Point3f end) {
        List<String> l = new ArrayList<String>();
        
        return l;
    }
    
    static public Point3f convertRToCenter(Point3f start, Point3f end, float radius, boolean absoluteIJK, boolean clockwise) {
        float R = radius;
        Point3f center = new Point3f();
        
        // This math is copied from GRBL in gcode.c
        float x = end.x - start.x;
        float y = end.y - start.y;

        float h_x2_div_d = 4 * R*R - x*x - y*y;
        if (h_x2_div_d < 0) { System.out.println("Error computing arc radius."); }
        h_x2_div_d = (float) ((-Math.sqrt(h_x2_div_d)) / Math.hypot(x, y));

        if (clockwise == false) {
            h_x2_div_d = -h_x2_div_d;
        }

        // Special message from gcoder to software for which radius
        // should be used.
        if (R < 0) {
            h_x2_div_d = -h_x2_div_d;
            // TODO: Places that use this need to run ABS on radius.
            radius = -radius;
        }

        float offsetX = 0.5f*(x-(y*h_x2_div_d));
        float offsetY = 0.5f*(y+(x*h_x2_div_d));

        if (!absoluteIJK) {
            center.x = start.x + offsetX;
            center.y = start.y + offsetY;
        } else {
            center.x = offsetX;
            center.y = offsetY;
        }

        return center;
    }

    /** 
     * Return the angle in radians when going from start to end.
     */
    static public float getAngle(final Point3f start, final Point3f end) {
        float deltaX = end.x - start.x;
        float deltaY = end.y - start.y;

        float angle = 0;

        if (deltaX != 0) { // prevent div by 0
            // it helps to know what quadrant you are in
            if (deltaX > 0 && deltaY >= 0) {  // 0 - 90
                angle = (float) Math.atan(deltaY/deltaX);
            } else if (deltaX < 0 && deltaY >= 0) { // 90 to 180
                angle = (float) (Math.PI - Math.abs(Math.atan(deltaY/deltaX)));
            } else if (deltaX < 0 && deltaY < 0) { // 180 - 270
                angle = (float) (Math.PI + Math.abs(Math.atan(deltaY/deltaX)));
            } else if (deltaX > 0 && deltaY < 0) { // 270 - 360
                angle = (float) (Math.PI * 2 - Math.abs(Math.atan(deltaY/deltaX)));
            }
        }
        else {
            // 90 deg
            if (deltaY > 0) {
                angle = (float) (Math.PI / 2);
            }
            // 270 deg
            else {
                angle = (float) (Math.PI * 3.0 / 2.0);
            }
        }
      
        return angle;
    }

    static public float calculateSweep(float startAngle, float endAngle, boolean isCw) {
        float sweep;

        // Full circle
        if (startAngle == endAngle) {
            sweep = (float) (Math.PI * 2);
            // Arcs
        } else {
            // Account for full circles and end angles of 0/360
            if (endAngle == 0) {
                endAngle = (float) Math.PI * 2;
            }
            // Calculate distance along arc.
            if (!isCw && endAngle < startAngle) {
                sweep = (float) ((Math.PI * 2 - startAngle) + endAngle);
            } else if (isCw && endAngle > startAngle) {
                sweep = (float) ((Math.PI * 2 - endAngle) + startAngle);
            } else {
                sweep = Math.abs(endAngle - startAngle);
            }
        }

        return sweep;
    }

    /**
     * Generates the points along an arc including the start and end points.
     */
    static public List<Point3f> generatePointsAlongArcBDring(final Point3f start, final Point3f end, final Point3f center, boolean clockwise, float R, float minArcLength, float arcSegmentLength) {
        float radius = R;

        // Calculate radius if necessary.
        if (radius == 0) {
            radius = (float) Math.sqrt(Math.pow(start.x - center.x, 2.0) + Math.pow(end.y - center.y, 2.0));
        }

        float startAngle = GcodePreprocessorUtils.getAngle(center, start);
        float endAngle = GcodePreprocessorUtils.getAngle(center, end);
        float sweep = GcodePreprocessorUtils.calculateSweep(startAngle, endAngle, clockwise);

        // Convert units.
        float arcLength = sweep * radius;

        // If this arc doesn't meet the minimum threshold, don't expand.
        if (minArcLength > 0 && arcLength < minArcLength) {
            return null;
        }

        int numPoints = 20;

        if (arcSegmentLength <= 0 && minArcLength > 0) {
            arcSegmentLength = (sweep * radius) / minArcLength;
        }

        if (arcSegmentLength > 0) {
            numPoints = (int)Math.ceil(arcLength/arcSegmentLength);
        }

        return GcodePreprocessorUtils.generatePointsAlongArcBDring(start, end, center, clockwise, radius, startAngle, sweep, numPoints);
    }

    /**
     * Generates the points along an arc including the start and end points.
     */
    static public List<Point3f> generatePointsAlongArcBDring(final Point3f p1,
            final Point3f p2, final Point3f center, boolean isCw, float radius, 
            float startAngle, float sweep, int numPoints) {

        Point3f lineEnd = new Point3f(p2.x, p2.y, p2.z);
        List<Point3f> segments = new ArrayList<Point3f>();
        float angle;

        // Calculate radius if necessary.
        if (radius == 0) {
            radius = (float) Math.sqrt(Math.pow(p1.x - center.x, 2.0) + Math.pow(p1.y - center.y, 2.0));
        }

        float zIncrement = (p2.z - p1.z) / numPoints;
        for(int i=0; i<numPoints; i++)
        {
            if (isCw) {
                angle = (startAngle - i * sweep/numPoints);
            } else {
                angle = (startAngle + i * sweep/numPoints);
            }

            if (angle >= Math.PI * 2) {
                angle = angle - (float) Math.PI * 2;
            }

            lineEnd.x = (float) (Math.cos(angle) * radius + center.x);
            lineEnd.y = (float) (Math.sin(angle) * radius + center.y);
            lineEnd.z += zIncrement;
            
            segments.add(new Point3f(lineEnd));
        }
        
        segments.add(new Point3f(p2));
        
        return segments;
    }
}
