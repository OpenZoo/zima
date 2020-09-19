/**
 * Copyright (c) 2020 Adrian Siekierka
 *
 * This file is part of zima.
 *
 * zima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * zima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with zima.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.asie.zima.util;

import java.util.*;

public class PropertyHolder {
    @FunctionalInterface
    public interface ChangeListener<T> {
        void onChange(Property<T> property, T value);
    }

    private final Object lock;
    private final Map<Property<?>, Object> propertyValueMap;
    private final Set<PropertyAffect> affects;
    private final Map<Property<?>, List<ChangeListener<?>>> changeListenerMap;
    private final List<ChangeListener<?>> globalChangeListeners;

    public PropertyHolder() {
        this.lock = new Object();
        this.propertyValueMap = new HashMap<>();
        this.affects = new HashSet<>();
        this.changeListenerMap = new HashMap<>();
        this.globalChangeListeners = new ArrayList<>();
    }

    public <T> void addChangeListener(Property<T> property, ChangeListener<T> listener) {
        changeListenerMap.computeIfAbsent(property, p -> new ArrayList<>(2)).add(listener);
    }

    public void addGlobalChangeListener(ChangeListener<?> listener) {
        globalChangeListeners.add(listener);
    }

    public boolean has(Property<?> key) {
        synchronized (this.lock) {
            return key.getDefaultValue() != null || propertyValueMap.containsKey(key);
        }
    }

    public <T> T get(Property<T> key) {
        synchronized (this.lock) {
            //noinspection unchecked
            return (T) propertyValueMap.getOrDefault(key, key.getDefaultValue());
        }
    }

    public <T> void reset(Property<T> key) {
        set(key, key.getDefaultValue());
    }

    @SuppressWarnings("unchecked")
    public <T> void set(Property<T> key, T value) {
        synchronized (this.lock) {
            if (!Objects.equals(get(key), value)) {
                propertyValueMap.put(key, value);
                key.getAffects().forEach(this::affect);
                changeListenerMap.getOrDefault(key, List.of()).forEach(c -> ((ChangeListener<T>) c).onChange(key, value));
                globalChangeListeners.forEach(c -> ((ChangeListener<T>) c).onChange(key, value));
            }
        }
    }

    public void affect(PropertyAffect affect) {
        synchronized (this.lock) {
            this.affects.add(affect);
        }
    }

    public void affectAll(PropertyAffect... affect) {
        synchronized (this.lock) {
            this.affects.addAll(Arrays.asList(affect));
        }
    }

    public boolean isAffected(PropertyAffect affect) {
        synchronized (this.lock) {
            return this.affects.contains(affect);
        }
    }

    public void copyTo(PropertyHolder other) {
        Map<Property<?>, Object> propertyValueMapCopied = new HashMap<>();

        synchronized (this.lock) {
            this.propertyValueMap.forEach(propertyValueMapCopied::put);
        }

        synchronized (other.lock) {
            propertyValueMapCopied.forEach((k, v) -> {
                if (!Objects.equals(other.get(k), v)) {
                    other.propertyValueMap.put(k, v);
                    other.affects.addAll(k.getAffects());
                }
            });
        }
    }

    public PropertyHolder clone(PropertyAffect... affectsPop) {
        synchronized (this.lock) {
            PropertyHolder cloned = new PropertyHolder();
            this.propertyValueMap.forEach(cloned.propertyValueMap::put);
            for (PropertyAffect a : affectsPop) {
                if (this.affects.remove(a)) {
                    cloned.affects.add(a);
                }
            }
            return cloned;
        }
    }
}
