package nibylandia.ecorp.networkutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import org.brotli.dec.BrotliInputStream;

import nibylandia.ecorp.networkutils.exception.ForbiddenStatusException;
import nibylandia.ecorp.networkutils.exception.MovedStatusException;
import nibylandia.ecorp.networkutils.exception.NonOkStatusException;
import nibylandia.ecorp.networkutils.exception.NotFoundStatusException;

/**
 * Network utilities for http traffic.
 */
public final class NetworkUtils {
	private static HttpClient CLIENT = HttpClient.newHttpClient();

	private NetworkUtils() {}
	
	/**
	 * Creates a new underlying HttpClient.
	 * This seems to be necessary in cases the maximum number of open streams is reached and the underlying implementation cannot cope with it.
	 * Usually should the server inform the client on the maximum number of possible open streams, but in some cases it does not work and
	 * it is not clear whether the server is responsible for that or the JVM. You just get an 'too many concurrent streams' error.
	 */
	public static void renewHttpClient() {
		CLIENT = HttpClient.newHttpClient();
	}
	
	/**
	 * Creates a new web socket builder related to the underlying HttpClient.
	 * @return a new web socket builder
	 */
	public static WebSocket.Builder getWebsocketBuilder() {
		return CLIENT.newWebSocketBuilder();
	}
	
	/**
	 * Performs a GET request on the given URI, buffers the whole response and returns it as a string.
	 * Use with caution as the whole response will be held in memory.
	 * @param uri see {@link #getInputStream(URI, String, boolean, boolean, boolean, boolean, String, String)}
	 * @param cookies see {@link #getInputStream(URI, String, boolean, boolean, boolean, boolean, String, String)}
	 * @param acceptAnything see {@link #getInputStream(URI, String, boolean, boolean, boolean, boolean, String, String)}
	 * @param doNotTrack see {@link #getInputStream(URI, String, boolean, boolean, boolean, boolean, String, String)}
	 * @param upgradeInsecureRequests see {@link #getInputStream(URI, String, boolean, boolean, boolean, boolean, String, String)}
	 * @param teTrailers see {@link #getInputStream(URI, String, boolean, boolean, boolean, boolean, String, String)}
	 * @param origin see {@link #getInputStream(URI, String, boolean, boolean, boolean, boolean, String, String)}
	 * @param referer see {@link #getInputStream(URI, String, boolean, boolean, boolean, boolean, String, String)}
	 * @return a string containing the whole response from the server, assuming an UTF-8 encoding
	 * @throws IOException see {@link #getInputStream(URI, String, boolean, boolean, boolean, boolean, String, String)}
	 * @throws InterruptedException see {@link #getInputStream(URI, String, boolean, boolean, boolean, boolean, String, String)}
	 * @throws NonOkStatusException see {@link #getInputStream(URI, String, boolean, boolean, boolean, boolean, String, String)}
	 */
	public static StringBuilder getString(URI uri, String cookies, boolean acceptAnything, boolean doNotTrack, boolean upgradeInsecureRequests, boolean teTrailers, String origin, String referer) throws IOException, InterruptedException, NonOkStatusException {
		StringBuilder result = new StringBuilder();

		HttpResponse<InputStream> response = NetworkUtils.getInputStream(uri, cookies, acceptAnything, doNotTrack, upgradeInsecureRequests, teTrailers, origin, referer);
		InputStream is = NetworkUtils.getInputStream(NetworkUtils.getContentEncoding(response), response.body());
		InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
		char buffer[] = new char[1024];
		int read;
		
		while ((read = isr.read(buffer)) > 0) {
			result.append(buffer, 0, read);
		}
		
		isr.close();
		
		return result;
	}
	
	/**
	 * Performs a GET request on the given URI and returns an InputStream for reading the server's response.
	 * @param uri URI to be requested from the server.
	 * @param cookies Value of the Cookie header or null for none.
	 * @param acceptAnything True for accepting \*\/\*, false for accepting html, xml and webp only.
	 * @param doNotTrack True for a DNT header, false for none.
	 * @param upgradeInsecureRequests True for Update-Insecure-Requests header, false for none. 
	 * @param teTrailers True for TE Trailers header, false for none.
	 * @param origin Value of the Origin header or null for none.
	 * @param referer Value of the Referer header or null for none.
	 * @return InputStream for reading the server's request, possibly encoded - see {@link #getContentEncoding(HttpResponse)}.
	 * @throws IOException In case of an I/O error on the network level. 
	 * @throws InterruptedException In case the thread has been interrupted.
	 * @throws NonOkStatusException In case a non-200 response has been returned from the server.
	 */
	public static HttpResponse<InputStream> getInputStream(URI uri, String cookies, boolean acceptAnything, boolean doNotTrack, boolean upgradeInsecureRequests, boolean teTrailers, String origin, String referer) throws IOException, InterruptedException, NonOkStatusException {
		HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
			.version(Version.HTTP_2)
			.GET()
			.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:77.0) Gecko/20100101 Firefox/77.0");
		
		if (acceptAnything) {
			builder.header("Accept", "*/*");
		} else {
			builder.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		}
		
		builder.header("Accept-Language", "en-US,en;q=0.5");
		builder.header("Accept-Encoding", "gzip, deflate, br");
		
		if (origin != null) {
			builder.header("Origin", origin);
		}
		
		if (referer != null) {
			builder.header("Referer", referer);
		}
		
		if (doNotTrack) {
			builder.header("DNT", "1");
		}
		
		if (cookies != null) {
			builder.header("Cookie", cookies);
		}
		
		if (upgradeInsecureRequests) {
			builder.header("Upgrade-Insecure-Requests", "1");
		}
		
		if (teTrailers) {
			builder.header("TE", "Trailers");
		}
		
		HttpRequest request = builder.build();
		BodyHandler<InputStream> bodyStream = BodyHandlers.ofInputStream();
		
		HttpResponse<InputStream> response;
			response = CLIENT.send(request, bodyStream);
		
		if (response.statusCode() != 200) {
			if (response.statusCode() == 301 // Moved Permanently
					|| response.statusCode() == 302 // Found
					|| response.statusCode() == 303 // See Also
					|| response.statusCode() == 307 // Temporary Redirect
					|| response.statusCode() == 308) { // Permanent Redirect
				Optional<String> hdr = response.headers().firstValue("location");
				throw new MovedStatusException(response.statusCode(), hdr.isPresent() ? hdr.get() : null);
			} else if (response.statusCode() == 403) {
				throw new ForbiddenStatusException(response.statusCode());
			} else if (response.statusCode() == 404) {
				throw new NotFoundStatusException(response.statusCode());
			}

			throw new NonOkStatusException(response.statusCode());
		}

		return response;
	}

	/**
	 * Returns the content encoding response header, if any.
	 * @param response HttpResponse object.
	 * @return Content encoding response header value or null if none.
	 */
	public static String getContentEncoding(HttpResponse<?> response) {
		return response.headers().firstValue("Content-Encoding").orElse(null);
	}
	
	/**
	 * Decodes the input stream, depending on the given encoding.
	 * Currently supported: br, gzip.
	 * @param contentEncoding Content encoding value as defined by the server. Null or empty string returns the same input stream without modifications.
	 * @param sourceStream Input stream to be decoded.
	 * @return Input stream translated on-the-fly by a corresponding decoder.
	 * @throws IOException in case of an I/O error.
	 */
	public static InputStream getInputStream(String contentEncoding, InputStream sourceStream) throws IOException {
		if (contentEncoding == null || contentEncoding.length() == 0 || "identity".equals(contentEncoding)) {
			return sourceStream;
		} else if ("br".equals(contentEncoding)) {
			return new BrotliInputStream(sourceStream);
		} else if ("gzip".equals(contentEncoding)) {
			return new GZIPInputStream(sourceStream);
		} else {
			throw new UnsupportedOperationException("content encoding of " + contentEncoding + " is not supported");
		}
	}
	
	/**
	 * Convenience function converting the given input stream to an UTF-8 buffered reader.
	 * @param sourceStream Input stream to be converted.
	 * @return BufferedReader on the given stream with UTF-8 encoding.
	 */
	public static BufferedReader getUtf8BufferedReader(InputStream sourceStream) {
		return new BufferedReader(new InputStreamReader(sourceStream, StandardCharsets.UTF_8));
	}
}
