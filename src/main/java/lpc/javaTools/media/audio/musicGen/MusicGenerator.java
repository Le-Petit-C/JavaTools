package lpc.javaTools.media.audio.musicGen;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import lpc.javaTools.media.FFmpegUtils;
import lpc.javaTools.media.audio.PCMData;
import lpc.javaTools.media.audio.musicGen.timbers.Timbre;
import lpc.javaTools.utils.math.MathUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicGenerator {
	private static final double soundVelocity = 340;
	private record Note(int beat, double subBeat, Timbre timbre, double sustainBeats, double frequency, double volume) {}
	private record BPMChange(int previousSections, double newBPM) {}
	private final ArrayList<Note> notes = new ArrayList<>();
	private final double startBPM;
	private final int beatsPerSection, sampleRate;
	private final ArrayList<BPMChange> bpmChanges = new ArrayList<>();
	
	// recordDistance是录制时双声道采样的距离，单位是米
	public MusicGenerator(double startBPM, int beatsPerSection, int sampleRate) {
		this.startBPM = startBPM;
		this.beatsPerSection = beatsPerSection;
		this.sampleRate = sampleRate;
	}
	public void addNote(int section, int beat, double subBeat, Timbre timbre, double sustainBeats, double frequency, double volume) {
		notes.add(new Note(section * beatsPerSection + beat, subBeat, timbre, sustainBeats, frequency, volume));
	}
	public void nextBPMChange(int previousSections, double newBPM) {
		bpmChanges.add(new BPMChange(previousSections, newBPM));
	}
	public PCMData generatePCM() {
		double rawLastBeat = MathUtils.doubleMax(notes, note->note.beat + note.subBeat + note.sustainBeats);
		int lastBeat = (int)Math.ceil(rawLastBeat / beatsPerSection) * beatsPerSection;
		DoubleArrayList beatStartTimes = new DoubleArrayList();
		DoubleArrayList beatSustainTimes = new DoubleArrayList();
		double secondPerBeats = 60.0 / startBPM;
		double startTime = 0;
		for (var change : bpmChanges) {
			int beats = change.previousSections * beatsPerSection;
			for(int i = 0; i < beats; ++i) {
				beatStartTimes.add(startTime);
				beatSustainTimes.add(secondPerBeats);
				startTime += secondPerBeats;
			}
			secondPerBeats = 60.0 / change.newBPM;
		}
		for (int i = beatStartTimes.size(); i <= lastBeat + beatsPerSection; ++i) {
			beatStartTimes.add(startTime);
			beatSustainTimes.add(secondPerBeats);
			startTime += secondPerBeats;
		}
		DoubleSampleRecorder samples = new DoubleSampleRecorder();
		for(var note : notes) {
			double startBeat = note.beat + note.subBeat;
			double endBeat = startBeat + note.sustainBeats;
			int flooredStartBeat = (int) Math.floor(startBeat);
			int flooredEndBeat = (int) Math.floor(endBeat);
			double noteStartTime = beatStartTimes.getDouble(flooredStartBeat) + beatSustainTimes.getDouble(flooredStartBeat) * (startBeat - flooredStartBeat);
			double noteEndTime = beatStartTimes.getDouble(flooredEndBeat) + beatSustainTimes.getDouble(flooredEndBeat) * (endBeat - flooredEndBeat);
			note.timbre.superpose(samples, noteStartTime, noteEndTime - noteStartTime, sampleRate, note.frequency, 1);
		}
		double lastSampleTime = beatStartTimes.getDouble(lastBeat);
		while(Math.round(lastSampleTime * sampleRate) < samples.getSampleCount())
			lastSampleTime += secondPerBeats * beatsPerSection;
		return new PCMData(new int[][]{samples.getMaxSamples(new int[(int)Math.round(lastSampleTime * sampleRate)])}, sampleRate);
	}
	public void save(File outputFile) throws IOException {
		FFmpegUtils.encodeFromPCM(generatePCM(), outputFile);
	}
	public void save(String path) throws IOException {
		FFmpegUtils.encodeFromPCM(generatePCM(), path);
	}
}
