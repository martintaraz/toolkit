/*
 * Copyright (c) 1998-2017 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.collections;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides storage and quick retrieval of 2D spatially-oriented objects.
 * <p>
 * Once an object has been added, its bounds should not be changed. To change its bounds, first
 * remove it from the tree, then re-insert it once the change has been made.
 * <p>
 * This class must be synchronized externally if multiple threads intend to access it at the same
 * time.
 */
public class AreaTree {
    private AreaNode mRoot;

    /** Creates an new, empty tree. */
    public AreaTree() {
        mRoot = new AreaNode();
    }

    /** Removes all nodes. */
    public void clear() {
        mRoot = new AreaNode();
    }

    /**
     * @param obj The object to look for.
     * @return {@code true} if the tree currently contains the object.
     */
    public boolean contains(AreaObject obj) {
        return mRoot.contains(obj);
    }

    /** @return The number of data elements in the tree. */
    public int count() {
        return searchCount(null, false);
    }

    /** @return All data in the tree. */
    public List<AreaObject> getAllData() {
        return search(null, false);
    }

    /** @return The bounds that this tree encompasses. */
    public Rectangle getBounds() {
        return mRoot.getBounds();
    }

    /**
     * Inserts an object into the tree.
     *
     * @param obj The object to add. May not be {@code null}.
     */
    public void insert(AreaObject obj) {
        mRoot = mRoot.insert(obj);
    }

    /**
     * Inserts a list of objects into the tree.
     *
     * @param list The list of objects to add. May not be {@code null}.
     */
    public void insert(List<AreaObject> list) {
        for (AreaObject bounds : list) {
            insert(bounds);
        }
    }

    /**
     * Inserts an array of objects into the tree.
     *
     * @param array The array of objects to add. May not be {@code null}.
     */
    public void insert(AreaObject[] array) {
        for (AreaObject bounds : array) {
            insert(bounds);
        }
    }

    /**
     * Removes an object from the tree. If the object was not present, nothing occurs.
     *
     * @param obj The object to remove. May not be {@code null}.
     */
    public void remove(AreaObject obj) {
        mRoot = mRoot.remove(obj);
    }

    /**
     * Removes a list of objects from the tree. Any objects within the list which are not in the
     * tree are ignored.
     *
     * @param list The list of objects to remove. May not be {@code null}.
     */
    public void remove(List<AreaObject> list) {
        mRoot = mRoot.remove(list);
    }

    /**
     * @param x The x coordinate to search with.
     * @param y The y coordinate to search with.
     * @return All objects that intersect with the coordinates x &amp; y.
     */
    public List<AreaObject> search(int x, int y) {
        return search(new Point(x, y));
    }

    /**
     * Adds all objects that intersect with the coordinates x &amp; y to the passed in list.
     *
     * @param x      The x coordinate to search with.
     * @param y      The y coordinate to search with.
     * @param result Filled in with the list of objects that match.
     * @return {@code false} if nothing was found.
     */
    public boolean search(int x, int y, List<AreaObject> result) {
        return search(new Point(x, y), result);
    }

    /**
     * @param location The location to search with.
     * @return All objects that intersect with the {@code location}.
     */
    public List<AreaObject> search(Point location) {
        List<AreaObject> list = new ArrayList<>();
        mRoot.search(location, list);
        return list;
    }

    /**
     * @param location    The location to search with.
     * @param targetClass The class of object for which we're looking
     * @return All objects that intersect with {@code location} and are instances of the target
     *         class.
     */
    public List<AreaObject> search(Point location, Class<? extends AreaObject> targetClass) {
        List<AreaObject> list = new ArrayList<>();
        mRoot.search(location, list, targetClass);
        return list;
    }

    /**
     * Adds all objects that intersect with the {@code location} to the passed in list.
     *
     * @param location The location to search with.
     * @param result   Filled in with the list of objects that match.
     * @return {@code false} if nothing was found.
     */
    public boolean search(Point location, List<AreaObject> result) {
        result.clear();
        mRoot.search(location, result);
        return !result.isEmpty();
    }

    /**
     * @param bounds The bounds to search with.
     * @return All objects that intersect with the {@code bounds}.
     */
    public List<AreaObject> search(Rectangle bounds) {
        return search(bounds, false);
    }

    /**
     * Adds all objects that intersect with the {@code bounds} to the passed in list.
     *
     * @param bounds The bounds to search with.
     * @param result Filled in with the list of objects that match.
     * @return {@code false} if nothing was found.
     */
    public boolean search(Rectangle bounds, List<AreaObject> result) {
        return search(bounds, false, result);
    }

    /**
     * @param bounds     The bounds to search with.
     * @param exactMatch {@code true} to match coordinates exactly, {@code false} only require an
     *                   intersection.
     * @return All objects that intersect with {@code bounds}. If {@code exactMatch} is {@code
     *         true}, then only those objects that have the exact same coordinates as {@code
     *         bounds}.
     */
    public List<AreaObject> search(Rectangle bounds, boolean exactMatch) {
        List<AreaObject> list = new ArrayList<>();
        mRoot.search(bounds, list, exactMatch);
        return list;
    }

    /**
     * @param bounds      The bounds to search with.
     * @param targetClass The class of object for which we're looking
     * @return All objects that intersect with {@code bounds} and are instances of the target
     *         class.
     */
    public List<AreaObject> search(Rectangle bounds, Class<? extends AreaObject> targetClass) {
        List<AreaObject> list = new ArrayList<>();
        mRoot.search(bounds, list, targetClass);
        return list;
    }

    /**
     * If {@code exactMatch} is {@code true}, adds only objects that have the exact same coordinates
     * as {@code bounds}, otherwise, adds all objects that intersect with {@code bounds} to the
     * passed in list.
     *
     * @param bounds     The bounds to search with.
     * @param exactMatch {@code true} to match coordinates exactly, {@code false} only require an
     *                   intersection.
     * @param result     Filled in with the list of objects that match.
     * @return {@code false} if nothing was found.
     */
    public boolean search(Rectangle bounds, boolean exactMatch, List<AreaObject> result) {
        result.clear();
        mRoot.search(bounds, result, exactMatch);
        return !result.isEmpty();
    }

    /**
     * @param x The x coordinate to search with.
     * @param y The y coordinate to search with.
     * @return The number of objects that intersect with the coordinates {@code x}& {@code y}.
     */
    public int searchCount(int x, int y) {
        return searchCount(new Point(x, y));
    }

    /**
     * @param location The location to search with.
     * @return The number of objects that intersect with the {@code location}.
     */
    public int searchCount(Point location) {
        return mRoot.searchCount(location);
    }

    /**
     * @param bounds The bounds to search with.
     * @return The number of objects that intersect with the {@code bounds}.
     */
    public int searchCount(Rectangle bounds) {
        return searchCount(bounds, false);
    }

    /**
     * @param bounds     The bounds to search with.
     * @param exactMatch {@code true} to match coordinates exactly, {@code false} only require an
     *                   intersection.
     * @return The count of objects that intersect with {@code bounds}. If {@code exactMatch} is
     *         {@code true}, then only those objects that have the exact same coordinates as {@code
     *         bounds} are counted.
     */
    public int searchCount(Rectangle bounds, boolean exactMatch) {
        return mRoot.searchCount(bounds, exactMatch);
    }

    /**
     * @param location The location to search with. {@code location} must not be {@code null}.
     * @return {@code true} if there are any objects that intersect with {@code location}.
     */
    public boolean searchHit(Point location) {
        return mRoot.searchHit(location);
    }

    /**
     * @param bounds The bounds to search with. {@code bounds} must not be {@code null}.
     * @return {@code true} if there are any objects that intersect with {@code bounds}.
     */
    public boolean searchHit(Rectangle bounds) {
        return mRoot.searchHit(bounds);
    }

    /**
     * @param bounds      The bounds to search with. {@code bounds} must not be {@code null}.
     * @param targetClass The class of object for which we're looking
     * @return {@code true} if there are any objects of the target class that intersect with {@code
     *         bounds}.
     */
    public boolean searchHit(Rectangle bounds, Class<? extends AreaObject> targetClass) {
        return mRoot.searchHit(bounds, targetClass);
    }
}
