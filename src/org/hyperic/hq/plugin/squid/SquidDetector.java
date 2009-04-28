/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2004-2009], Hyperic, Inc.
 * This file is part of HQ.
 * 
 * HQ is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.hq.plugin.squid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.hyperic.hq.product.SNMPDetector;

public class SquidDetector extends SNMPDetector {

    protected Map getProcOpts(long pid) {
        String exe = getProcExe(pid);
        Map opts = super.getProcOpts(pid);
        //parse squid.conf for use in discovery templates, example:
        //Squid\ 2.x.AUTOINVENTORY_NAME=squid @%platform.name%:%http_port%"
        String optf = (String)opts.get("-f");
        if (optf == null) {
            File path = new File(exe).getParentFile();
            if (path.getName().equals("sbin")) {
                path = new File(path.getParent(), "etc/squid.conf");
                if (path.exists()) {
                    optf = path.getPath();
                    getLog().debug("Using default squid.conf: " + optf);
                }
            }
        }
        if (optf == null) {
            getLog().debug("Unable to find squid.conf");
            return opts;
        }

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(optf));
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if ((line.length() == 0) || (line.charAt(0) == '#')) {
                    continue;
                }
                String[] elts = line.split("\\s+");
                if (elts.length == 2) {
                    opts.put(elts[0], elts[1]);
                }
            }
        } catch (IOException e) {
            getLog().error("Failed to read " + optf + e.getMessage(), e);
        } finally {
            if (in != null) {
                try { in.close(); } catch (IOException e) {}
            }
        }
        return opts;
    }
}
