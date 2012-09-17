package fi.android.spacify.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;
import fi.android.service.EventService;
import fi.android.service.web.WebListener;
import fi.android.service.web.WebService;
import fi.android.service.web.WebServiceException;
import fi.android.spacify.R;
import fi.android.spacify.db.BubbleDatabase;
import fi.android.spacify.model.Bubble;
import fi.spacify.android.util.Events;
import fi.spacify.android.util.WebPipes;

public class ComicParser {

	private static final String TAG = "ComicParser";
	private final WebService WEB = WebService.getInstance();
	private final BubbleDatabase db = BubbleDatabase.getInstance();
	private final EventService es = EventService.getInstance();

	private final String COMIC_BUBBLE_STYLE = "web, comic, fullscreen, landscape";
	private final int COMIC_BUBBLE_ID = -1010101010;

	private static ComicParser instance;
	private Bubble comicTopBubble;
	private Context context;

	private ComicParser(Context context) {
		this.context = context;
		makeCartoonBubble();
	}

	/**
	 * Get singleton instance of CMS.
	 * 
	 * @return ContentManagementService
	 */
	public static ComicParser getInstance() {
		if(instance == null) {
			throw new IllegalStateException(TAG + " not initialized!");
		}
		return instance;
	}

	/**
	 * Initializes CMS.
	 * 
	 * @param context
	 * 
	 */
	public static void init(Context context) {
		if(instance != null) {
			throw new IllegalStateException(TAG + " has already been initialized!");
		}
		instance = new ComicParser(context);
		instance.WEB.openPipe(WebPipes.COMIC_WEB_PIPE);
	}

	public void fetchComics() {
		HttpGet get = new HttpGet(context.getResources().getString(R.string.url_comic_rss));

		Log.d(TAG, "Fetching comics from: " + get.getURI().toString());
		WEB.request(get, comicWebParser, WebPipes.COMIC_WEB_PIPE);
	}

	private WebListener comicWebParser = new WebListener() {

		@Override
		public void successResult(HttpResponse response) {
			String respString = null;
			try {
				respString = EntityUtils.toString(response.getEntity());
				// Log.d(TAG, "Got respose: " + respString);
				response.getEntity().consumeContent();
			} catch(ParseException e) {
				Log.w(TAG, "No valid HTTP responce", e);
			} catch(IOException e) {
				Log.w(TAG, "No valid HTTP responce", e);
			}
			if(respString != null) {
				parseComicRSS(respString);
			}

		}

		@Override
		public void error(WebServiceException e) {
			// TODO Auto-generated method stub

		}
	};
	
	private void parseComicRSS(String comicRSS) {
		// Log.d(TAG, "Parsing: " + comicRSS);
//		Pattern pattern = Pattern.compile(
//				"<item>[^<title>]*" +
//					"<title>([^<]*)</title>[^<link>]*" +
//					"<link>([^<]*)</link>" +
//					"<description>([^<]*)</description>[^<]*" +
//					"<pubDate>([^<]*)</pubDate>[^<]*"+
//					"<guid isPermaLink=\"([^<]*)\">([^<]*)</guid>[^<]*"+
//				"[^</item>]*</item");
		Pattern pattern = Pattern
				.compile("<item>[^<]*<title>(.*) ([0-9][0-9]?.[0-9][0-9]?.[0-9][0-9][0-9][0-9])</title>[^<link>]*<link>([^<]*)</link>");
	    Matcher matcher = pattern.matcher(comicRSS);
	    
		Map<Integer, Bubble> bubbles = new HashMap<Integer, Bubble>();


	    while(matcher.find()) {
	    	int groups = matcher.groupCount();
			String title = matcher.group(1).replaceAll("&amp;", "&");
			String date = matcher.group(2);
			String link = matcher.group(3);

			Log.d(TAG, title + " : " + link);
			Bubble group = new Bubble(title.hashCode());
			group.setTitle(title);
			group.setStyle("");
			group.setContents(title);
			group.setPriority(2);
			group.setTitleImageUrl("");
			// TODO improve so that links are not overwritten
			group.addLink(COMIC_BUBBLE_ID);
			group.addLink(link.hashCode());
			group.setType("comic");
			group.setContentImageUrl("");

			bubbles.put(group.getID(), group);

			Bubble b = new Bubble(link.hashCode());
			b.setTitle(date);
			b.setStyle(COMIC_BUBBLE_STYLE);
			b.setContents(link);
			b.setPriority(1);
			b.setTitleImageUrl("");
			b.addLink(group.getID());
			b.setType("comic");
			b.setContentImageUrl(link);

			bubbles.put(b.getID(), b);

			comicTopBubble.addLink(group.getID());
	    }

		bubbles.put(comicTopBubble.getID(), comicTopBubble);
		db.storeBubbles(new ArrayList<Bubble>(bubbles.values()));
		es.dispatchEvent(Events.COMICS_UPDATED.ordinal());
	}
	
	private void makeCartoonBubble() {
		comicTopBubble = new Bubble(COMIC_BUBBLE_ID);
		comicTopBubble.setTitle("Comics");
		comicTopBubble.setStyle("");
		comicTopBubble.setContents("");
		comicTopBubble.setPriority(3);
		comicTopBubble.setTitleImageUrl("");
		// b.setLinks(links);
		comicTopBubble.setType("comic");
		comicTopBubble.setContentImageUrl("");
	}

}
