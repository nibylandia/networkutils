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

public final class NetworkUtils {
	private static HttpClient CLIENT = HttpClient.newHttpClient();

	private NetworkUtils() {}
	
	public static void renewHttpClient() {
		CLIENT = HttpClient.newHttpClient();
	}
	
	public static WebSocket.Builder getWebsocketBuilder() {
		return CLIENT.newWebSocketBuilder();
	}
	
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
		
		//.header("Cookie", "__cfduid=d9b18f21565c18c240d5305f110057adf1591614606; csrftoken=TiwmszcyH4ZCMGlpzBlvdJ1trmCNiMmi8Royw3hdHh5xn4otio9COU8qtwLTrjIx; stcki="VbMkPs=0"; affkey="eJyrVipSslJQUqoFAAwfAk0="; sbr="sec:sbr69e13ea0-5315-4bed-81ae-f02a8ecd14fe:1jiFfK:fHY_ykqAJNqMgA4lX0qR35_ooBs"; dwf_s_a=True; __cf_bm=38f06a5027be484d80424c3ec1416eae42c3b8a4-1591614606-1800-ATmHB7jQp2v3yTD/tKJdGGAc5mrQRR9kUJ+w35QdcX11Fnl6Qu6CMuLPNS5ll4sqE8buXyu+uZftxv6iOSo9884=; xaduuid=33118a43-43fd-43c9-b0ee-b5b8ec09e2a0; __utfpp="f:trnxf08d860f47dd7a53ae3329a81aea70bd:1jiFfM:dAT2aID6GcJUG63xikBhe-Hc50A"; agreeterms=1; tbu_justin4men=df,8,-1")
		//.header("Cookie", "xhamsterlive_com_firstVisit=2020-03-13T11%3A23%3A13Z; xhamsterlive_com_affiliateId=2952b855a5c5a8ff0a377046f7949056e8391a3e01f0553be04ed1123b56e992; ABTest_ab_index_20191209_key=B; isVisitorsAgreementAccepted=1; alreadyVisited=1; baseAmpl=%7B%22up%22%3A%7B%22page%22%3A%22view%22%2C%22navigationParams%22%3A%7B%22limit%22%3A60%2C%22offset%22%3A0%7D%2C%22viewportParameters%22%3A%7B%22isFullscreen%22%3Afalse%7D%7D%7D; xhamsterlive_com_ABTest_recommended_key=B; guestWatchHistoryIds=; amplitude_id_19a23394adaadec51c3aeee36622058dxhamsterlive.com=eyJkZXZpY2VJZCI6IjY5MzkzY2EyLWE1N2EtNDk3ZS04NzczLTc4NDE3ODEzODNjZlIiLCJ1c2VySWQiOiIxMTMxMjE1NiIsIm9wdE91dCI6ZmFsc2UsInNlc3Npb25JZCI6MTU5MDc1NjU0NzIwOCwibGFzdEV2ZW50VGltZSI6MTU5MDc1ODMwMTU1MiwiZXZlbnRJZCI6MzksImlkZW50aWZ5SWQiOjcwMiwic2VxdWVuY2VOdW1iZXIiOjc0MX0=; G_ENABLED_IDPS=google; xhamsterlive_com_sessionId=1722312519cc53c150099ce43bfd236313dc47811410ff35e06372141870; xhamsterlive_com_sessionRemember=1; cookiesReminder=2020-04-28T11%3A56%3A05.253Z; __cfduid=d4f375713a721338ba8e9f4d8cf6409b01589468942; ABTest_ab_stripscore_viewers2575_key=A; crossDomainAuth=1; xhamsterlive_com_tagPreferred=men")

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
	
	public static String getContentEncoding(HttpResponse<?> response) {
		return response.headers().firstValue("Content-Encoding").orElse(null);
	}
	
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
	
	public static BufferedReader getUtf8BufferedReader(InputStream sourceStream) {
		return new BufferedReader(new InputStreamReader(sourceStream, StandardCharsets.UTF_8));
	}

}
