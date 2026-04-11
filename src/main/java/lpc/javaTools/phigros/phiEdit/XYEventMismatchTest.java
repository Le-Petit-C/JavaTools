package lpc.javaTools.phigros.phiEdit;

import com.google.gson.*;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class XYEventMismatchTest {
	static void main() throws IOException {
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(System.in));
		String path = reader.readLine();
		File file = new File(path);
		if(file.isFile()) {
			JsonObject element = (JsonObject)JsonParser.parseReader(new FileReader(file));
			JsonArray judgeLineList = (JsonArray)element.get("judgeLineList");
			TestStatus testStatus = new TestStatus();
			testStatus.lineIndex = 0;
			for(var v : judgeLineList) {
				JsonObject judgeLine = (JsonObject)v;
				if(judgeLine.has("eventLayers")) {
					JsonArray eventLayers = (JsonArray)judgeLine.get("eventLayers");
					testStatus.eventLayerIndex = 0;
					for(var v2 : eventLayers) {
						if(!v2.isJsonNull()){
							JsonObject eventLayer = (JsonObject)v2;
							test(testStatus, (JsonArray)eventLayer.get("moveXEvents"), (JsonArray)eventLayer.get("moveYEvents"));
						}
						++testStatus.eventLayerIndex;
					}
				}
				++testStatus.lineIndex;
			}
		}
	}
	static class TestStatus {
		int lineIndex;
		int eventLayerIndex;
		boolean isMoveX;
		void printInfo(String prefix, EventDuration duration) {
			System.out.println(prefix + "line " + lineIndex + ", eventLayer " + eventLayerIndex + ", "
				+ (isMoveX ? "moveXEvent" : "moveYEvent") + ", duration " + duration);
		}
		void printMismatch(EventDuration duration) {
			printInfo("Mismatch event:", duration);
		}
		void printDuplicate(EventDuration duration) {
			printInfo("Duplicate event:", duration);
		}
	}
	record EventTime(int beat, int part, int divider){
		EventTime(JsonArray array) {
			this(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
		}
		@Override public String toString() {
			return "(" + beat + ", " + part + ", " + divider + ")";
		}
	}
	record EventDuration(EventTime start, EventTime end){
		EventDuration(JsonObject eventJson) {
			this(new EventTime((JsonArray)eventJson.get("startTime")),
				new EventTime((JsonArray)eventJson.get("endTime")));
		}
		@Override public String toString() {
			return "{start:" + start.toString() + ", end:" + end.toString() + "}";
		}
	}
	static LinkedHashSet<EventDuration> parseEventDurations(TestStatus status, JsonArray events) {
		LinkedHashSet<EventDuration> res = new LinkedHashSet<>();
		for(var v : events) {
			var duration = new EventDuration((JsonObject)v);
			if(res.contains(duration)) status.printDuplicate(duration);
			else res.add(duration);
		}
		return res;
	}
	static void test(TestStatus status, JsonArray xEvents, JsonArray yEvents) {
		status.isMoveX = true;
		LinkedHashSet<EventDuration> xEventDurations = parseEventDurations(status, xEvents);
		status.isMoveX = false;
		LinkedHashSet<EventDuration> yEventDurations = parseEventDurations(status, yEvents);
		HashSet<EventDuration> xBackup = new HashSet<>(xEventDurations);
		xEventDurations.removeAll(yEventDurations);
		yEventDurations.removeAll(xBackup);
		if(!xEventDurations.isEmpty()) {
			status.isMoveX = true;
			for(var v : xEventDurations) status.printMismatch(v);
		}
		if(!yEventDurations.isEmpty()) {
			status.isMoveX = false;
			for(var v : yEventDurations) status.printDuplicate(v);
		}
	}
}
