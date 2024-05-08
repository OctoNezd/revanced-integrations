package app.revanced;

import android.net.Uri;
import androidx.annotation.Nullable;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.requests.Requester;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.services.youtube.YoutubeService;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Test {
    private static Map<Integer, String> formats;


    @Nullable
    public static HttpURLConnection makeRequest(final Request request) {
        try {
            Logger.printInfo(() -> "Hooked request");

            HttpURLConnection connection = (HttpURLConnection) new URL(request.url()).openConnection();
            connection.setRequestMethod(request.httpMethod());

            connection.setUseCaches(false);
            connection.setDoOutput(Objects.equals(request.httpMethod(), "POST"));

            String agentString = "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0";
            connection.setRequestProperty("User-Agent", agentString);

            for (final Map.Entry<String, List<String>> pair : request.headers().entrySet()) {
                final String headerName = pair.getKey();
                final List<String> headerValueList = pair.getValue();

                if (headerValueList.size() > 1) {
                    for (final String headerValue : headerValueList) {
                        connection.addRequestProperty(headerName, headerValue);
                    }
                } else if (headerValueList.size() == 1) {
                    connection.addRequestProperty(headerName, headerValueList.get(0));
                }
            }
            Logger.printInfo(() -> "Hooked headers");

            final byte[] innerTubeBody = request.dataToSend();
            if (innerTubeBody != null) {
                connection.getOutputStream().write(innerTubeBody, 0, innerTubeBody.length);
            }
            Logger.printInfo(() -> "Hooked body");

            final int responseCode = connection.getResponseCode();
            if (responseCode == 200) return connection;
            else if (responseCode == 429) {
                Logger.printInfo(() -> "Hooked reCaptcha Challenge requested");
                throw new Exception("reCaptcha Challenge requested");
            } else {
                Logger.printInfo(() -> "Hooked Error making request: " + responseCode);
                throw new Exception("Error making request: " + responseCode);
            }

        } catch (Exception ignored) {
            Logger.printInfo(() -> "Hooked Error making request: " + ignored.getMessage(), ignored);
        }

        return null;
    }

    public static void hook(Object formatsListObject) {
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[1];
        StackTraceElement stackTraceElement2 = new Throwable().getStackTrace()[2];
        StackTraceElement stackTraceElement3 = new Throwable().getStackTrace()[3];
        Logger.printInfo(() -> "Call " + stackTraceElement3);
        Logger.printInfo(() -> "Call " + stackTraceElement2);
        Logger.printInfo(() -> "Call " + stackTraceElement);

        if (formats == null) {
            Logger.printInfo(() -> "Fetching urls");
            try {
                formats = Utils.submitOnBackgroundThread(() -> {
                    var f = new HashMap<Integer, String>();
                    NewPipe.init(new Downloader() {
                        @Override
                        public Response execute(Request request) throws IOException {
                            var c = makeRequest(request);
                            var body = false;
                            try {
                                body = c.getInputStream() != null;
                            } catch (Exception e) {
                            }
                            Response r = null;
                            try {
                                r = new Response(
                                        c.getResponseCode(),
                                        c.getResponseMessage(),
                                        c.getHeaderFields(),
                                        body ? Requester.parseString(c) : null,
                                        c.getURL().toString()
                                );
                            } catch (IOException e) {
                                throw e;
                            }
                            c.disconnect();
                            return r;
                        }
                    });
                    var extractor = new YoutubeService(1).getStreamExtractor(YoutubeStreamLinkHandlerFactory.getInstance().fromId("piKJAUwCYTo"));
                    extractor.fetchPage();
                    for (AudioStream audioStream : extractor.getAudioStreams()) {
                        f.put(audioStream.getItag(), audioStream.getContent());
                    }

                    for (VideoStream videoOnlyStream : extractor.getVideoOnlyStreams()) {
                        f.put(videoOnlyStream.getItag(), videoOnlyStream.getContent());
                    }

                    for (VideoStream videoStream : extractor.getVideoStreams()) {
                        f.put(videoStream.getItag(), videoStream.getContent());
                    }

                    return f;
                }).get();

            } catch (Exception i) {
            }
            //formats = StoryboardRendererRequester.getFormats("piKJAUwCYTo");
        }

        try {
            if (formatsListObject instanceof List) {
                var formatsList = (List) formatsListObject;
                for (Object formatObject : formatsList) {
                    var field = formatObject.getClass().getDeclaredField("f");
                    var url = (String) field.get(formatObject);
                    if (!url.contains("googlevideo")) continue;

                    var itag = Uri.parse(url).getQueryParameter("itag");
                    if (itag == null) {
                        Logger.printInfo(() -> "URL does not contain itag: " + url);
                        continue;
                    }

                    String replacement = formats.get(Integer.parseInt(itag));
                    if (replacement == null) {
                        Logger.printInfo(() -> "Falling back to itag 133");
                        replacement = formats.get(133);
                    }

                    if (replacement == null) {
                        Logger.printInfo(() -> "No replacement found for itag: " + itag);
                        continue;
                    }

                    String finalReplacement = replacement;
                    Logger.printInfo(() -> "Replacing " + url + " with " + finalReplacement);

                    field.set(formatObject, replacement);

                }
            }
        } catch (Exception e) {
            Logger.printInfo(() -> "Hooked Error: " + e.getMessage(), e);
        }

    }
}