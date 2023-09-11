/*
 * MIT License
 *
 * Copyright (c) 2022-2023 Andre_601
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package ch.andre601.advancedserverlist.versionuploader;

import java.util.List;

public enum PlatformInfo{
    
    BUKKIT(
        "bukkit/target/AdvancedServerList-Bukkit-${plugin.version}.jar",
        "Spigot", "Paper", "Folia"
    ),
    BUNGEECORD(
        "bungeecord/target/AdvancedServerList-BungeeCord-${plugin.version}.jar",
        "BungeeCord", "Waterfall"
    ),
    VELOCITY(
        "velocity/target/AdvancedServerList-Velocity-${plugin.version}.jar",
        "Velocity"
    );
    
    private final String filePath;
    private final List<String> loaders;
    
    PlatformInfo(String filePath, String... loaders){
        this.filePath = filePath;
        this.loaders = List.of(loaders);
    }
    
    public String getFilePath(){
        return filePath;
    }
    
    public List<String> getLoaders(){
        return loaders;
    }
}