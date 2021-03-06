/**
 * Copyright (C) 2016 eBusiness Information
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor;

import timber.log.Timber;

public class SysoTree implements Timber.Tree {
    @Override
    public void v(String message, Object... args) {
        System.out.println(String.format(message, args));
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
        System.out.println(String.format(message, args));
        t.printStackTrace();
    }

    @Override
    public void d(String message, Object... args) {
        System.out.println(String.format(message, args));

    }

    @Override
    public void d(Throwable t, String message, Object... args) {
        System.out.println(String.format(message, args));
        t.printStackTrace();
    }

    @Override
    public void i(String message, Object... args) {
        System.out.println(String.format(message, args));

    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        System.out.println(String.format(message, args));
        t.printStackTrace();
    }

    @Override
    public void w(String message, Object... args) {
        System.out.println(String.format(message, args));

    }

    @Override
    public void w(Throwable t, String message, Object... args) {
        System.out.println(String.format(message, args));
        t.printStackTrace();
    }

    @Override
    public void e(String message, Object... args) {
        System.out.println(String.format(message, args));

    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        System.out.println(String.format(message, args));
        t.printStackTrace();
    }
}
