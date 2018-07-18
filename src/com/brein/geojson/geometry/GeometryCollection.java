package com.brein.geojson.geometry;

import com.brein.geojson.tools.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeometryCollection implements IGeometryObject {
    private final List<IGeometryObject> shapes;

    public GeometryCollection(final List<IGeometryObject> shapes) {this.shapes = shapes;}

    public GeometryCollection(final IGeometryObject... shapes) {
        this(Arrays.asList(shapes));
    }

    @Override
    public boolean within(final IGeometryObject other) {
        for (final IGeometryObject s : shapes) {
            if (!s.within(other)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean encases(final IGeometryObject other) {
        //warning, this will fail if two polygons encase an object, but neither of them completely encase it by themself

        if (other.getClass().isAssignableFrom(GeometryCollection.class)) {
            for (final IGeometryObject otherShape : ((GeometryCollection) other).shapes) {
                boolean otherShapeEncased = false;
                for (final IGeometryObject shape : shapes) {
                    if (shape.encases(otherShape)) {
                        otherShapeEncased = true;
                        break;
                    }
                }
                if (!otherShapeEncased) {
                    return false;
                }
            }
            return true;
        }
        //otherwise, simple single object to check for
        for (final IGeometryObject shape : shapes) {

            if (other.within(shape)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double distance(final IGeometryObject other) {
        return 0;//todo
    }

    @Override
    public double surfaceArea() {
        return shapes.stream().mapToDouble(IGeometryObject::surfaceArea).sum();
    }

    @Override
    public BoundingBox boundingBox() {
        if (shapes.isEmpty()) {
            return null;
        } else {
            final BoundingBox res = shapes.get(0).boundingBox().copy();

            shapes.forEach(shape -> res.addBBox(shape.boundingBox()));
            return res;
        }
    }

    @Override
    public Point centroid() {
        double latSum = 0;
        double lonSum = 0;
        double weightSum = 0;
        for (final IGeometryObject shape : shapes) {
            final double weight = shape.surfaceArea();
            final Point center = shape.centroid();
            latSum += weight * center.getLat();
            lonSum += weight * center.getLon();
            weightSum += weight;

        }
        return new Point(latSum / weightSum, lonSum / weightSum);
    }

    @Override
    public Map<String, Object> toMap() {
        final Map<String, Object> res = new HashMap<>();
        res.put(Constants.GEOJSON_TYPE, Constants.GEOJSON_TYPE_GEOMETRY_COLLECTION);
        res.put(Constants.GEOJSON_GEOMETRIES, shapes.stream().map(IGeometryObject::toMap).collect(Collectors.toList()));
        return res;
    }

    public List<IGeometryObject> getShapes() {
        return shapes;
    }
}