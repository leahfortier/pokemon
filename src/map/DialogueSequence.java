package map;

import gui.view.MapView;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Game;
import map.triggers.Trigger;

public class DialogueSequence {
	protected static final Pattern multiVariablePattern = Pattern.compile("(\\w+)(?:\\[(\\d+)\\])?:\\s*(?:(\\w+)|\"([^\"]*)\")");
	public String name;
	public String text;
	public String[] next, triggers, choices;
	public DialogueSequence(String name, String contents){
		this.name = name;
		Matcher m = multiVariablePattern.matcher(contents);
		int max = -1;
		next = new String[100];
		triggers = new String[100];
		choices = new String[100];
		int val = -1;
		while (m.find()){
			switch (m.group(1)){
			case "text":
				text = m.group(4);
				break;
			case "next":
				val = Integer.parseInt(m.group(2));
				next[val] = m.group(3);
				break;
			case "choice":
				val = Integer.parseInt(m.group(2));
				if (m.group(3) != null)
					choices[val] = m.group(3);
				else
					choices[val] = m.group(4);
				break;
			case "trigger":
				val = Integer.parseInt(m.group(2));
				triggers[val] = m.group(3);
				break;
			}
			max = Math.max(max, val);
		}
		next = Arrays.copyOf(next, max+1);
		choices = Arrays.copyOf(choices, max+1);
		triggers = Arrays.copyOf(triggers, max+1);
	}
	public void choose(int choiceIndex, MapView mapView, Game game){
		if (choiceIndex < 0 || choiceIndex >= next.length)
			return;
		if (next[choiceIndex] != null){
			mapView.setDialogue(next[choiceIndex]);
		}
		if (triggers[choiceIndex] != null){
			Trigger trigger = game.data.getTrigger(triggers[choiceIndex]);
			if (trigger.isTriggered(game.charData))
				trigger.execute(game);
		}
	}
}
