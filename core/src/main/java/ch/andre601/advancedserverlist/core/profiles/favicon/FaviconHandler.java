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

package ch.andre601.advancedserverlist.core.profiles.favicon;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.function.Function;
import javax.imageio.ImageIO;

public class FaviconHandler<F> {

    private final Cache<String, CompletableFuture<F>> favicons =
            CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

    private final AdvancedServerList<F> core;
    private final ThreadPoolExecutor faviconThreadPool;

    public FaviconHandler(AdvancedServerList<F> core) {
        this.core = core;
        this.faviconThreadPool = createFaviconThreadPool();
    }

    public F getFavicon(String input, Function<BufferedImage, F> function) {
        try {
            return favicons.get(input, () -> convert(input, function)).getNow(null);
        } catch (ExecutionException e) {
            return null;
        }
    }

    public void clearCache() {
        favicons.invalidateAll();
    }

    private CompletableFuture<F> convert(String input, Function<BufferedImage, F> function) {
        return CompletableFuture.supplyAsync(
                () -> {
                    core.getPlugin()
                            .getPluginLogger()
                            .debug(FaviconHandler.class, "Getting BufferedImage for input '%s'...", input);

                    BufferedImage img = resolveImage(core, input);
                    if (img == null) {
                        core.getPlugin()
                                .getPluginLogger()
                                .debugWarn(FaviconHandler.class, "No BufferedImage could be created!");

                        return null;
                    }

                    core.getPlugin()
                            .getPluginLogger()
                            .debug(FaviconHandler.class, "Received BufferedImage! Applying it...");

                    return function.apply(img);
                },
                faviconThreadPool);
    }

    private BufferedImage resolveImage(AdvancedServerList<F> core, String input) {
        core.getPlugin().getPluginLogger().debug(FaviconHandler.class, "Resolving favicon for input '%s'...", input);

        InputStream stream;

        if (input.toLowerCase(Locale.ROOT).startsWith("https://")) {
            core.getPlugin().getPluginLogger().debug(FaviconHandler.class, "URL detected. Getting file from it...");

            stream = getFromUrl(core, input);
        } else if (input.toLowerCase(Locale.ROOT).endsWith(".png")) {
            core.getPlugin()
                    .getPluginLogger()
                    .debug(FaviconHandler.class, "Image file detected. Trying to reolve it...");

            File folder = core.getPlugin().getFolderPath().resolve("favicons").toFile();
            if (!folder.exists()) {
                core.getPlugin()
                        .getPluginLogger()
                        .warn("Cannot get Favicon %s from favicons folder. Folder doesn't exist!", input);
                return null;
            }

            File file = new File(folder, input);

            try {
                stream = new FileInputStream(file);

                core.getPlugin().getPluginLogger().debug(FaviconHandler.class, "Resolved image file!");
            } catch (IOException ex) {
                core.getPlugin().getPluginLogger().warn("Cannot create Favicon from File %s.", input);
                core.getPlugin().getPluginLogger().warn("Cause: %s", ex.getMessage());
                return null;
            }
        } else {
            core.getPlugin()
                    .getPluginLogger()
                    .debug(
                            FaviconHandler.class,
                            "Possible player name/UUID detected. Using https://mc-heads.net/avatar/%s/64...",
                            input);

            stream = getFromUrl(core, "https://mc-heads.net/avatar/" + input + "/64");
        }

        if (stream == null) {
            core.getPlugin().getPluginLogger().warn("Cannot create Favicon. InputStream was null.");
            return null;
        }

        try {
            BufferedImage original = ImageIO.read(stream);
            if (original == null) {
                core.getPlugin().getPluginLogger().warn("Cannot create Favicon. Unable to create BufferedImage.");
                return null;
            }

            core.getPlugin().getPluginLogger().debug(FaviconHandler.class, "Resizing image to 64x64 pixels...");

            BufferedImage favicon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D img = favicon.createGraphics();

            // Resize original image to 64x64 and apply to favicon.
            img.drawImage(original, 0, 0, 64, 64, null);
            img.dispose();

            core.getPlugin().getPluginLogger().debug(FaviconHandler.class, "Image resized!");
            return favicon;
        } catch (IOException ex) {
            core.getPlugin()
                    .getPluginLogger()
                    .warn("Unable to create Favicon. Encountered IOException during creation.");
            core.getPlugin().getPluginLogger().warn("Cause: %s", ex.getMessage());
            return null;
        }
    }

    private InputStream getFromUrl(AdvancedServerList<F> core, String url) {
        try {
            core.getPlugin().getPluginLogger().debug(FaviconHandler.class, "Resolving URL '%s'...", url);

            URL faviconUrl = new URL(url);
            URLConnection connection = faviconUrl.openConnection();
            connection.setRequestProperty("User-Agent", "AdvancedServerList/" + core.getVersion());
            connection.connect();

            core.getPlugin().getPluginLogger().debug(FaviconHandler.class, "URL resolved!");
            return connection.getInputStream();
        } catch (IOException ex) {
            core.getPlugin().getPluginLogger().warn("Error while connecting to %s for Favicon creation.", url);
            core.getPlugin().getPluginLogger().warn("Cause: %s", ex.getMessage());
            return null;
        }
    }

    private ThreadPoolExecutor createFaviconThreadPool() {
        return new ThreadPoolExecutor(3, 3, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1024), r -> {
            Thread t = new Thread(r, "AdvancedServerList-FaviconThread");
            t.setDaemon(true);
            return t;
        });
    }
}
